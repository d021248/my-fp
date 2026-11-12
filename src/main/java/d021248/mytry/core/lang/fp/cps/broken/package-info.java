/// ANTI-PATTERN: exceptions as non-local exits from a pipeline.
///
/// Throwing a checked or runtime exception to "break out" of a multi-step
/// computation is expensive (JVM stack-unwind) and opaque — callers cannot
/// distinguish an abort carrying partial results from a real failure.
///
/// The correct approach is in {@link d021248.mytry.core.lang.fp.cps}.
///
/// @since 1.0
package d021248.mytry.core.lang.fp.cps.broken;
