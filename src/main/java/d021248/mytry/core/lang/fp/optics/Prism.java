package d021248.mytry.core.lang.fp.optics;

import java.util.Optional;
import java.util.function.Function;

/// A `Prism<S, A>` focuses on one variant of a sum type (sealed interface).
///
/// Where a `Lens<S, A>` always succeeds (every `S` has the field `A`),
/// a `Prism` may fail: `getOption` returns `Optional.empty()` when `s` is
/// not the targeted variant.  Conversely, `review` reconstructs the whole `S`
/// from just an `A` (it is the "constructor" direction).
///
/// ## Laws
///
/// 1. **Review–getOption round-trip**: `getOption(review(a)) == Optional.of(a)`
/// 2. **getOption–review round-trip**: if `getOption(s) == Optional.of(a)` then
///    `review(a) == s`
///
/// ## Composition
///
/// `Prism<S, A>` and `Prism<A, B>` compose into `Prism<S, B>` via `andThen`.
/// A `Lens<S, X>` and `Prism<X, A>` compose into an `AffineTraversal<S, A>`
/// via {@link AffineTraversal#fromLensAndPrism}.
///
/// @param <S> the sum type (e.g. a sealed interface)
/// @param <A> the focused variant's inner value type
public record Prism<S, A>(Function<S, Optional<A>> getOption, Function<A, S> review) {

  // ── Factory ───────────────────────────────────────────────────────────────

  /// Creates a prism from a partial getter and a constructor.
  public static <S, A> Prism<S, A> of(Function<S, Optional<A>> getOption, Function<A, S> review) {
    return new Prism<>(getOption, review);
  }

  // ── Operations ────────────────────────────────────────────────────────────

  /// Returns the focused value when `s` is the targeted variant, else empty.
  public Optional<A> getOption(S s) {
    return getOption.apply(s);
  }

  /// Constructs an `S` from an `A` (the "constructor" direction).
  public S review(A a) {
    return review.apply(a);
  }

  /// Modifies the focused value when present; leaves other variants unchanged.
  public S modify(S s, Function<A, A> f) {
    return getOption(s).map(a -> review(f.apply(a))).orElse(s);
  }

  /// Returns true iff `s` matches this prism's variant.
  public boolean matches(S s) {
    return getOption(s).isPresent();
  }

  // ── Composition ───────────────────────────────────────────────────────────

  /// Composes two prisms: focuses further into a nested variant.
  public <B> Prism<S, B> andThen(Prism<A, B> inner) {
    return Prism.of(s -> getOption(s).flatMap(inner::getOption), b -> review(inner.review(b)));
  }
}
