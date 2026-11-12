package d021248.mytry.core.lang.fp.recursionschemes.broken;

/// Demonstrates explicit recursive functions on a tree without recursion schemes.
///
/// Every operation requires its own recursive traversal. The structural pattern
/// (visit children, combine results) is duplicated across `eval`, `prettyPrint`,
/// and `depth` — only the per-node logic differs.
///
/// Compare with {@link d021248.mytry.core.lang.fp.recursionschemes.RecursionSchemes}
/// which factors the traversal into a single `cata` call, letting each operation
/// supply only its algebra.
public final class ExplicitRecursion {

  sealed interface Expr {
    record Num(int value) implements Expr {}

    record Add(Expr left, Expr right) implements Expr {}

    record Mul(Expr left, Expr right) implements Expr {}
  }

  // ── Each operation repeats the traversal ──────────────────────────────────

  static int eval(Expr e) {
    return switch (e) {
      case Expr.Num n -> n.value();
      case Expr.Add a -> eval(a.left()) + eval(a.right()); // explicit recursion
      case Expr.Mul m -> eval(m.left()) * eval(m.right()); // explicit recursion
    };
  }

  static String prettyPrint(Expr e) {
    return switch (e) {
      case Expr.Num n -> String.valueOf(n.value());
      case Expr.Add a ->
          "(%s + %s)".formatted(prettyPrint(a.left()), prettyPrint(a.right())); // again
      case Expr.Mul m ->
          "(%s * %s)".formatted(prettyPrint(m.left()), prettyPrint(m.right())); // again
    };
  }

  static int depth(Expr e) {
    return switch (e) {
      case Expr.Num _ -> 0;
      case Expr.Add a -> 1 + Math.max(depth(a.left()), depth(a.right())); // again
      case Expr.Mul m -> 1 + Math.max(depth(m.left()), depth(m.right())); // again
    };
  }

  public static void main() {
    // (2 * 3) + 4
    var expr = new Expr.Add(new Expr.Mul(new Expr.Num(2), new Expr.Num(3)), new Expr.Num(4));

    System.out.println("=== Explicit Recursion ===");
    System.out.println("Expression : " + prettyPrint(expr));
    System.out.println("Evaluated  : " + eval(expr));
    System.out.println("Depth      : " + depth(expr));
    System.out.println();
    System.out.println("Each operation repeats the same traversal.");
    System.out.println("RecursionSchemes.cata() captures that traversal once.");
  }
}
