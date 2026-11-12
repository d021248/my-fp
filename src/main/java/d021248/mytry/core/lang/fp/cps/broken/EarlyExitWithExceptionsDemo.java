package d021248.mytry.core.lang.fp.cps.broken;

import java.util.List;

/// ANTI-PATTERN: using exceptions for early exit from a multi-step pipeline.
///
/// Throwing an exception to "break out" of a computation carries three costs:
///
///  1. **Performance** — the JVM captures a full stack trace; this is 10–100×
///     more expensive than a normal return.
///  2. **Type opacity** — the caller sees `RuntimeException`; the partial
///     result carried inside is invisible to the type system.
///  3. **Non-composability** — two pipelines that use different exception types
///     for early exit cannot be combined without nested try/catch.
///
/// See {@link d021248.mytry.core.lang.fp.cps.CpsDemo} for the correct approach.
public final class EarlyExitWithExceptionsDemo {

  // ── Helper exception carrying the partial result ───────────────────────────

  static class EarlyResult extends RuntimeException {
    private static final long serialVersionUID = 1L;

    final int value;

    EarlyResult(int value) {
      // Suppress stack-trace capture for performance — but this is still
      // a smell: we are abusing exception machinery for normal control flow
      super("early result", null, true, false);
      this.value = value;
    }
  }

  // ── Sum until negative, abort with accumulated total ──────────────────────

  /// Returns the sum of `numbers` up to (but not including) the first negative.
  /// Uses an exception as a non-local goto — expensive and opaque.
  static int sumUntilNegative(List<Integer> numbers) {
    try {
      var acc = new int[] {0};
      numbers.forEach(
          n -> {
            if (n < 0) throw new EarlyResult(acc[0]); // ← exception as break
            acc[0] += n;
          });
      return acc[0];
    } catch (EarlyResult e) {
      return e.value;
    }
  }

  // ── Nested pipelines: two early-exit points, two catch blocks ─────────────

  record Stage(String name, int limit) {}

  /// Processes stages, aborting the outer loop when a stage limit is exceeded.
  /// The outer loop needs its own exception to break out — nesting two such
  /// patterns quickly produces unreadable code.
  static String processPipeline(List<Stage> stages, List<Integer> inputs) {
    class OuterAbort extends RuntimeException {
      private static final long serialVersionUID = 1L;
      final String message;

      OuterAbort(String msg) {
        super(msg, null, true, false);
        message = msg;
      }
    }
    try {
      for (var stage : stages) {
        try {
          var sum = new int[] {0};
          inputs.forEach(
              n -> {
                sum[0] += n;
                if (sum[0] > stage.limit()) throw new EarlyResult(sum[0]);
              });
          System.out.println("  Stage " + stage.name() + ": completed, sum=" + sum[0]);
        } catch (EarlyResult e) {
          System.out.println("  Stage " + stage.name() + ": limit exceeded at " + e.value);
          if (stage.name().equals("critical")) {
            throw new OuterAbort("critical stage failed at " + e.value);
          }
        }
      }
      return "all stages completed";
    } catch (OuterAbort e) {
      return "aborted: " + e.message;
    }
  }

  // ── Main ──────────────────────────────────────────────────────────────────

  public static void main() {
    System.out.println("=== Early exit via exceptions (anti-pattern) ===\n");

    var numbers = List.of(1, 2, 3, -1, 4, 5);
    System.out.println("sumUntilNegative: " + sumUntilNegative(numbers));

    System.out.println("\nPipeline:");
    var stages = List.of(new Stage("warmup", 5), new Stage("critical", 3), new Stage("final", 100));
    var inputs = List.of(2, 2, 2, 2);
    System.out.println("result: " + processPipeline(stages, inputs));

    System.out.println("\nSee cps/CpsDemo for the correct approach.");
  }
}
