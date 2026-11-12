package d021248.mytry.core.lang.fp.applicative;

/// Demonstrates form validation using {@link Validated}.
///
/// Three independent field validators run simultaneously; all errors are
/// accumulated in one pass rather than stopping after the first failure.
public final class ValidatedDemo {

  record FormInput(String name, int age, String email) {}

  record NameAndAge(String name, int age) {}

  static Validated<String, String> validateName(String name) {
    return (name == null || name.isBlank())
        ? Validated.invalid("name must not be blank")
        : Validated.valid(name.strip());
  }

  static Validated<String, Integer> validateAge(int age) {
    return age < 0
        ? Validated.invalid("age must be non-negative, got: " + age)
        : Validated.valid(age);
  }

  static Validated<String, String> validateEmail(String email) {
    return (email == null || !email.contains("@"))
        ? Validated.invalid("email must contain '@', got: " + email)
        : Validated.valid(email.strip());
  }

  /// Combines three validated fields into a `FormInput` using `liftA2` twice.
  static Validated<String, FormInput> validateForm(String name, int age, String email) {
    var vNameAge = Validated.liftA2(NameAndAge::new, validateName(name), validateAge(age));
    return Validated.liftA2(
        (na, e) -> new FormInput(na.name(), na.age(), e), vNameAge, validateEmail(email));
  }

  public static void main() {
    System.out.println("=== Applicative: form validation ===");

    System.out.println("\n-- All valid --");
    System.out.println(validateForm("Alice", 30, "alice@example.com"));

    System.out.println("\n-- All three invalid (ap accumulates all errors) --");
    var allBad = validateForm("", -5, "not-an-email");
    System.out.println(allBad);

    System.out.println("\n-- flatMap short-circuits at first error --");
    var result =
        validateName("")
            .flatMap(n -> validateAge(-5)) // never reached
            .flatMap(a -> validateEmail("bad")); // never reached
    System.out.println(result);
  }
}
