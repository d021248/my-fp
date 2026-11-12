package d021248.mytry.core.lang.fp.cps;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/// Demonstrates {@link Cont} — CPS with type-safe early exit via `callcc`.
///
/// Three scenarios contrasted with the exception-based approach in
/// {@link d021248.mytry.core.lang.fp.cps.broken.EarlyExitWithExceptionsDemo}:
///
/// 1. **Sum-until-negative** — `callcc` exits early without throwing
/// 2. **Pipeline stages** — composed `Cont` values with a shared abort
/// 3. **Pure pipeline** — `map`/`flatMap` without any early exit
public final class CpsDemo {

  // ── 1. Sum-until-negative via callcc ─────────────────────────────────────

  /// Returns the sum of `numbers` up to (but not including) the first negative.
  /// `callcc` captures the "return from this method" continuation; calling it
  /// with the accumulated value exits immediately — no exception, no flag.
  static int sumUntilNegative(List<Integer> numbers) {
    Cont<Integer, Integer> computation =
        Cont.callcc(
            escape -> {
              var acc = new int[] {0};
              for (int n : numbers) {
                if (n < 0) return escape.apply(acc[0]); // early exit — no exception
                acc[0] += n;
              }
              return Cont.pure(acc[0]);
            });
    return Cont.eval(computation);
  }

  // ── 2. Pipeline: map and flatMap ──────────────────────────────────────────

  /// A three-step pipeline: parse → validate → format.
  /// `callcc` wraps the whole pipeline so `escape` bypasses all remaining steps.
  static String process(String raw) {
    // 1. Define local helper functions
    Function<String, Optional<String>> tryParse =
        s -> {
          try {
            int value = Integer.parseInt(s.trim());
            return Optional.of(String.valueOf(value));
          } catch (NumberFormatException _) {
            return Optional.empty();
          }
        };

    Predicate<String> isPositive = s -> Integer.parseInt(s) >= 0;

    // 2. The actual pipeline with explicit type witnesses <String>
    Cont<String, String> pipeline =
        Cont.callcc(
            escape ->
                Cont.<String, String>pure(raw)
                    .flatMap(
                        s ->
                            tryParse
                                .apply(s)
                                .map(Cont::<String, String>pure)
                                .orElseGet(() -> escape.apply("error: not a number: " + s)))
                    .flatMap(
                        s ->
                            isPositive.test(s)
                                ? Cont.pure(s)
                                : escape.apply("error: negative value: " + s))
                    .map(s -> "result: " + s));

    return Cont.eval(pipeline);
  }

  // ── 3. Pure transformation chain ─────────────────────────────────────────
  static String transform(int n) {
    return Cont.eval(
        Cont.<Integer, String>pure(n)
            .map(x -> x * 2)
            .map(x -> x + 10)
            .map("value=%d"::formatted)
            .map(String::toUpperCase));
  }

  // ── Main ──────────────────────────────────────────────────────────────────

  public static void main() {
    System.out.println("=== CPS with callcc ===\n");

    // 1. Early exit
    System.out.println(
        "sumUntilNegative([1,2,3,-1,4,5]): " + sumUntilNegative(List.of(1, 2, 3, -1, 4, 5)));
    System.out.println("sumUntilNegative([1,2,3]):        " + sumUntilNegative(List.of(1, 2, 3)));

    // 2. Pipeline with error short-circuit
    System.out.println("\nprocess(\"42\"):  " + process("42"));
    System.out.println("process(\"abc\"):   " + process("abc"));
    System.out.println("process(\"-5\"):    " + process("-5"));

    // 3. Pure chain
    System.out.println("\ntransform(5): " + transform(5));
  }
}
