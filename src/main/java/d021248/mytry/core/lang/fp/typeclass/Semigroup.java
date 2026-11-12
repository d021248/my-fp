package d021248.mytry.core.lang.fp.typeclass;

import module java.base;

/// Type class for an associative binary operation on values of type `A`.
///
/// A `Semigroup<A>` provides a single operation `combine` that must be
/// associative: `combine(combine(a,b),c) == combine(a,combine(b,c))`.
///
/// ## Examples
///
/// | Type | Operation | Instance |
/// |---|---|---|
/// | `String` | concatenation | `Semigroup.of(String::concat)` |
/// | `Integer` | addition | `Semigroup.of(Integer::sum)` |
/// | `List<A>` | list append | `Semigroup.of((xs,ys) -> Stream.concat(...))` |
/// | `boolean` | AND | `Semigroup.of(Boolean::logicalAnd)` |
///
/// ## Law
///
/// **Associativity**: `combine(combine(a,b),c) == combine(a,combine(b,c))`
@FunctionalInterface
public interface Semigroup<A> {

  /// Combines two values of type `A`.
  A combine(A a, A b);

  /// Returns a new `Semigroup` that applies this one twice in sequence
  /// with a fixed `delimiter` in between.
  default Semigroup<A> intercalate(A delimiter) {
    return (a, b) -> combine(combine(a, delimiter), b);
  }

  /// Reduces a non-empty list using this semigroup.
  ///
  /// @throws NoSuchElementException if `elements` is empty
  default A sconcat(List<A> elements) {
    var it = elements.iterator();
    var acc = it.next();
    while (it.hasNext()) acc = combine(acc, it.next());
    return acc;
  }

  /// Creates a `Semigroup` from a `BinaryOperator`.
  static <A> Semigroup<A> of(BinaryOperator<A> op) {
    return op::apply;
  }
}
