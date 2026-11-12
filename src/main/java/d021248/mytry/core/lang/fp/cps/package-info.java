/// Continuation-passing style (CPS) — passing "what to do next" explicitly.
///
/// In direct style a function *returns* its result; the caller decides what
/// to do with it.  In CPS the function receives a *continuation* — a callback
/// representing the rest of the computation — and calls it instead of
/// returning.  This inverts control, enabling early exit, backtracking, and
/// coroutine-like stepping without stack overflow or exceptions.
///
/// ## Key types
///
///   - {@link d021248.mytry.core.lang.fp.cps.Cont} — monad with `pure`,
///       `map`, `flatMap`, and `callcc` for structured early exit
///
/// ## Sub-packages
///
///   - {@link d021248.mytry.core.lang.fp.cps.broken} — anti-pattern: using
///       exceptions as non-local exits from a pipeline; expensive, opaque,
///       and not composable
///
/// @since 1.0
package d021248.mytry.core.lang.fp.cps;
