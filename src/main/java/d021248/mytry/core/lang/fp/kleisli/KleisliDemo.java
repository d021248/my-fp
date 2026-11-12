package d021248.mytry.core.lang.fp.kleisli;

import module java.base;

/// Demonstrates {@link Kleisli} arrows for safe mathematical functions.
///
/// Each function is a partial function that may fail (negative square root,
/// division by zero, log of non-positive).  Kleisli composition (`andThen`)
/// threads the `Optional` automatically.
public final class KleisliDemo {

  // Safe sqrt: fails for negative input
  static final Kleisli<Double, Double> safeSqrt =
      Kleisli.of(n -> n < 0 ? Optional.empty() : Optional.of(Math.sqrt(n)));

  // Safe reciprocal: fails for zero
  static final Kleisli<Double, Double> safeReciprocal =
      Kleisli.of(n -> n == 0.0 ? Optional.empty() : Optional.of(1.0 / n));

  // Safe log: fails for non-positive input
  static final Kleisli<Double, Double> safeLog =
      Kleisli.of(n -> n <= 0 ? Optional.empty() : Optional.of(Math.log(n)));

  public static void main() {
    System.out.println("=== Kleisli: composable partial functions ===");

    // Compose: sqrt >=> reciprocal
    var sqrtThenReciprocal = safeSqrt.andThen(safeReciprocal);
    System.out.println("\nsqrt >=> reciprocal:");
    System.out.println("  4.0  -> " + sqrtThenReciprocal.run(4.0)); // sqrt(4)=2, 1/2=0.5
    System.out.println("  0.0  -> " + sqrtThenReciprocal.run(0.0)); // sqrt(0)=0, 1/0=empty
    System.out.println("  -1.0 -> " + sqrtThenReciprocal.run(-1.0)); // sqrt(-1)=empty

    // Compose three functions: sqrt >=> log >=> reciprocal
    var pipeline = safeSqrt.andThen(safeLog).andThen(safeReciprocal);
    System.out.println("\nsqrt >=> log >=> reciprocal:");
    System.out.println("  Math.E^2 -> " + pipeline.run(Math.E * Math.E));
    System.out.println("  1.0      -> " + pipeline.run(1.0)); // sqrt(1)=1, log(1)=0, 1/0=empty

    // Monad laws as category laws
    System.out.println("\n=== Kleisli category laws ===");
    double x = 9.0;
    // Left identity: identity >=> f == f
    System.out.println(
        "Left identity same result: "
            + Kleisli.<Double>identity().andThen(safeSqrt).run(x).equals(safeSqrt.run(x)));
    // Right identity: f >=> identity == f
    System.out.println(
        "Right identity same result: "
            + safeSqrt.andThen(Kleisli.identity()).run(x).equals(safeSqrt.run(x)));
    // Associativity: (f >=> g) >=> h == f >=> (g >=> h)
    var left = (safeSqrt.andThen(safeLog)).andThen(safeReciprocal);
    var right = safeSqrt.andThen(safeLog.andThen(safeReciprocal));
    System.out.println("Associativity same result: " + left.run(x).equals(right.run(x)));
  }
}
