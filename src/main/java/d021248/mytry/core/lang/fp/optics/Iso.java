package d021248.mytry.core.lang.fp.optics;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/// An invertible bijection between `S` and `A` — the lossless optic.
///
/// `Iso<S, A>` sits at the top of the optics hierarchy: it is more powerful
/// than both `Lens` and `Prism` because it can be used as either one.
/// Every `Iso` can be downcast to a `Lens` (via `asLens()`) or to a `Prism`
/// (via `asPrism()`), but not every `Lens` or `Prism` is an `Iso`.
///
/// ## Optics hierarchy
///
/// | Optic | Focuses on | Succeeds? | Reversible? |
/// |---|---|---|---|
/// | `Iso<S,A>` | exactly one value | always | **yes** |
/// | `Lens<S,A>` | exactly one field | always | no |
/// | `Prism<S,A>` | one sum-type variant | 0 or 1 | no |
///
/// ## Laws
///
/// A well-formed `Iso` must satisfy both round-trip laws:
///
/// 1. **get-reverseGet**: `get(reverseGet(a)) == a`
/// 2. **reverseGet-get**: `reverseGet(get(s)) == s`
///
/// These laws guarantee a lossless, bidirectional conversion.
///
/// ## Composition
///
/// `Iso<S, A>` and `Iso<A, B>` compose into `Iso<S, B>` via `andThen`.
///
/// @param <S> the source type
/// @param <A> the target type
public record Iso<S, A>(Function<S, A> get, Function<A, S> reverseGet) {

  // ── Factory ───────────────────────────────────────────────────────────────

  /// Creates an `Iso` from a forward function and an inverse function.
  public static <S, A> Iso<S, A> of(Function<S, A> get, Function<A, S> reverseGet) {
    return new Iso<>(get, reverseGet);
  }

  // ── Operations ────────────────────────────────────────────────────────────

  /// Converts an `S` to an `A` using the forward function.
  public A get(S s) {
    return get.apply(s);
  }

  /// Converts an `A` back to an `S` using the inverse function.
  public S reverseGet(A a) {
    return reverseGet.apply(a);
  }

  /// Returns the inverse `Iso<A, S>` by swapping the two functions.
  public Iso<A, S> reverse() {
    return Iso.of(reverseGet, get);
  }

  /// Applies `f` to the focused `A` and returns the updated `S`.
  ///
  /// Equivalent to `reverseGet(f(get(s)))`.
  public S modify(S s, UnaryOperator<A> f) {
    return reverseGet(f.apply(get(s)));
  }

  // ── Composition ───────────────────────────────────────────────────────────

  /// Composes this `Iso<S, A>` with `other` to produce `Iso<S, B>`.
  public <B> Iso<S, B> andThen(Iso<A, B> other) {
    return Iso.of(s -> other.get(get(s)), b -> reverseGet(other.reverseGet(b)));
  }

  // ── Downcasts ─────────────────────────────────────────────────────────────

  /// Downcasts this `Iso` to a `Lens<S, A>`.
  ///
  /// The setter ignores the current `S` and reconstructs it entirely from `A`
  /// via `reverseGet`, which is valid because an `Iso` is lossless.
  public Lens<S, A> asLens() {
    return Lens.of(get, (_, a) -> reverseGet(a));
  }

  /// Downcasts this `Iso` to a `Prism<S, A>`.
  ///
  /// The `getOption` always succeeds (wrapping the result in `Optional.of`),
  /// and `review` is exactly `reverseGet`.
  public Prism<S, A> asPrism() {
    return Prism.of(s -> Optional.of(get(s)), reverseGet);
  }
}
