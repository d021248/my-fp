package d021248.mytry.core.lang.fp.optics;

import java.util.List;
import java.util.Optional;

/// Demonstrates `Prism` and `Traversal` working together with the existing `Lens`.
///
/// The domain: a simple expression language
/// `Expr = Num(int) | Add(Expr, Expr) | Mul(Expr, Expr)`.
/// The optics let us focus into specific variants and transform collections
/// without manual pattern matching.
public final class OpticsDemo {

  // ── Domain ────────────────────────────────────────────────────────────────

  sealed interface Expr permits Expr.Num, Expr.Add, Expr.Mul {
    record Num(int value) implements Expr {}

    record Add(Expr left, Expr right) implements Expr {}

    record Mul(Expr left, Expr right) implements Expr {}
  }

  // ── Prisms ────────────────────────────────────────────────────────────────

  static final Prism<Expr, Integer> numPrism =
      Prism.of(
          e -> e instanceof Expr.Num n ? Optional.of(n.value()) : Optional.empty(), Expr.Num::new);

  static final Prism<Expr, Expr.Add> addPrism =
      Prism.of(e -> e instanceof Expr.Add a ? Optional.of(a) : Optional.empty(), a -> a);

  // ── Traversal over list elements ──────────────────────────────────────────

  static final Traversal<List<Expr>, Expr> exprListTraversal = Traversal.forList();

  // ── Lens for record field ─────────────────────────────────────────────────

  static final Lens<Expr.Num, Integer> numValueLens =
      Lens.of(Expr.Num::value, (num, v) -> new Expr.Num(v));

  // ── Main ──────────────────────────────────────────────────────────────────

  public static void main() {
    Expr num3 = new Expr.Num(3);
    Expr num5 = new Expr.Num(5);
    Expr addEx = new Expr.Add(num3, num5);
    Expr mulEx = new Expr.Mul(num3, num5);

    // ── Prism: extract ────────────────────────────────────────────────────
    System.out.println("=== Prism: getOption ===");
    System.out.println("numPrism on Num(3) : " + numPrism.getOption(num3)); // Optional[3]
    System.out.println("numPrism on Add    : " + numPrism.getOption(addEx)); // Optional.empty
    System.out.println("matches Add        : " + addPrism.matches(addEx)); // true

    // ── Prism: modify ─────────────────────────────────────────────────────
    System.out.println("\n=== Prism: modify ===");
    var doubled = numPrism.modify(num3, v -> v * 2);
    System.out.println("Num(3) doubled     : " + doubled); // Num[value=6]
    var addUntouched = numPrism.modify(addEx, v -> v * 2);
    System.out.println("Add untouched      : " + addUntouched.equals(addEx)); // true

    // ── Prism: review ─────────────────────────────────────────────────────
    System.out.println("\n=== Prism: review (constructor direction) ===");
    System.out.println("review(42)         : " + numPrism.review(42)); // Num[value=42]

    // ── Traversal over a list ─────────────────────────────────────────────
    System.out.println("\n=== Traversal over list ===");
    var exprs = List.of(num3, num5, addEx, mulEx);
    System.out.println("all exprs          : " + exprs);
    // double every Num inside the list via prism + traversal
    var modified = exprListTraversal.modify(exprs, e -> numPrism.modify(e, v -> v * 2));
    System.out.println("nums doubled       : " + modified);

    // ── Lens + Prism composition (Optional-like) ──────────────────────────
    System.out.println("\n=== Lens on Num record ===");
    System.out.println("numValueLens get   : " + numValueLens.get(new Expr.Num(7))); // 7
    System.out.println("numValueLens set   : " + numValueLens.set(new Expr.Num(7), 99)); // Num[99]
  }
}
