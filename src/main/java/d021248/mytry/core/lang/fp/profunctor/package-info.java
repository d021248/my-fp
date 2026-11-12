/// Profunctors — the abstraction underlying all optics.
///
/// A profunctor is like a function `A → B` but generalised in both directions:
/// the input can be **contravariantly** transformed (pre-processed), and the
/// output can be **covariantly** transformed (post-processed).
///
/// ## Key operations
///
/// | Operation | Signature | Effect |
/// |-----------|-----------|--------|
/// | `dimap` | `(C→A, B→D) → p<C,D>` | Pre- and post-process |
/// | `contramap` | `(C→A) → p<C,B>` | Pre-process only |
/// | `rmap` | `(B→D) → p<A,D>` | Post-process only |
///
/// ## Profunctor optics
///
/// Every optic is a profunctor transformer — a function `p A A → p S S`
/// for a specific class of profunctors `p`:
///
/// - **Lens**: requires `Strong p` (`first`/`second`)
/// - **Prism**: requires `Choice p` (`left`/`right`)
///
/// Composing optics = composing profunctor transformers with `andThen`.
///
/// ## Key types
///
/// - `ProfunctorFn` — `@FunctionalInterface` wrapping `A → B` with
///   `dimap`, `contramap`, `rmap`
/// - `StrongFn` — extends `ProfunctorFn` with `first`/`second` for Lens encoding
///
/// ## Sub-packages
///
/// - `d021248.mytry.core.lang.fp.profunctor.broken` — manual getter/setter
///   composition without the profunctor encoding
///
/// @since 1.0
package d021248.mytry.core.lang.fp.profunctor;
