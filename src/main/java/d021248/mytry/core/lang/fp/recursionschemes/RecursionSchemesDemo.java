package d021248.mytry.core.lang.fp.recursionschemes;

import d021248.mytry.core.lang.fp.recursionschemes.RecursionSchemes.ExprF;
import d021248.mytry.core.lang.fp.recursionschemes.RecursionSchemes.Fix;

/// Demonstrates catamorphism, anamorphism, and hylomorphism on arithmetic
/// expression trees.
///
/// Three algebras and two coalgebras show how the same recursive data structure
/// can be processed (or built) without writing explicit recursive functions.
public final class RecursionSchemesDemo {

  // ─── Algebras (for cata) ───────────────────────────────────────────────

  static int eval(ExprF<Integer> e) {
    return switch (e) {
      case ExprF.Num<Integer> n -> n.value();
      case ExprF.Add<Integer> a -> a.left() + a.right();
      case ExprF.Mul<Integer> m -> m.left() * m.right();
    };
  }

  static String prettyPrint(ExprF<String> e) {
    return switch (e) {
      case ExprF.Num<String> n -> String.valueOf(n.value());
      case ExprF.Add<String> a -> "(%s + %s)".formatted(a.left(), a.right());
      case ExprF.Mul<String> m -> "(%s * %s)".formatted(m.left(), m.right());
    };
  }

  static int depth(ExprF<Integer> e) {
    return switch (e) {
      case ExprF.Num<Integer> _ -> 0;
      case ExprF.Add<Integer> a -> 1 + Math.max(a.left(), a.right());
      case ExprF.Mul<Integer> m -> 1 + Math.max(m.left(), m.right());
    };
  }

  // ─── Coalgebras (for ana / hylo) ───────────────────────────────────────

  // Fibonacci coalgebra: fib(n) = fib(n-1) + fib(n-2).
  // Both seeds strictly decrease, so this terminates for all n >= 1.
  static ExprF<Integer> fibCoalgebra(int n) {
    if (n <= 2) return new ExprF.Num<>(1);
    return new ExprF.Add<>(n - 1, n - 2);
  }

  // ─── Main ──────────────────────────────────────────────────────────────

  public static void main() {
    // Tree: (2 * 3) + 4
    var expr = Fix.add(Fix.mul(Fix.num(2), Fix.num(3)), Fix.num(4));

    System.out.println("=== Catamorphism (fold) ===");
    System.out.println(
        "Expression : " + RecursionSchemes.cata(RecursionSchemesDemo::prettyPrint, expr));
    System.out.println("Evaluated  : " + RecursionSchemes.cata(RecursionSchemesDemo::eval, expr));
    System.out.println("Depth      : " + RecursionSchemes.cata(RecursionSchemesDemo::depth, expr));

    System.out.println("\n=== Anamorphism (unfold) ===");
    var fib7tree = RecursionSchemes.ana(RecursionSchemesDemo::fibCoalgebra, 7);
    System.out.println(
        "fib(7) tree: " + RecursionSchemes.cata(RecursionSchemesDemo::prettyPrint, fib7tree));
    System.out.println(
        "Evaluated  : " + RecursionSchemes.cata(RecursionSchemesDemo::eval, fib7tree));

    System.out.println("\n=== Hylomorphism (unfold then fold) ===");
    System.out.println("Fibonacci numbers via hylo:");
    for (int n = 1; n <= 10; n++) {
      int fib =
          RecursionSchemes.hylo(RecursionSchemesDemo::eval, RecursionSchemesDemo::fibCoalgebra, n);
      System.out.printf("  fib(%2d) = %d%n", n, fib);
    }
  }
}
