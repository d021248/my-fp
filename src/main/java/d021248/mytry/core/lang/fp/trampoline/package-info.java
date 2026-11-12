/// Trampolining — Java's workaround for the JVM's lack of tail-call optimisation.
///
/// ## Sub-packages
///
///   - {@link d021248.mytry.core.lang.fp.trampoline.broken} — direct recursion
///       that throws {@link StackOverflowError} on large inputs
///   - {@link d021248.mytry.core.lang.fp.trampoline} — the
///       {@link d021248.mytry.core.lang.fp.trampoline.Bounce} sealed type
///       that converts recursion to iteration
///
/// @since 1.0
package d021248.mytry.core.lang.fp.trampoline;
