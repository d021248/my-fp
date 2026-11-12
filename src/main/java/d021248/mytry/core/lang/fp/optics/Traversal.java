package d021248.mytry.core.lang.fp.optics;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/// A `Traversal<S, A>` focuses on **zero or more** `A` values inside an `S`.
///
/// Where a `Lens` focuses on exactly one value and a `Prism` on at most one,
/// a `Traversal` can reach into all elements of a collection or all fields
/// of a given type simultaneously.
///
/// ## Laws
///
/// 1. **Purity**: `modify(s, identity) == s`
/// 2. **Composition of modifications**: `modify(modify(s, f), g) == modify(s, g ∘ f)`
///
/// ## Composition
///
/// `Traversal<S, A>` and `Traversal<A, B>` compose into `Traversal<S, B>` via `andThen`.
///
/// @param <S> the whole structure
/// @param <A> the type of each focused element
public record Traversal<S, A>(
    Function<S, List<A>> getAll, BiFunction<S, Function<A, A>, S> modify) {

  // ── Factory ───────────────────────────────────────────────────────────────

  /// Creates a traversal from a "get all" extractor and a "modify each" function.
  public static <S, A> Traversal<S, A> of(
      Function<S, List<A>> getAll, BiFunction<S, Function<A, A>, S> modify) {
    return new Traversal<>(getAll, modify);
  }

  /// Creates a traversal over the elements of a `List<A>`.
  public static <A> Traversal<List<A>, A> forList() {
    return Traversal.of(List::copyOf, (list, f) -> list.stream().map(f).toList());
  }

  // ── Operations ────────────────────────────────────────────────────────────

  /// Returns all focused values as a `List`.
  public List<A> getAll(S s) {
    return getAll.apply(s);
  }

  /// Applies `f` to every focused value and returns the modified `S`.
  public S modify(S s, Function<A, A> f) {
    return modify.apply(s, f);
  }

  /// Replaces every focused value with `value`.
  public S set(S s, A value) {
    return modify(s, _ -> value);
  }

  // ── Composition ───────────────────────────────────────────────────────────

  /// Composes two traversals: focuses into each element's inner traversal.
  public <B> Traversal<S, B> andThen(Traversal<A, B> inner) {
    return Traversal.of(
        s -> getAll(s).stream().flatMap(a -> inner.getAll(a).stream()).toList(),
        (s, f) -> modify(s, a -> inner.modify(a, f)));
  }
}
