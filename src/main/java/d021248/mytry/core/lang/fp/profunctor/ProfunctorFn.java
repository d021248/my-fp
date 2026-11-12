package d021248.mytry.core.lang.fp.profunctor;

import module java.base;

/// A function `A → B` enriched with a profunctor structure.
///
/// A **profunctor** generalises a function in both input and output simultaneously:
///
/// - `map` (covariant): transforms the output `B → D`
/// - `contramap` (contravariant): transforms the input `C → A`
/// - `dimap` (both at once): `(C → A, B → D) → ProfunctorFn<C, D>`
///
/// ## Why profunctors matter
///
/// Every optic (Lens, Prism, Traversal) is a **profunctor transformer**:
/// a function `p A A → p S S` for a specific class of profunctors `p`.
///
/// - `Lens` requires `Strong p` (`first`/`second`)
/// - `Prism` requires `Choice p` (`left`/`right`)
///
/// The composition of optics = function composition of profunctor transformers.
/// This is why optics compose so elegantly with `andThen` / `.`.
@FunctionalInterface
public interface ProfunctorFn<A, B> {

  /// Applies this function.
  B apply(A input);

  /// Transforms both the input (contravariantly) and the output (covariantly).
  default <C, D> ProfunctorFn<C, D> dimap(Function<C, A> f, Function<B, D> g) {
    return c -> g.apply(apply(f.apply(c)));
  }

  /// Transforms only the input (pre-processes before applying this function).
  default <C> ProfunctorFn<C, B> contramap(Function<C, A> f) {
    return c -> apply(f.apply(c));
  }

  /// Transforms only the output (post-processes the result of this function).
  default <D> ProfunctorFn<A, D> rmap(Function<B, D> g) {
    return a -> g.apply(apply(a));
  }

  /// Lifts a `Function` into a `ProfunctorFn`.
  static <A, B> ProfunctorFn<A, B> of(Function<A, B> f) {
    return f::apply;
  }
}
