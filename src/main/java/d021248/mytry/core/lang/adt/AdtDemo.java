package d021248.mytry.core.lang.adt;

import module java.base;

import d021248.mytry.core.lang.adt.JsonValue.*;

/// Demonstrates algebraic data types (ADTs) in Java using sealed interfaces
/// and records — the Java equivalent of Haskell's `data` or Rust's `enum`.
///
/// ## Sections
///
/// 1. **JsonValue as recursive ADT** — build, pattern-match, pretty-print
/// 2. **Exhaustive switch** — the compiler guarantees all variants are handled
/// 3. **Nested record patterns** — destructure at arbitrary depth
/// 4. **Fold (catamorphism)** — the universal operation on any ADT
/// 5. **Either as generic sum type** — map, flatMap, fold
public final class AdtDemo {

  public static void main() {

    // =================================================================
    System.out.println("=== 1. Build a JSON value — recursive ADT ===");
    // =================================================================

    var person =
        JsonValue.obj(
            "name",
            JsonValue.of("Alice"),
            "age",
            JsonValue.of(30),
            "tags",
            JsonValue.arr(JsonValue.of("java"), JsonValue.of("fp"), JsonValue.of("adt")));

    System.out.println(person.prettyPrint());

    // =================================================================
    System.out.println("=== 2. Exhaustive switch — typeName() ===");
    // =================================================================

    var values =
        List.of(
            JsonValue.NULL,
            JsonValue.of(true),
            JsonValue.of(Math.PI),
            JsonValue.of("hello"),
            JsonValue.arr(),
            JsonValue.obj("k", JsonValue.of(1)));

    for (var v : values) {
      System.out.printf("  %-30s → type = %s%n", v, v.typeName());
    }

    // =================================================================
    System.out.println();
    System.out.println("=== 3. Nested record patterns — deep destructuring ===");
    // =================================================================

    // Extract the name from a JSON object in one switch arm:
    var greeting =
        switch (person) {
          case JObj(var fields) when fields.get("name") instanceof JStr(var name) ->
              "Hello, " + name + "!";
          default -> "Hello, stranger!";
        };
    System.out.println("  " + greeting);

    // Pattern match on array contents:
    var firstTag =
        switch (person) {
          case JObj(var f)
              when f.get("tags") instanceof JArr(var elems)
                  && !elems.isEmpty()
                  && elems.getFirst() instanceof JStr(var tag) ->
              tag;
          default -> "(none)";
        };
    System.out.println("  First tag: " + firstTag);

    // =================================================================
    System.out.println();
    System.out.println("=== 4. Fold (catamorphism) — the universal operation ===");
    // =================================================================

    // Use fold to convert JsonValue to a plain Java object (Object)
    Function<JsonValue, String> toSummary =
        v ->
            v.fold(
                () -> "<null>",
                b -> b.booleanValue() ? "YES" : "NO",
                n -> "NUM(" + n + ")",
                s -> "STR(" + s + ")",
                elems -> "ARR[" + elems.size() + "]",
                fields -> "OBJ{" + fields.size() + "}");

    for (var v : values) {
      System.out.printf("  %-30s → %s%n", v, toSummary.apply(v));
    }

    System.out.println("  Scalar count in person: " + person.scalarCount());

    // =================================================================
    System.out.println();
    System.out.println("=== 5. Either — generic sum type ===");
    // =================================================================

    Either<String, Integer> parsed = parseAge("25");
    Either<String, Integer> failed = parseAge("abc");

    System.out.println("  parse(\"25\"): " + parsed);
    System.out.println("  parse(\"abc\"): " + failed);

    // map (right-biased)
    System.out.println("  parse(\"25\").map(+1): " + parsed.map(n -> n + 1));
    System.out.println("  parse(\"abc\").map(+1): " + failed.map(n -> n + 1));

    // flatMap (chaining)
    var result =
        parseAge("25")
            .flatMap(age -> age >= 18 ? Either.right(age) : Either.left("Too young: " + age));
    System.out.println("  parse(\"25\") → check ≥18: " + result);

    var tooYoung =
        parseAge("12")
            .flatMap(age -> age >= 18 ? Either.right(age) : Either.left("Too young: " + age));
    System.out.println("  parse(\"12\") → check ≥18: " + tooYoung);

    // fold (elimination)
    var message = parsed.fold(err -> "Error: " + err, val -> "Valid age: " + val);
    System.out.println("  fold: " + message);

    // swap
    System.out.println("  swap(Left(\"err\")): " + Either.left("err").swap());

    // =================================================================
    System.out.println();
    System.out.println("=== 6. Why ADTs beat the Visitor pattern ===");
    // =================================================================

    System.out.println(
        """
				  OOP Visitor:
				    - Add a variant → modify every Visitor subclass
				    - Runtime type check (or forget accept())
				    - 2 classes per variant (variant + visit method)

				  ADT (sealed + switch):
				    - Add a variant → compiler flags every incomplete switch
				    - Compile-time exhaustiveness
				    - Zero boilerplate — records + pattern matching

				  Trade-off:
				    - ADTs are great when variants are fixed and operations grow
				    - Visitor is great when operations are fixed and variants grow
				    - Java's sealed types give you the ADT option that was missing before
				""");
  }

  // ── helper ───────────────────────────────────────────────────────────

  static Either<String, Integer> parseAge(String input) {
    try {
      return Either.right(Integer.parseInt(input));
    } catch (NumberFormatException _) {
      return Either.left("Not a number: " + input);
    }
  }
}
