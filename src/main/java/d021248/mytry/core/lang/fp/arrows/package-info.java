/// Hughes Arrows — composable computations that generalise functions.
///
/// ## What is an Arrow?
///
/// An **Arrow** `A ~> B` is a generalisation of a function `A → B`.  Where
/// a plain function only composes end-to-end, an arrow also supports
/// *parallel* composition and *fan-out* via structured pairs.
///
/// Arrows were introduced by John Hughes (2000) to capture computation patterns
/// that are more structured than monads but more flexible than applicatives.
///
/// ## Core operations
///
/// - `arr(f)` — lift any total function into an arrow
/// - `andThen(g)` — sequential composition
/// - `first()` — apply the arrow to the first element of a pair
/// - `second()` — apply the arrow to the second element of a pair
/// - `fanout(g)` — apply both arrows to the same input, collect results in a pair
///
/// ## Arrow laws
///
/// 1. `arr(id)` = identity arrow
/// 2. `arr(f).andThen(arr(g))` = `arr(f.andThen(g))`
/// 3. `a.first().andThen(arr(Pair::first))` = `arr(Pair::first).andThen(a)`
///
/// ## Relationship to other packages
///
/// - **`kleisli`** — a Kleisli arrow `A → Optional<B>` is an arrow over `Optional`;
///   it supports `andThen` through monadic bind but cannot implement `first` without
///   distributing `Optional` over the pair, making it a weaker structure
/// - **`functional`** — plain `Function` composition is a degenerate arrow with
///   no parallel structure
///
/// @since 1.0
package d021248.mytry.core.lang.fp.arrows;
