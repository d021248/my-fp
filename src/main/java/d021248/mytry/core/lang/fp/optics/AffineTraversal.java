package d021248.mytry.core.lang.fp.optics;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/// A partial lens — focuses on a value that may or may not be present.
///
/// `AffineTraversal<S, A>` is the composition of a `Lens` and a `Prism`:
///
/// ```
/// Lens<S, X>  +  Prism<X, A>  =  AffineTraversal<S, A>
/// ```
///
/// In the optics hierarchy it sits between `Lens` (always succeeds) and
/// `Traversal` (zero or many targets):
///
/// | Optic | Targets | Succeeds? |
/// |-------|---------|-----------|
/// | `Lens<S,A>` | exactly one | always |
/// | **`AffineTraversal<S,A>`** | **zero or one** | **maybe** |
/// | `Traversal<S,A>` | zero or many | always |
///
/// ## Laws
///
/// - If `getOption(s) == Optional.of(a)` then `getOption(set(s, a))` = `Optional.of(a)`
/// - `modify(s, identity)` = `s`
public record AffineTraversal<S, A>(Function<S, Optional<A>> getOption, BiFunction<S, A, S> set) {

  /// Creates an affine traversal from a partial getter and a setter.
  public static <S, A> AffineTraversal<S, A> of(
      Function<S, Optional<A>> getOption, BiFunction<S, A, S> set) {
    return new AffineTraversal<>(getOption, set);
  }

  /// Builds an `AffineTraversal<S, B>` from a `Lens<S, X>` and a `Prism<X, B>`.
  ///
  /// The lens always reaches `X`; the prism tries to reach `B` inside `X`.
  /// When the prism does not match, all operations are no-ops.
  public static <S, X, B> AffineTraversal<S, B> fromLensAndPrism(
      Lens<S, X> lens, Prism<X, B> prism) {
    return AffineTraversal.of(
        s -> prism.getOption(lens.get(s)), (s, b) -> lens.modify(s, x -> prism.modify(x, _ -> b)));
  }

  /// Returns the focused value when the prism matches, else empty.
  public Optional<A> getOption(S s) {
    return getOption.apply(s);
  }

  /// Replaces the focused value; no-op when the prism does not match.
  public S set(S s, A a) {
    return set.apply(s, a);
  }

  /// Applies `f` to the focused value; no-op when the prism does not match.
  public S modify(S s, UnaryOperator<A> f) {
    return getOption(s).map(a -> set(s, f.apply(a))).orElse(s);
  }

  /// Composes two affine traversals into a deeper partial focus.
  public <B> AffineTraversal<S, B> andThen(AffineTraversal<A, B> other) {
    return AffineTraversal.of(
        s -> getOption(s).flatMap(other::getOption),
        (s, b) -> getOption(s).map(a -> set(s, other.set(a, b))).orElse(s));
  }
}
