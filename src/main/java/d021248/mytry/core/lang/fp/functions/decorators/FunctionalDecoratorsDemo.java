package d021248.mytry.core.lang.fp.functions.decorators;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public final class FunctionalDecoratorsDemo {
  public static void main() {

    // ── memoize + withLogging ─────────────────────────────────────────────

    Function<Integer, Boolean> isPrime =
        n -> {
          if (n < 2) return false;
          for (int i = 2; i * i <= n; i++) if (n % i == 0) return false;
          return true;
        };

    // withLogging inside memoize: fires only on cache miss
    var fastIsPrime =
        FunctionalDecorators.memoize(FunctionalDecorators.withLogging("isPrime", isPrime));

    System.out.println("=== memoize + withLogging (log only on cache miss) ===");
    for (var n : List.of(97, 101, 97, 89, 97))
      System.out.println("  %d -> %s".formatted(n, fastIsPrime.apply(n)));

    // ── withTiming ────────────────────────────────────────────────────────

    System.out.println("\n=== withTiming ===");
    var timedIsPrime = FunctionalDecorators.withTiming("isPrime", isPrime);
    timedIsPrime.apply(999_983);

    // ── withRetry ─────────────────────────────────────────────────────────

    System.out.println("\n=== withRetry ===");
    var attempts = new AtomicInteger();
    Function<String, String> flaky =
        s -> {
          if (attempts.incrementAndGet() < 3) throw new RuntimeException("transient");
          return s.toUpperCase();
        };
    var resilient = FunctionalDecorators.withRetry(5, flaky);
    System.out.println(
        "  result: " + resilient.apply("hello") + " (took %d attempts)".formatted(attempts.get()));

    // ── withFallback ──────────────────────────────────────────────────────

    System.out.println("\n=== withFallback ===");
    var safe = FunctionalDecorators.<String, Integer>withFallback(-1, Integer::parseInt);
    System.out.println("  parse(\"42\")   -> " + safe.apply("42"));
    System.out.println("  parse(\"oops\") -> " + safe.apply("oops"));
  }
}
