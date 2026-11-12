/// Bifunctor — a type constructor that supports mapping over both type parameters.
///
/// A `Bifunctor<A, B>` generalises `Functor` from one type parameter to two:
/// `bimap(f, g)` applies `f` to the first parameter and `g` to the second.
///
/// ## Relationship to Functor
///
/// A `Functor` fixes one type parameter and maps over the other.
/// A `Bifunctor` leaves both free and maps over each independently:
///
/// | Type class | Type params | Map method | Fixes |
/// |---|---|---|---|
/// | `Functor<F>` | one | `map(f)` | nothing (single param) |
/// | `Bifunctor<A,B>` | two | `bimap(f, g)` | nothing |
/// | Right-biased Functor | two | `mapSecond(g)` | first param |
///
/// ## Implementations
///
/// - {@link d021248.mytry.core.lang.fp.bifunctor.Bifunctor.Pair} —
///   product type: holds both A and B; `bimap` transforms each independently
/// - {@link d021248.mytry.core.lang.fp.bifunctor.Bifunctor.Either} —
///   sum type: holds exactly one of L or R; `bimap` applies `f` to Left or
///   `g` to Right
///
/// ## Contents
///
/// - {@link d021248.mytry.core.lang.fp.bifunctor.Bifunctor} — sealed interface
///   with `Pair`, `Either.Left`, and `Either.Right`
/// - {@link d021248.mytry.core.lang.fp.bifunctor.BifunctorDemo} — usage demo
///
/// @since 1.0
package d021248.mytry.core.lang.fp.bifunctor;
