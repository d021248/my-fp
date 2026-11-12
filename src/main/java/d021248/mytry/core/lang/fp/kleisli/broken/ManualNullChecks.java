package d021248.mytry.core.lang.fp.kleisli.broken;

import module java.base;

/// Manual chaining of partial functions — without Kleisli arrows.
///
/// Each step checks for empty or failure explicitly.  The composition logic
/// is entangled with the business logic, and there is no reusable abstraction.
///
/// Compare with {@link d021248.mytry.core.lang.fp.kleisli.Kleisli} where
/// composition is expressed once as `andThen` and reused across pipelines.
public final class ManualNullChecks {

  private ManualNullChecks() {}

  static Optional<Double> safeSqrt(double n) {
    return n < 0 ? Optional.empty() : Optional.of(Math.sqrt(n));
  }

  static Optional<Double> safeReciprocal(double n) {
    return n == 0.0 ? Optional.empty() : Optional.of(1.0 / n);
  }

  static Optional<Double> safeLog(double n) {
    return n <= 0 ? Optional.empty() : Optional.of(Math.log(n));
  }

  /// Manual pipeline: must call each step with explicit null/empty checks.
  ///
  /// Every composition is ad hoc — there is no reusable `andThen` abstraction.
  static Optional<Double> sqrtThenReciprocal(double x) {
    var r1 = safeSqrt(x);
    if (r1.isEmpty()) return Optional.empty();
    return safeReciprocal(r1.get()); // explicit get() + re-wrap
  }

  public static void main() {
    System.out.println("=== Manual chaining (broken) ===");
    System.out.println(sqrtThenReciprocal(4.0)); // Optional[0.5]
    System.out.println(sqrtThenReciprocal(-1.0)); // Optional.empty
    System.out.println(
        "Problem: every composition requires a new method with explicit isEmpty checks.");
  }
}
