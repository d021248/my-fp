/// Comparison: manual state threading without the State monad.
///
/// Functions that read and update state must pass it explicitly as a parameter
/// and return an updated copy alongside their result. The threading logic is
/// always visible, coupling unrelated concerns.
///
/// The parent package's `State`, `Reader`, and `Writer` monads replace this
/// pattern with a `flatMap`-based pipeline.
///
/// @since 1.0
package d021248.mytry.core.lang.fp.monads.broken;
