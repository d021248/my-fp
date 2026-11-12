package d021248.mytry.core.lang.fp.trampoline;

import java.math.BigInteger;

/// Demonstrates {@link Bounce} for `BigInteger` factorial — the same
/// trampoline pattern as {@link TrampolineDemo} but with arbitrary-precision
/// arithmetic, computing values up to 1 000 000! without stack overflow.
public final class TrampolineDemo2 {

  // -------------------------------------------------------------------------
  // Factorial — tail-recursive via accumulator
  // -------------------------------------------------------------------------

  static Bounce<BigInteger> factStep(BigInteger n, BigInteger acc) {
    // Base case: n <= 1
    if (n.compareTo(BigInteger.ONE) <= 0) {
      return Bounce.done(acc);
    }
    // Recursive step: n - 1 and n * acc
    return Bounce.more(() -> factStep(n.subtract(BigInteger.ONE), n.multiply(acc)));
  }

  public static BigInteger factorial(int n) {
    if (n < 0) throw new IllegalArgumentException("value must not be negative");

    // Start the trampoline loop with n and accumulator 1
    return Bounce.run(factStep(BigInteger.valueOf(n), BigInteger.ONE));
  }

  // -------------------------------------------------------------------------
  // Demo
  // -------------------------------------------------------------------------

  public static void main() {
    System.out.println("=== Factorial Trampoline Loop ===");

    // Loop from 10 to 1 000 000 (each step multiplies by 10)
    for (int n = 10; n <= 1_000_000; n = (n == 10) ? 20 : (n == 20) ? 100 : n * 10) {
      BigInteger res = factorial(n);

      // How many digits does the result have?
      int digits = res.toString().length();

      if (n <= 20) {
        // Print small values in full
        System.out.printf("%-6d! = %s (%d digits)%n", n, res, digits);
      } else {
        // Large values would flood the console, so just print a status line
        System.out.printf(
            "%-6d! = [computed successfully] -> %d digits ✓ (no StackOverflow)%n", n, digits);
      }
    }
  }
}
