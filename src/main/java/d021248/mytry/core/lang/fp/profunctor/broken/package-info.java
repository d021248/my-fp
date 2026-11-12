/// Comparison: manually composed getter/setter pairs without profunctor encoding.
///
/// Without profunctors, lens composition requires writing a new getter and setter
/// for every composed path.  The composition does not arise from the types.
///
/// Compare with `StrongFn` where optic composition IS function composition
/// of profunctor transformers.
///
/// @since 1.0
package d021248.mytry.core.lang.fp.profunctor.broken;
