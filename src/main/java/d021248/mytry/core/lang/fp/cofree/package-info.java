/// Cofree comonad — values at every node of a lazily unfolded structure.
///
/// `Cofree F A` = `(A, F (Cofree F A))`: a value `A` at the root paired with
/// an `F`-shaped structure of recursive subtrees.
///
/// ## Duality with Free
///
/// | Concept | Free monad | Cofree comonad |
/// |---------|-----------|----------------|
/// | Structure | tree of *requests* | tree of *values* |
/// | Key op | `flatMap` — sequence effects | `extend` — enrich with context |
/// | Use case | programs as data | streams, cellular automata |
///
/// ## Concrete instantiation
///
/// - `Cofree Identity A` = `CofreeStream A` — a lazy infinite stream
/// - `Cofree List A` = a rose tree with A at every node
/// - `Cofree Maybe A` = a non-empty list
///
/// ## Key types
///
/// - {@link d021248.mytry.core.lang.fp.cofree.CofreeStream} — `Cofree` over the
///   identity functor: a lazy stream with comonad operations
///
/// ## Sub-packages
///
/// - {@link d021248.mytry.core.lang.fp.cofree.broken} — eager {@code ArrayList}-based
///   stream that cannot express infinite structures
///
/// @since 1.0
package d021248.mytry.core.lang.fp.cofree;
