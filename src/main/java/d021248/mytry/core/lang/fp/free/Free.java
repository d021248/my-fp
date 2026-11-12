package d021248.mytry.core.lang.fp.free;

import module java.base;

/// Free monad over the `StoreOp` functor.
///
/// A `Free<A>` value is a **pure data structure** — a description of a
/// key-value store program that produces an `A`.  No I/O happens until
/// `Free.run(program, interpreter)` is called with a concrete interpreter.
///
/// ## Why Free Monad?
///
/// Ordinarily, writing to a store and reading from it are side effects
/// intertwined with business logic.  The Free monad separates the two:
///
/// - **Program layer** — write programs in terms of `get`, `put`, `delete`.
///   Programs are values; they compose with `flatMap` / `andThen`.
/// - **Interpreter layer** — supply meaning at the call site.  Swap in a
///   real HashMap store, a logging store, a dry-run store, or a pure test
///   fixture without changing the program.
///
/// ## Structure
///
/// ```
/// Free<A>
///  ├── Pure<A>       — done, carries value a
///  ├── Lift<A>       — one StoreOp step (op contains the continuation)
///  └── Bind<A,B>     — sequence: run step:Free<B>, feed B into k:B→Free<A>
/// ```
///
/// ## Stack safety
///
/// `run` uses an explicit continuation stack instead of recursion, so
/// arbitrarily deep programs do not cause a stack overflow.
///
/// ## Java type-system limitation
///
/// A fully generic `Free<F, A>` requires a `Functor<F>` higher-kinded constraint
/// (`F` itself parameterised by `A`) that Java's type system cannot express.
/// This implementation is therefore specific to `StoreOp` as its functor and uses
/// raw types with `@SuppressWarnings` in the runner. The structure
/// (Pure / Lift / Bind + stack-safe trampoline interpreter) is representative of
/// the pattern in languages with higher-kinded types (Haskell, Scala/Cats).
///
/// @see StoreOp
public sealed interface Free<A> permits Free.Pure, Free.Lift, Free.Bind {

  // ── Constructors ─────────────────────────────────────────────────────────

  /// Completed program carrying value `a`.
  record Pure<A>(A value) implements Free<A> {}

  /// A single `StoreOp` step lifted into `Free`.
  /// The continuation `Free<A>` lives inside `op` (the functor position).
  record Lift<A>(StoreOp<Free<A>> op) implements Free<A> {}

  /// Sequence: run `step` to obtain a `B`, then continue with `k`.
  record Bind<A, B>(Free<B> step, Function<B, Free<A>> k) implements Free<A> {}

  // ── Smart constructors ───────────────────────────────────────────────────

  /// Returns a program that immediately yields `value` without touching the store.
  static <A> Free<A> pure(A value) {
    return new Pure<>(value);
  }

  /// Returns a program that reads `key` from the store.
  static Free<Optional<String>> get(String key) {
    return new Lift<>(new StoreOp.Get<>(key, Free::pure));
  }

  /// Returns a program that writes `value` at `key`.
  static Free<Void> put(String key, String value) {
    return new Lift<>(new StoreOp.Put<>(key, value, Free.pure(null)));
  }

  /// Returns a program that deletes `key`.
  static Free<Void> delete(String key) {
    return new Lift<>(new StoreOp.Delete<>(key, Free.pure(null)));
  }

  // ── Monad operations ─────────────────────────────────────────────────────

  /// Chains a follow-up computation that depends on this program's result.
  default <B> Free<B> flatMap(Function<A, Free<B>> f) {
    return new Bind<>(this, f);
  }

  /// Transforms this program's result without touching the store.
  default <B> Free<B> map(Function<A, B> f) {
    return flatMap(a -> new Pure<>(f.apply(a)));
  }

  /// Sequences `next` after `this`, discarding `this` program's result.
  default <B> Free<B> andThen(Free<B> next) {
    return flatMap(_ -> next);
  }

  // ── Interpreter API ───────────────────────────────────────────────────────

