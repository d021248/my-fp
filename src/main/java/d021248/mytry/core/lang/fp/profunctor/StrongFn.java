package d021248.mytry.core.lang.fp.profunctor;

import module java.base;

/// A `ProfunctorFn` extended with `first` and `second` — the operations
/// that make Lens encoding possible.
///
/// A **Strong** profunctor can operate on one component of a pair while
/// carrying the other component through unchanged.
///
/// ## Lens as a Strong profunctor transformer
///
/// A `Lens<S, A>` is representable as:
///
/// ```
/// forall p. Strong p => p A A -> p S S
/// ```
///
/// In Java: a `Function<StrongFn<A, A>, StrongFn<S, S>>` built from a getter
/// and a setter.  When you compose two such functions — `lens1.andThen(lens2)` —
/// you get a composed lens for free.
@FunctionalInterface
public interface StrongFn<A, B> extends ProfunctorFn<A, B> {

  /// Lifts this function to operate on the **first** component of a pair,
  /// leaving the second component unchanged.
  default <C> StrongFn<Pair<A, C>, Pair<B, C>> first() {
    return ac -> new Pair<>(apply(ac.first()), ac.second());
  }

  /// Lifts this function to operate on the **second** component of a pair,
  /// leaving the first component unchanged.
  default <C> StrongFn<Pair<C, A>, Pair<C, B>> second() {
    return ca -> new Pair<>(ca.first(), apply(ca.second()));
  }

  /// Lifts a `Function` into a `StrongFn`.
  static <A, B> StrongFn<A, B> of(Function<A, B> f) {
    return f::apply;
  }

  /// A simple immutable pair.
  record Pair<A, B>(A first, B second) {}
}
