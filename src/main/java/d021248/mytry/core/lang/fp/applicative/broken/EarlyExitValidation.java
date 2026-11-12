package d021248.mytry.core.lang.fp.applicative.broken;

import module java.base;

/// Traditional early-exit validation: stops after the first error.
///
/// When name is blank AND age is negative, only the name error is reported.
/// The caller cannot see the age problem until the name is fixed — a poor
/// user experience.  Compare with {@link d021248.mytry.core.lang.fp.applicative.Validated}
/// which collects all errors in one pass.
public final class EarlyExitValidation {

  private EarlyExitValidation() {}

  /// Returns a non-empty error list on failure, or an empty list on success.
  ///
  /// Problem: returns after the FIRST failure — subsequent errors are hidden.
  public static List<String> validate(String name, int age, String email) {
    if (name == null || name.isBlank()) return List.of("name must not be blank");
    if (age < 0) return List.of("age must be non-negative, got: " + age);
    if (email == null || !email.contains("@")) return List.of("email must contain '@'");
    return List.of();
  }

  public static void main() {
    System.out.println("=== Early-exit validation (broken) ===");
    System.out.println("\n-- name blank + age negative + bad email --");
    System.out.println(validate("", -5, "bad"));
    System.out.println("Only one error reported! The other two are hidden.");
  }
}