  /// Executes one `StoreOp` step and returns the continuation as a `Free`.
  /// Implement this to give a `Free` program its execution semantics.
  @FunctionalInterface
  interface Interpreter {
    Free<?> step(StoreOp<Free<?>> op);
  }

  /// Returns an interpreter backed by a mutable `Map`.
  @SuppressWarnings({"unchecked", "rawtypes"})
  static Interpreter mapInterpreter(Map<String, String> store) {
    return op ->
        switch (op) {
          case StoreOp.Get get ->
              ((Function<Optional<String>, Free<?>>) get.next())
                  .apply(Optional.ofNullable(store.get(get.key())));
          case StoreOp.Put put -> {
            store.put(put.key(), put.value());
            yield (Free<?>) put.next();
          }
          case StoreOp.Delete del -> {
            store.remove(del.key());
            yield (Free<?>) del.next();
          }
        };
  }

  /// Returns an interpreter that reads from `readStore` but silently discards writes.
  /// Useful for "what would this program return?" queries without side effects.
  @SuppressWarnings({"unchecked", "rawtypes"})
  static Interpreter dryRunInterpreter(Map<String, String> readStore) {
    return op ->
        switch (op) {
          case StoreOp.Get get ->
              ((Function<Optional<String>, Free<?>>) get.next())
                  .apply(Optional.ofNullable(readStore.get(get.key())));
          case StoreOp.Put put -> (Free<?>) put.next(); // no-op
          case StoreOp.Delete del -> (Free<?>) del.next(); // no-op
        };
  }

  /// Returns an interpreter that records each operation in `log`, then delegates
  /// to `delegate`.  The log entry format is `"get(key)"`, `"put(key, value)"`,
  /// or `"delete(key)"`.
  static Interpreter loggingInterpreter(Interpreter delegate, List<String> log) {
    return op -> {
      switch (op) {
        case StoreOp.Get<?> g -> log.add("get(%s)".formatted(g.key()));
        case StoreOp.Put<?> p -> log.add("put(%s, %s)".formatted(p.key(), p.value()));
        case StoreOp.Delete<?> d -> log.add("delete(%s)".formatted(d.key()));
      }
      return delegate.step(op);
    };
  }

  // ── Runner ────────────────────────────────────────────────────────────────

  /// Runs `program` to completion using `interp` to execute each `StoreOp`.
  ///
  /// The interpreter loop is iterative (not recursive), so deeply nested
  /// `flatMap` chains do not overflow the call stack.
  @SuppressWarnings({"unchecked", "rawtypes"})
  static <A> A run(Free<A> program, Interpreter interp) {
    var stack = new ArrayDeque<Function>();
    Free current = program;

    while (true) {
      switch (current) {
        case Pure p -> {
          if (stack.isEmpty()) return (A) p.value();
          current = (Free) stack.pop().apply(p.value());
        }
        case Lift l -> current = (Free) interp.step(l.op());
        case Bind b -> {
          stack.push(b.k());
          current = b.step();
        }
      }
    }
  }

  // ── Result types ─────────────────────────────────────────────────────────

  /// Result of {@link #runFresh()}: the program's return value and the final store state.
  record RunResult<A>(A value, Map<String, String> store) {}

  /// Result of {@link #runLogged()}: the program's return value and the operation log.
  record LoggedResult<A>(A value, List<String> log) {}

  // ── Convenience shortcuts ─────────────────────────────────────────────────

  /// Runs this program with a fresh empty map; returns the result and the final store state.
  default RunResult<A> runFresh() {
    var store = new HashMap<String, String>();
    return new RunResult<>(Free.run(this, mapInterpreter(store)), store);
  }

  /// Runs this program with a fresh empty map and collects a log of operations.
  default LoggedResult<A> runLogged() {
    var store = new HashMap<String, String>();
    var log = new ArrayList<String>();
    var result = Free.run(this, loggingInterpreter(mapInterpreter(store), log));
    return new LoggedResult<>(result, log);
  }
}
