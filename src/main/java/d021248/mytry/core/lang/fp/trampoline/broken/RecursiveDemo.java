package d021248.mytry.core.lang.fp.trampoline.broken;

/// ANTI-PATTERN: naive recursion in Java.
///
/// Functional languages (Haskell, Scala, Erlang, F#) perform
/// *tail-call optimisation* (TCO): a tail-recursive call reuses the
/// current stack frame instead of allocating a new one. The recursion runs in
/// O(1) stack space regardless of depth.
///
/// The JVM does not perform TCO. Every recursive call pushes a new frame.
/// Deep recursion therefore throws {@link StackOverflowError}.
///
/// The methods below work for small inputs but crash for large ones.
/// Run {@link #main} to observe the exact depth at which the JVM gives up.
public final class RecursiveDemo {

  /// Tail-recursive in spirit — but the JVM keeps every frame.
  static long factorial(int n, long acc) {
    if (n <= 1) return acc;
    return factorial(n - 1, (long) n * acc); // ← NOT a tail call to the JVM
  }

  static long fibonacci(int n) {
    if (n <= 1) return n;
    return fibonacci(n - 1) + fibonacci(n - 2); // exponential AND deep stack
  }

  /// Count how deep we can go before StackOverflow.
  static int stackDepth(int depth) {
    return stackDepth(depth + 1); // pure tail call — still crashes
  }

  public static void main() {
    System.out.println("factorial(10)  = " + factorial(10, 1));
    System.out.println("fibonacci(20)  = " + fibonacci(20));

    System.out.println("\nHow deep can we go?");
    try {
      stackDepth(0);
    } catch (StackOverflowError e) {
      System.out.println("StackOverflowError — the JVM gave up.");
    }

    System.out.println("\nNow let's try a deep factorial:");
    try {
      System.out.println(factorial(100_000, 1));
    } catch (StackOverflowError e) {
      System.out.println("StackOverflowError at factorial(100_000)");
    }
  }
}
