/// Comparison: manually chained partial functions with explicit empty checks.
///
/// Without Kleisli arrows, composing partial functions requires writing a
/// new method for every combination, with explicit `isEmpty` checks at each
/// step.  The composition pattern is repeated rather than abstracted.
///
/// Compare with {@link d021248.mytry.core.lang.fp.kleisli.Kleisli} which
/// provides `andThen` as a single reusable composition operator.
///
/// @since 1.0
package d021248.mytry.core.lang.fp.kleisli.broken;
