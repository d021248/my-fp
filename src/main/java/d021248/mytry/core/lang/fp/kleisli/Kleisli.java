package d021248.mytry.core.lang.fp.kleisli;

import module java.base;

/// A monadic function `A → Optional<B>`, composable with the Kleisli arrow `>=>`.
///
/// A **Kleisli arrow** for a monad `M` is a function `A → M<B>`.  Here `M`
/// is `Optional`, modelling safe partial functions that may fail.
///
/// ## Kleisli composition `>=>`
///
/// ```
/// (A → Optional<B>) >=> (B → Optional<C>)  =  A → Optional<C>
/// ```
///
/// `andThen` implements `>=>`: run the first function; if it succeeds, pipe
/// the result into the second.  An empty result short-circuits to empty.
///
/// ## Monad laws = Category laws
///
/// Kleisli arrows form a **category**:
///
/// | Category law | Kleisli form |
/// |---|---|
/// | Left identity | `identity().andThen(f)` = `f` |
/// | Right identity | `f.andThen(identity())` = `f` |
/// | Associativity | `(f >=> g) >=> h` = `f >=> (g >=> h)` |
///
/// These are exactly the monad laws rewritten for Kleisli arrows, showing
/// that **a monad is a monoid in the category of Kleisli arrows**.
@FunctionalInterface
public interface Kleisli<A, B> {

  /// Runs this arrow on the given input.
  Optional<B> run(A input);

  /// Wraps a function into a Kleisli arrow.
  static <A, B> Kleisli<A, B> of(Function<A, Optional<B>> fn) {
    return fn::apply;
  }

  /// The identity Kleisli arrow: always succeeds, wrapping its input in `Optional.of`.
  static <A> Kleisli<A, A> identity() {
    return Optional::of;
  }

  /// Lifts a total function into a Kleisli arrow that always succeeds.
  static <A, B> Kleisli<A, B> lift(Function<A, B> f) {
    return a -> Optional.of(f.apply(a));
  }

  /// Returns a Kleisli arrow that ignores its input and always returns `value`.
  static <A, B> Kleisli<A, B> pure(B value) {
    return _ -> Optional.of(value);
  }

  /// Composes this arrow with `other` (`>=>`): run `this`, then `other`.
  ///
  /// If `this` returns empty, `other` is never called.
  default <C> Kleisli<A, C> andThen(Kleisli<B, C> other) {
    return a -> run(a).flatMap(other::run);
  }

  /// Reverse composition: `other >=> this`.
  default <C> Kleisli<C, B> compose(Kleisli<C, A> other) {
    return other.andThen(this);
  }

  /// Transforms the output type (functor `map` over the result).
  default <C> Kleisli<A, C> map(Function<B, C> f) {
    return a -> run(a).map(f);
  }
}
