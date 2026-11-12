package d021248.mytry.core.lang.fp.typeclass;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/// Type class for converting a value of type `A` to its string representation.
///
/// `Show<A>` is a *capability*: an instance is an ordinary value (lambda,
/// method reference, or record) rather than a base class.  The same type can
/// have multiple `Show` instances — decimal, hexadecimal, debug — without any
/// subclassing or wrapper objects.
///
/// ## Derived combinators
///
/// - `contramap` lifts a `Show<B>` to a `Show<A>` by mapping `A → B` first
/// - `listShow` derives `Show<List<A>>` from `Show<A>`
/// - `optionalShow` derives `Show<Optional<A>>` from `Show<A>`
@FunctionalInterface
public interface Show<A> {

  /// Returns the string representation of `value`.
  String show(A value);

  // ── Derived combinators ───────────────────────────────────────────────────

  /// Lifts this `Show<B>` into a `Show<A>` by applying `f` before showing.
  default <B> Show<B> contramap(Function<B, A> f) {
    return b -> show(f.apply(b));
  }

  // ── Standard instances ────────────────────────────────────────────────────

  /// `Show<A>` using `Object.toString`.
  static <A> Show<A> natural() {
    return Object::toString;
  }

  /// `Show<List<A>>` from `Show<A>`.
  static <A> Show<List<A>> listShow(Show<A> elem) {
    return list -> {
      var sb = new StringBuilder("[");
      for (int i = 0; i < list.size(); i++) {
        if (i > 0) sb.append(", ");
        sb.append(elem.show(list.get(i)));
      }
      return sb.append("]").toString();
    };
  }

  /// `Show<Optional<A>>` from `Show<A>`.
  static <A> Show<Optional<A>> optionalShow(Show<A> elem) {
    return opt -> opt.map(a -> "Some(%s)".formatted(elem.show(a))).orElse("None");
  }
}
