/// Optics — composable, first-class access into immutable data structures.
///
/// An optic is a getter/setter pair generalised over *how many* targets it
/// focuses on. This package collects the whole family in one place, from the
/// most general (`Iso`, a reversible bijection) down to the multi-focus
/// `Traversal`. Each is a record that composes with the others via `andThen`.
///
/// ## The optics hierarchy
///
/// | Optic | Focuses on | Succeeds? | Reversible? |
/// |---|---|---|---|
/// | `Iso<S,A>` | exactly one value (lossless) | always | **yes** |
/// | `Lens<S,A>` | exactly one field (product type) | always | no |
/// | `Prism<S,A>` | one variant of a sum type | 0 or 1 | no |
/// | `AffineTraversal<S,A>` | one optional field (Lens ∘ Prism) | 0 or 1 | no |
/// | `Traversal<S,A>` | all elements of a collection | 0..N | no |
///
/// Weaker optics are derivable from stronger ones: an `Iso` downcasts to a
/// `Lens`, and `AffineTraversal.fromLensAndPrism(lens, prism)` builds a partial
/// lens by combining the two.
///
/// ## Contents
///
/// - {@link d021248.mytry.core.lang.fp.optics.Iso} — invertible bijection; the most general optic
/// - {@link d021248.mytry.core.lang.fp.optics.Lens} — getter/setter for one field of a product type
/// - {@link d021248.mytry.core.lang.fp.optics.Prism} — partial getter + constructor for a variant
/// - {@link d021248.mytry.core.lang.fp.optics.AffineTraversal} — partial lens (Lens ∘ Prism)
/// - {@link d021248.mytry.core.lang.fp.optics.Traversal} — multi-focus over collections
/// - {@link d021248.mytry.core.lang.fp.optics.LensDemo} — nested-record update demo
/// - {@link d021248.mytry.core.lang.fp.optics.IsoDemo} — temperature-conversion demo
/// - {@link d021248.mytry.core.lang.fp.optics.OpticsDemo} — expression AST demo
///
/// ## Sub-packages
///
/// - {@link d021248.mytry.core.lang.fp.optics.broken} — anti-pattern: manual `new`
///   chains to update a deeply-nested field; verbose, error-prone, does not compose
///
/// @since 1.0
package d021248.mytry.core.lang.fp.optics;
