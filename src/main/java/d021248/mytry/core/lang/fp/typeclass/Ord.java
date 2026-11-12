package d021248.mytry.core.lang.fp.typeclass;

import module java.base;

/// Type class for ordered comparison of values of type `A`.
///
/// `Ord<A>` extends `Eq<A>` with a total order.  Like `Eq` and `Show`, an
/// instance is a plain value — a lambda, method reference, or record —
/// rather than a base class.
///
/// ## Laws
///
/// - **Totality**: for any `a, b` either `compare(a,b) < 0`, `== 0`, or `> 0`
/// - **Antisymmetry**: `compare(a,b) < 0` implies `compare(b,a) > 0`
/// - **Transitivity**: `compare(a,b) < 0 && compare(b,c) < 0` implies
///   `compare(a,c) < 0`
/// - **Consistency with Eq**: `compare(a,b) == 0` iff `eq(a,b)`
@FunctionalInterface
public interface Ord<A> extends Eq<A> {

  /// Returns a negative int, zero, or positive int as `a` is less than,
  /// equal to, or greater than `b`.
  int compare(A a, A b);

  @Override
  default boolean eq(A a, A b) {
    return compare(a, b) == 0;
  }

  /// Returns the lesser of `a` and `b`.
  default A min(A a, A b) {
    return compare(a, b) <= 0 ? a : b;
  }

  /// Returns the greater of `a` and `b`.
  default A max(A a, A b) {
    return compare(a, b) >= 0 ? a : b;
  }

  /// Returns an `Ord<A>` derived from a `Comparator<A>`.
  static <A> Ord<A> fromComparator(Comparator<A> cmp) {
    return cmp::compare;
  }

  /// Natural `Ord` for any `Comparable` type.
  static <A extends Comparable<A>> Ord<A> natural() {
    return Comparable::compareTo;
  }

  /// Reverses the ordering.
  default Ord<A> reversed() {
    return (a, b) -> compare(b, a);
  }

  /// Lifts this `Ord<B>` into an `Ord<A>` by mapping `A → B` first.
  default <B> Ord<B> contramap(Function<B, A> f) {
    return (x, y) -> compare(f.apply(x), f.apply(y));
  }
}
