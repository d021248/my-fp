package d021248.mytry.core.lang.fp.free;

import module java.base;

/// Demonstrates the Free monad pattern with three different interpreters.
///
/// The same `program` value is run three times:
/// 1. **Map interpreter** — real mutations on a `HashMap`
/// 2. **Logging interpreter** — records every operation, then executes
/// 3. **Dry-run interpreter** — reads a pre-populated store; writes are no-ops
public final class FreeDemo {

  /// A program that stores a language name, reads it back,
  /// conditionally upgrades it, then returns the final value.
  static Free<Optional<String>> buildProgram() {
    return Free.put("lang", "Java")
        .andThen(Free.put("version", "25"))
        .andThen(
            Free.get("lang")
                .flatMap(
                    maybeLang ->
                        maybeLang
                            .map(lang -> Free.put("lang", lang + " 25").andThen(Free.get("lang")))
                            .orElseGet(() -> Free.get("lang"))));
  }

  public static void main() {
    var program = buildProgram();

    // ── 1. Map interpreter ────────────────────────────────────────────────
    System.out.println("=== 1. Map interpreter ===");
    var store1 = new HashMap<String, String>();
    var result1 = Free.run(program, Free.mapInterpreter(store1));
    System.out.println("result : " + result1);
    System.out.println("store  : " + store1);

    // ── 2. Logging interpreter ────────────────────────────────────────────
    System.out.println("\n=== 2. Logging interpreter ===");
    var store2 = new HashMap<String, String>();
    var log = new ArrayList<String>();
    var result2 = Free.run(program, Free.loggingInterpreter(Free.mapInterpreter(store2), log));
    System.out.println("result : " + result2);
    System.out.println("ops    : " + log);

    // ── 3. Dry-run interpreter ────────────────────────────────────────────
    System.out.println("\n=== 3. Dry-run interpreter (reads only) ===");
    var snapshot = Map.of("lang", "Kotlin"); // pre-populated — writes ignored
    var result3 = Free.run(program, Free.dryRunInterpreter(snapshot));
    System.out.println("result       : " + result3);
    System.out.println("store intact : " + snapshot);
  }
}
