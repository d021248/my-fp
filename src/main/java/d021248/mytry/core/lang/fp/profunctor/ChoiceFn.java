package d021248.mytry.core.lang.fp.profunctor;

import module java.base;

import d021248.mytry.core.lang.adt.Either;

/// A `ProfunctorFn` extended with `left` and `right` — the operations
/// that make Prism encoding possible.
///
/// A **Choice** profunctor can operate on one branch of a sum type while
/// leaving the other branch unchanged.
///
/// ## Prism as a Choice profunctor transformer
///
/// A `Prism<S, A>` is representable as:
///
/// ```
/// forall p. Choice p => p A A -> p S S
/// ```
///
/// In Java: a `Function<ChoiceFn<A, A>, ChoiceFn<S, S>>` built from a
/// `getOption` and a `review`.  When you compose two such functions the
/// result is a composed prism for free — no explicit `andThen` needed.
///
/// ## Relationship to `StrongFn`
///
/// | Optic | Required class | Operation |
/// |---|---|---|
/// | `Lens<S,A>` | `Strong` | `first` / `second` (product) |
/// | `Prism<S,A>` | `Choice` | `left` / `right` (sum) |
/// | `AffineTraversal` | `Strong ∩ Choice` | both |
@FunctionalInterface
public interface ChoiceFn<A, B> extends ProfunctorFn<A, B> {

  /// Lifts this function to operate on the **left** branch of an `Either`,
  /// passing `Right` values through unchanged.
  default <C> ChoiceFn<Either<A, C>, Either<B, C>> left() {
    return ac ->
        switch (ac) {
          case Either.Left<A, C>(var a) -> Either.left(apply(a));
          case Either.Right<A, C>(var c) -> Either.right(c);
        };
  }

  /// Lifts this function to operate on the **right** branch of an `Either`,
  /// passing `Left` values through unchanged.
  default <C> ChoiceFn<Either<C, A>, Either<C, B>> right() {
    return ca ->
        switch (ca) {
          case Either.Left<C, A>(var c) -> Either.left(c);
          case Either.Right<C, A>(var a) -> Either.right(apply(a));
        };
  }

  /// Lifts a `Function` into a `ChoiceFn`.
  static <A, B> ChoiceFn<A, B> of(Function<A, B> f) {
    return f::apply;
  }
}
