/// Recursion schemes: catamorphism, anamorphism, and hylomorphism.
///
/// A *recursion scheme* is a higher-order function that captures a canonical
/// pattern of structural recursion — fold, unfold, or both — and separates it
/// from the per-node logic (the algebra or coalgebra).
///
/// ## Key components
///
/// | Concept | Description |
/// |---------|-------------|
/// | `ExprF<A>` | Base functor — the non-recursive shape of the tree |
/// | `Fix` | Fixed point — ties the knot to form a recursive tree |
/// | `fmap` | Lifts `A → B` to `ExprF<A> → ExprF<B>` |
/// | `cata` | Catamorphism — bottom-up fold |
/// | `ana` | Anamorphism — top-down unfold |
/// | `hylo` | Hylomorphism — unfold then fold without an intermediate tree |
///
/// ## Stack safety
///
/// The implementations here are **not** stack-safe: each recursive level
/// consumes a JVM stack frame.  For large trees this will produce a
/// `StackOverflowError`.  Trampolining or continuation-passing transforms
/// (see the `trampoline` and `cps` packages) are required for production-scale
/// recursive structures.
///
/// ## Sub-packages
///
/// - {@link d021248.mytry.core.lang.fp.recursionschemes.broken} — comparison:
///   explicit recursion duplicated across every operation
///
/// @since 1.0
package d021248.mytry.core.lang.fp.recursionschemes;
