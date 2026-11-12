package d021248.mytry.core.lang.fp.lazy;

import java.util.concurrent.atomic.AtomicInteger;

/// Demonstrates {@link Lazy} evaluation, memoization, and thread safety.
public final class LazyDemo {

  public static void main() throws InterruptedException {

    System.out.println("=== Basic deferred evaluation ===");
    var calls = new AtomicInteger();
    var lazy =
        Lazy.of(
            () -> {
              System.out.println("  [supplier called]");
              calls.incrementAndGet();
              return "hello from supplier";
            });
    System.out.println("  isEvaluated before get(): " + lazy.isEvaluated());
    System.out.println("  get() #1: " + lazy.get());
    System.out.println("  get() #2: " + lazy.get());
    System.out.println("  supplier invocations: " + calls.get());
    System.out.println("  isEvaluated after get(): " + lazy.isEvaluated());

    System.out.println();
    System.out.println("=== Eager variant already evaluated ===");
    var eager = Lazy.eager(42);
    System.out.println("  isEvaluated: " + eager.isEvaluated());
    System.out.println("  get():       " + eager.get());

    System.out.println();
    System.out.println("=== map: deferred transformation ===");
    var base = Lazy.of(() -> "  base value");
    var mapped = base.map(String::toUpperCase);
    System.out.println("  base evaluated:   " + base.isEvaluated());
    System.out.println("  mapped evaluated: " + mapped.isEvaluated());
    System.out.println("  mapped.get():     " + mapped.get());
    System.out.println("  base evaluated after mapped.get(): " + base.isEvaluated());

    System.out.println();
    System.out.println("=== Thread safety: 50 threads, supplier called exactly once ===");
    var sharedCalls = new AtomicInteger();
    var shared =
        Lazy.of(
            () -> {
              sharedCalls.incrementAndGet();
              return "shared";
            });
    var threads = new Thread[50];
    for (var i = 0; i < threads.length; i++) {
      threads[i] = Thread.ofVirtual().start(shared::get);
    }
    for (var t : threads) t.join();
    System.out.println("  supplier invocations: " + sharedCalls.get() + " (expected 1)");
    System.out.println("  toString: " + shared);

    System.out.println();
    System.out.println("=== Null result is cached correctly ===");
    var nullCalls = new AtomicInteger();
    var nullLazy =
        Lazy.<String>of(
            () -> {
              nullCalls.incrementAndGet();
              return null;
            });
    System.out.println("  get() #1: " + nullLazy.get());
    System.out.println("  get() #2: " + nullLazy.get());
    System.out.println("  supplier invocations: " + nullCalls.get() + " (expected 1)");
  }
}
