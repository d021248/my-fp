/// Kleisli arrows — composable monadic functions `A → M<B>`.
///
/// A **Kleisli arrow** lifts an ordinary function `A → B` into the context
/// of a monad `M`: `A → M<B>`.  The Kleisli composition operator `>=>` connects
/// two such arrows:
///
/// ```
/// (A → M<B>) >=> (B → M<C>)  =  A → M<C>
/// ```
///
/// ## Monad laws = Category laws
///
/// Kleisli arrows form a **category** under `>=>`:
///
/// - **Left identity**: `identity >=> f` = `f`
/// - **Right identity**: `f >=> identity` = `f`
/// - **Associativity**: `(f >=> g) >=> h` = `f >=> (g >=> h)`
///
/// These are exactly the monad laws, showing that **a monad is precisely a
/// monoid in the category of Kleisli arrows**.
///
/// ## Key types
///
/// - {@link d021248.mytry.core.lang.fp.kleisli.Kleisli} — `@FunctionalInterface`
///   wrapping `A → Optional<B>` with `andThen` (Kleisli composition), `compose`,
///   and functor `map`
///
/// ## Sub-packages
///
/// - {@link d021248.mytry.core.lang.fp.kleisli.broken} — manual chaining with
///   explicit `Optional` checks
///
/// @since 1.0
package d021248.mytry.core.lang.fp.kleisli;
