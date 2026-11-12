package d021248.mytry.core.lang.fp.tagless;

/// Evaluating interpreter for the {@link Expr} algebra.
///
/// Implements `Expr<Integer>`: every expression node is evaluated directly
/// to its integer value.
public final class EvalInterpreter implements Expr<Integer> {

  @Override
  public Integer lit(int n) {
    return n;
  }

  @Override
  public Integer add(Integer left, Integer right) {
    return left + right;
  }

  @Override
  public Integer mul(Integer left, Integer right) {
    return left * right;
  }

  @Override
  public Integer neg(Integer value) {
    return -value;
  }
}
