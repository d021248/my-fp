package d021248.mytry.core.lang.fp.parsing;

import module java.base;

/// Demonstrates applicative parser combinators.
///
/// Shows two concrete examples:
/// 1. Parsing a simple additive expression `3 + 5`
/// 2. Parsing a single CSV record
public final class ParserDemo {

  /// Parses `digit + digit` (e.g. `"3 + 5"`) and returns the sum.
  static int runAdditionDemo() {
    var ws = Parsers.whitespace();
    var num = Parsers.integer();
    var plus = ws.thenKeep(Parser.ch('+')).thenDrop(ws);

    // left operand, skip " + ", right operand
    var exprParser = num.thenDrop(plus).flatMap(left -> num.map(right -> left + right));

    return exprParser
        .parse("3 + 5")
        .map(Parser.ParseResult::value)
        .orElseThrow(() -> new IllegalStateException("parse failed"));
  }

  /// Parses a CSV record (comma-separated list of words).
  static List<String> runCsvDemo() {
    var letter = Parser.letter();
    var word =
        letter
            .many1()
            .map(chars -> chars.stream().map(String::valueOf).collect(Collectors.joining()));
    var comma = Parser.ch(',').thenDrop(Parsers.whitespace());
    var csvField = word.thenDrop(comma.or(Parser.pure(',')));

    // collect all fields, then the last word (no trailing comma)
    var csvLine =
        csvField
            .many()
            .flatMap(
                fields ->
                    word.map(
                        last -> {
                          var all = new ArrayList<String>(fields);
                          all.add(last);
                          return Collections.unmodifiableList(all);
                        }));

    return csvLine
        .parse("apple, banana, cherry")
        .map(Parser.ParseResult::value)
        .orElseThrow(() -> new IllegalStateException("csv parse failed"));
  }

  public static void main() {
    System.out.println("Addition demo: 3 + 5 = %d".formatted(runAdditionDemo()));
    System.out.println("CSV demo: %s".formatted(runCsvDemo()));
  }
}
