package d021248.mytry.core.lang.fp.functions.decorators.broken;

import java.util.HashMap;
import java.util.function.Function;

/// Demonstrates the `ConcurrentModificationException` thrown by reentrant
/// `HashMap.computeIfAbsent` when used for recursive memoization.
///
/// The canonical memoize pattern works for non-recursive functions:
///
/// ```java
/// cache.computeIfAbsent(input, f)   // fine when f never calls computeIfAbsent on cache
/// ```
///
/// It breaks when `f` recurses through the same memoized function, re-entering
/// `computeIfAbsent` while the outer call still holds the modification guard:
///
/// ```
/// computeIfAbsent(10, fib)   ← outer: sets guard
///   fib(10) → computeIfAbsent(9, fib)  ← re-enters: guard fires → CME
/// ```
public final class RecursiveMemoizeDemo {

  @SuppressWarnings("unchecked")
  public static void main() {

    // ── Broken: recursive memoize via computeIfAbsent ────────────────────

    System.out.println("=== Broken: computeIfAbsent + recursion ===");

    var cache = new HashMap<Integer, Integer>();
    Function<Integer, Integer>[] fib = new Function[1];
    fib[0] =
        n ->
            n <= 1
                ? n
                : cache.computeIfAbsent(n - 1, fib[0]) // re-enters cache → CME
                    + cache.computeIfAbsent(n - 2, fib[0]);

    try {
      cache.computeIfAbsent(10, fib[0]);
    } catch (Exception e) {
      System.out.println("  " + e.getClass().getSimpleName());
      System.out.println("  computeIfAbsent re-entered the same HashMap during computation.");
    }

    // ── Fix: compute first, then put ─────────────────────────────────────

    System.out.println("\n=== Fix: compute value, then put ===");

    var safeCache = new HashMap<Integer, Integer>();
    Function<Integer, Integer>[] safeFib = new Function[1];
    safeFib[0] =
        n -> {
          if (safeCache.containsKey(n)) return safeCache.get(n);
          var result = n <= 1 ? n : safeFib[0].apply(n - 1) + safeFib[0].apply(n - 2);
          safeCache.put(n, result); // put after computation — no re-entry
          return result;
        };

    System.out.println("  fib(10) = " + safeFib[0].apply(10));
    System.out.println("  fib(20) = " + safeFib[0].apply(20));
  }
}
