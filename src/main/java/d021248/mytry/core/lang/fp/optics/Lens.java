package d021248.mytry.core.lang.fp.optics;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/// A composable getter/setter pair for immutable data.
///
/// `Lens<S, A>` focuses on a field of type `A` inside a container `S`.
/// The key operation is `andThen`, which composes two lenses so that a
/// single value object can read or update an arbitrarily-nested field with
/// no repetition of sibling fields.
///
/// ## Laws
///
/// A well-behaved lens satisfies:
///
/// - `get(set(s, a)) == a` — you get back what you set
/// - `set(s, get(s)) == s` — setting the current value is a no-op
/// - `set(set(s, a1), a2) == set(s, a2)` — the last set wins
public record Lens<S, A>(Function<S, A> getter, BiFunction<S, A, S> setter) {

  /// Creates a lens from a getter and a setter.
  public static <S, A> Lens<S, A> of(Function<S, A> getter, BiFunction<S, A, S> setter) {
    return new Lens<>(getter, setter);
  }

  /// Returns the focused value.
  public A get(S s) {
    return getter.apply(s);
  }

  /// Returns a new `S` with the focused value replaced by `a`.
  public S set(S s, A a) {
    return setter.apply(s, a);
  }

  /// Applies `f` to the focused value and returns the updated `S`.
  public S modify(S s, UnaryOperator<A> f) {
    return set(s, f.apply(get(s)));
  }

  /// Composes this lens with `other`.
  ///
  /// If `this` focuses on `A` inside `S`, and `other` focuses on `B`
  /// inside `A`, the composed lens focuses on `B` inside `S`.
  public <B> Lens<S, B> andThen(Lens<A, B> other) {
    return new Lens<>(s -> other.get(get(s)), (s, b) -> set(s, other.set(get(s), b)));
  }
}
