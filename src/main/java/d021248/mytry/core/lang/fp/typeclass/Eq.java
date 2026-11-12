package d021248.mytry.core.lang.fp.typeclass;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

/// Type class for equality between values of type `A`.
///
/// Like {@link Show}, `Eq<A>` is a capability value, not a base class.
/// The built-in `equals` method is tied to the class hierarchy; `Eq` decouples
/// equality from the type, allowing multiple notions of equality for the same
/// type (case-insensitive strings, approximate floating-point, etc.).
///
/// ## Laws
///
/// - **Reflexive**: `eq(a, a) == true`
/// - **Symmetric**: `eq(a, b) == eq(b, a)`
/// - **Transitive**: `eq(a, b) && eq(b, c)` implies `eq(a, c)`
@FunctionalInterface
public interface Eq<A> {

  /// Returns `true` if `a` and `b` are considered equal.
  boolean eq(A a, A b);

  /// Returns `true` if `a` and `b` are not equal.
  default boolean neq(A a, A b) {
    return !eq(a, b);
  }

  /// Lifts this `Eq<B>` into an `Eq<A>` by mapping both values through `f`
  /// before comparing.
  default <B> Eq<B> contramap(Function<B, A> f) {
    return (x, y) -> eq(f.apply(x), f.apply(y));
  }

  // ── Standard instances ────────────────────────────────────────────────────

  /// `Eq<A>` using {@link Objects#equals}.
  static <A> Eq<A> natural() {
    return Objects::equals;
  }

  /// `Eq<String>` that ignores case.
  static Eq<String> caseInsensitive() {
    return String::equalsIgnoreCase;
  }

  /// `Eq<Double>` within an absolute tolerance.
  static Eq<Double> approximate(double epsilon) {
    return (a, b) -> Math.abs(a - b) <= epsilon;
  }

  /// `Eq<A>` derived from a `Comparator<A>`.
  static <A> Eq<A> fromComparator(Comparator<A> cmp) {
    return (a, b) -> cmp.compare(a, b) == 0;
  }
}
