package d021248.mytry.core.lang.fp.tagless;

/// Pretty-printing interpreter for the {@link Expr} algebra.
///
/// Implements `Expr<String>`: every expression node is rendered as a
/// parenthesised string, e.g. `add(lit(2), mul(lit(3), lit(4)))` becomes
/// `"(2 + (3 * 4))"`.
public final class PrettyPrinter implements Expr<String> {

  @Override
  public String lit(int n) {
    return String.valueOf(n);
  }

  @Override
  public String add(String left, String right) {
    return "(%s + %s)".formatted(left, right);
  }

  @Override
  public String mul(String left, String right) {
    return "(%s * %s)".formatted(left, right);
  }

  @Override
  public String neg(String value) {
    return "(-%s)".formatted(value);
  }
}
