package d021248.mytry.core.lang.fp.tagless;

/// Tagless-final algebra for a simple arithmetic expression DSL.
///
/// Each method corresponds to one constructor of the expression language.
/// An **interpreter** is an implementation of this interface for a particular
/// carrier type `F`:
///
/// - `F = Integer` → evaluation
/// - `F = String`  → pretty-printing
///
/// New interpreters can be added without changing existing code.
///
/// @param <F> the carrier type chosen by each interpreter
public interface Expr<F> {

  /// A literal integer constant.
  F lit(int n);

  /// Addition of two sub-expressions.
  F add(F left, F right);

  /// Multiplication of two sub-expressions.
  F mul(F left, F right);

  /// Arithmetic negation of a sub-expression.
  F neg(F value);
}
