package d021248.mytry.core.lang.fp.typeclass;

import module java.base;

/// Type class for a `Semigroup` extended with an identity element.
///
/// A `Monoid<A>` is a `Semigroup<A>` with an additional `empty()` element
/// such that `combine(empty(), a) == a` and `combine(a, empty()) == a`.
///
/// ## Examples
///
/// | Type | Operation | Identity |
/// |---|---|---|
/// | `String` | concatenation | `""` |
/// | `Integer` | addition | `0` |
/// | `Integer` | multiplication | `1` |
/// | `List<A>` | list append | `List.of()` |
/// | `boolean` | AND | `true` |
/// | `boolean` | OR | `false` |
///
/// ## Laws
///
/// 1. **Left identity**: `combine(empty(), a) == a`
/// 2. **Right identity**: `combine(a, empty()) == a`
/// 3. **Associativity** (from `Semigroup`): `combine(combine(a,b),c) == combine(a,combine(b,c))`
public interface Monoid<A> extends Semigroup<A> {

  /// Returns the identity element for this monoid.
  A empty();

  /// Reduces a possibly-empty list to a single value.
  ///
  /// Returns `empty()` for an empty list; otherwise folds left to right.
  default A mconcat(List<A> elements) {
    var acc = empty();
    for (var e : elements) acc = combine(acc, e);
    return acc;
  }

  /// Creates a `Monoid<A>` from a `BinaryOperator` and an identity element.
  static <A> Monoid<A> of(BinaryOperator<A> op, A identity) {
    return new Monoid<>() {
      @Override
      public A combine(A a, A b) {
        return op.apply(a, b);
      }

      @Override
      public A empty() {
        return identity;
      }
    };
  }

  // ── Standard instances ────────────────────────────────────────────────────

  /// `Monoid<String>` under concatenation.
  static Monoid<String> strings() {
    return Monoid.of(String::concat, "");
  }

  /// `Monoid<Integer>` under addition.
  static Monoid<Integer> intSum() {
    return Monoid.of(Integer::sum, 0);
  }

  /// `Monoid<Integer>` under multiplication.
  static Monoid<Integer> intProduct() {
    return Monoid.of((a, b) -> a * b, 1);
  }

  /// `Monoid<List<A>>` under list concatenation.
  static <A> Monoid<List<A>> lists() {
    return Monoid.of((xs, ys) -> Stream.concat(xs.stream(), ys.stream()).toList(), List.of());
  }
}
