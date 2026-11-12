/// Anti-pattern: naive direct recursion that overflows the JVM stack for large
/// inputs, because the JVM does not perform tail-call optimisation (TCO).
///
/// @since 1.0
package d021248.mytry.core.lang.fp.trampoline.broken;
