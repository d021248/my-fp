/// Selective applicative functors — conditional effects without full monadic power.
///
/// A **selective applicative functor** extends `Applicative` with the `select`
/// operation, which allows one of two branches to be skipped based on runtime
/// data — something `Applicative` cannot express without always evaluating
/// both arguments, and something `Monad` handles only by granting full
/// sequential dependency.
///
/// ```
/// select :: f (Either a b) → f (a→b) → f b
/// ```
///
/// The rule is:
///
/// - `Right(b)` → return `b` immediately; the function is not evaluated.
/// - `Left(a)` → the function is required; apply it to `a`.
///
/// This enables **conditional effects** such as "only read config if it was
/// not already cached" without exposing the full power of the monad bind.
///
/// ## Key types
///
///   - {@link d021248.mytry.core.lang.fp.selective.SelectiveOpt} —
///       selective functor over `Optional`, with `select`, `map`, and `ap`
///   - {@link d021248.mytry.core.lang.adt.Either} —
///       simple sum type used as the input to `select`
///
/// @since 1.0
package d021248.mytry.core.lang.fp.selective;
