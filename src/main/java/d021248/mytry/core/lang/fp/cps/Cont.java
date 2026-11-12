package d021248.mytry.core.lang.fp.cps;

import java.util.function.Function;

/// The continuation monad — a computation that produces `A` given a
/// continuation `k: A → R`.
///
/// In direct style a function returns its result; the caller decides what to
/// do next. With `Cont<A, R>`, the computation receives *what to do next*
/// (the continuation) as an argument and calls it instead of returning.
/// This reversal of control makes early exit, backtracking, and stepping
/// possible without exceptions or mutable state.
///
/// ## Core operations
///
/// - `pure(a)` — wraps a plain value; immediately calls the continuation
/// - `map(f)` — transforms the produced value
/// - `flatMap(f)` — sequences two continuations
/// - `callcc(f)` — captures the current continuation so `f` can call it
///   to exit early with any value, bypassing all subsequent steps
///
/// ## Running
///
/// Use `Cont.eval(cont)` to run a `Cont<A, A>` with the identity continuation.
/// The static form enforces `R == A` at compile time, eliminating unchecked
/// casts.
/// For `Cont<A, R>` where `R ≠ A`, pass an explicit continuation to `run`.
public record Cont<A, R>(Function<Function<A, R>, R> run) {

  /// Wraps a plain value — calls the continuation immediately.
  public static <A, R> Cont<A, R> pure(A value) {
    return new Cont<>(k -> k.apply(value));
  }

  /// Transforms the produced value without changing the continuation type.
  public <B> Cont<B, R> map(Function<A, B> f) {
    return flatMap(a -> Cont.pure(f.apply(a)));
  }

  /// Sequences this computation with another; passes this result to `f`.
  public <B> Cont<B, R> flatMap(Function<A, Cont<B, R>> f) {
    return new Cont<>(k -> run.apply(a -> f.apply(a).run.apply(k)));
  }

  /// Captures the current continuation.
  ///
  /// `f` receives the continuation `k` as a first-class value. Calling
  /// `k.apply(v)` immediately finishes the computation with `v`, ignoring
  /// any steps that follow in the chain.
  public static <A, R> Cont<A, R> callcc(Function<Function<A, Cont<A, R>>, Cont<A, R>> f) {
    return new Cont<>(k -> f.apply(a -> new Cont<>(_ -> k.apply(a))).run.apply(k));
  }

  /// Runs a `Cont<A, A>` with the identity continuation, returning the result.
  ///
  /// The `R == A` constraint is enforced at compile time — no unchecked casts.
  public static <A> A eval(Cont<A, A> cont) {
    return cont.run.apply(Function.identity());
  }
}
