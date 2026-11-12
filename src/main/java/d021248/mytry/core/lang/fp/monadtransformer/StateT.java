package d021248.mytry.core.lang.fp.monadtransformer;

import module java.base;

/// Monad transformer: State layered over Optional — `S → Optional<(A, S)>`.
///
/// `StateT<S, A>` extends the plain `State<S, A>` with the ability to
/// **fail**: each step threads mutable-looking state through the pipeline
/// but may also produce `Optional.empty()`, at which point all subsequent
/// steps are skipped.
///
/// ## Stacking monads
///
/// A plain `State<S, A>` models "state threading" but cannot fail. A plain
/// `Optional<A>` can fail but carries no state. `StateT` gives you
/// **both at once**: stateful computations that short-circuit on failure.
///
/// ## Core operations
///
/// | Operation       | Meaning |
/// |-----------------|---------|
/// | `pure(a)`       | Succeed with `a`, pass state unchanged |
/// | `fail()`        | Always produce `Optional.empty()` |
/// | `get()`         | Return current state as the value |
/// | `put(s)`        | Replace state, return `null` as value |
/// | `modify(f)`     | Apply `f` to state, return new state as value |
/// | `map(f)`        | Transform the value inside the optional pair |
/// | `flatMap(f)`    | Sequence, threading state, short-circuiting on empty |
///
/// ## Example
///
/// ```java
/// StateT<Integer, String> bounded =
///     StateT.<Integer>get()
///           .flatMap(n -> n < 3
///               ? StateT.<Integer>modify(s -> s + 1).map(s -> "tick " + s)
///               : StateT.fail());
///
/// System.out.println(bounded.run(2)); // Optional[(tick 3, 3)]
/// System.out.println(bounded.run(3)); // Optional.empty
/// ```
@FunctionalInterface
public interface StateT<S, A> {

  /// Runs this computation starting from `state`, returning
  /// an optional pair of `(value, newState)`.
  Optional<Pair<A, S>> run(S state);

  // ─── Inner pair type ─────────────────────────────────────────────────────

  /// Holds a result value `A` and the updated state `S` from one step.
  record Pair<A, S>(A value, S state) {}

  // ─── Constructors ─────────────────────────────────────────────────────────

  /// Returns a `StateT` that succeeds with `value`, passing state unchanged.
  static <S, A> StateT<S, A> pure(A value) {
    return s -> Optional.of(new Pair<>(value, s));
  }

  /// Returns a `StateT` that always fails.
  static <S, A> StateT<S, A> fail() {
    return _ -> Optional.empty();
  }

  /// Returns a `StateT` that exposes the current state as the result.
  static <S> StateT<S, S> get() {
    return s -> Optional.of(new Pair<>(s, s));
  }

  /// Returns a `StateT` that replaces the state with `newState`.
  static <S> StateT<S, Void> put(S newState) {
    return _ -> Optional.of(new Pair<>(null, newState));
  }

  /// Returns a `StateT` that applies `f` to the state, returning the new state.
  static <S> StateT<S, S> modify(UnaryOperator<S> f) {
    return s -> {
      var newS = f.apply(s);
      return Optional.of(new Pair<>(newS, newS));
    };
  }

  // ─── Functor / Monad ─────────────────────────────────────────────────────

  /// Transforms the result value; propagates empty and threads state.
  default <B> StateT<S, B> map(Function<A, B> f) {
    return s -> run(s).map(p -> new Pair<>(f.apply(p.value()), p.state()));
  }

  /// Sequences two `StateT` computations, threading state, short-circuiting on empty.
  default <B> StateT<S, B> flatMap(Function<A, StateT<S, B>> f) {
    return s -> run(s).flatMap(p -> f.apply(p.value()).run(p.state()));
  }
}
