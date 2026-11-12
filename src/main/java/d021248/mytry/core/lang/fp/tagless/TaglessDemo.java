package d021248.mytry.core.lang.fp.tagless;

/// Demonstrates tagless-final by running the same expression through two interpreters.
///
/// The expression `2 + 3 * 4` is built once as a sequence of algebra calls;
/// the same sequence is reused for evaluation and pretty-printing without any
/// changes to the expression itself.
public final class TaglessDemo {

  /// Builds `2 + 3 * 4` using the given interpreter.
  static <F> F buildExpr(Expr<F> e) {
    return e.add(e.lit(2), e.mul(e.lit(3), e.lit(4)));
  }

  public static void main() {
    var eval = new EvalInterpreter();
    var print = new PrettyPrinter();

    System.out.println("Evaluated : %d".formatted(buildExpr(eval)));
    System.out.println("Pretty    : %s".formatted(buildExpr(print)));
  }
}
