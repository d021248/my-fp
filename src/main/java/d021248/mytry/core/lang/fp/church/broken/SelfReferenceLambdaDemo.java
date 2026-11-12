package d021248.mytry.core.lang.fp.church.broken;

/// # Self-Referencing Lambda — Anti-Pattern
///
/// In Java, a lambda cannot refer to itself by name. The `var` declaration is
/// not
/// "definitely assigned" when the lambda body is compiled, so the compiler
/// rejects it.
///
/// Both forms shown below fail with "variable X might not have been
/// initialized".
/// The fix is `Church.Y(...)`, which encodes the fixed-point without any
/// self-reference.
///
/// @see d021248.mytry.core.lang.fp.church.Church#Y
public final class SelfReferenceLambdaDemo {

  public static void main() {
    // DOES NOT COMPILE — var-based self-reference:
    // var factorial = (java.util.function.Function<Integer, Integer>)
    // (n -> n <= 1 ? 1 : n * factorial.apply(n - 1));
    // Error: variable factorial might not have been initialized

    // DOES NOT COMPILE — explicit type self-reference:
    // java.util.function.Function<Integer, Integer> fact =
    // n -> n <= 1 ? 1 : n * fact.apply(n - 1);
    // Error: variable fact might not have been initialized

    System.out.println("Uncomment the lines above to see the compile errors.");
    System.out.println("Use Church.Y() to write recursive lambdas without self-reference.");
  }
}
