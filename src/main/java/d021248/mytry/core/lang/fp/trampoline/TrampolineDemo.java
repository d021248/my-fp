package d021248.mytry.core.lang.fp.trampoline;

/// Demonstrates {@link Bounce} for tail-recursive algorithms that would
/// otherwise throw {@link StackOverflowError}.
public final class TrampolineDemo {

  // -------------------------------------------------------------------------
  // Factorial — tail-recursive via accumulator
  // -------------------------------------------------------------------------

  static Bounce<Long> factStep(int n, long acc) {
    if (n <= 1) return Bounce.done(acc);
    return Bounce.more(() -> factStep(n - 1, (long) n * acc));
  }

  public static long factorial(int n) {
    return Bounce.run(factStep(n, 1L));
  }

  // -------------------------------------------------------------------------
  // isEven / isOdd — mutual recursion (impossible without a trampoline)
  // -------------------------------------------------------------------------

  static Bounce<Boolean> isEvenStep(int n) {
    if (n == 0) return Bounce.done(true);
    return Bounce.more(() -> isOddStep(n - 1));
  }

  static Bounce<Boolean> isOddStep(int n) {
    if (n == 0) return Bounce.done(false);
    return Bounce.more(() -> isEvenStep(n - 1));
  }

  public static boolean isEven(int n) {
    return Bounce.run(isEvenStep(Math.abs(n)));
  }

  // -------------------------------------------------------------------------
  // Sum 1..n — trivial example to show the pattern clearly
  // -------------------------------------------------------------------------

  static Bounce<Long> sumStep(long n, long acc) {
    if (n <= 0) return Bounce.done(acc);
    return Bounce.more(() -> sumStep(n - 1, acc + n));
  }

  public static long sum(long n) {
    return Bounce.run(sumStep(n, 0L));
  }

  // -------------------------------------------------------------------------
  // Demo
  // -------------------------------------------------------------------------

  public static void main() {
    System.out.println("=== Factorial ===");
    System.out.println("10!        = " + factorial(10));
    System.out.println("20!        = " + factorial(20));
    System.out.println(
        "100_000! overflows long but runs: "
            + (factorial(100_000) == 0 ? "result wraps" : "no StackOverflow ✓"));

    System.out.println("\n=== Mutual recursion: isEven ===");
    System.out.println("isEven(0)       = " + isEven(0));
    System.out.println("isEven(42)      = " + isEven(42));
    System.out.println("isEven(99)      = " + isEven(99));
    System.out.println("isEven(100_000) = " + isEven(100_000) + "  (no StackOverflow ✓)");

    System.out.println("\n=== Sum 1..n ===");
    System.out.println("sum(100)        = " + sum(100));
    System.out.println("sum(1_000_000)  = " + sum(1_000_000) + "  (no StackOverflow ✓)");
  }
}
