package d021248.mytry.core.lang.fp.parsing;

import module java.base;

/// Utility class with higher-level parser combinators.
///
/// These combinators build on {@link Parser}'s primitives to parse common
/// patterns: integers, whitespace, tokens, and bracketed expressions.
public final class Parsers {

  private Parsers() {}

  /// Parses a non-negative integer (one or more digits) into an `int`.
  public static Parser<Integer> integer() {
    return Parser.digit()
        .many1()
        .map(digits -> digits.stream().map(String::valueOf).collect(Collectors.joining()))
        .map(Integer::parseInt);
  }

  /// Parses zero or more whitespace characters (space or tab); always succeeds.
  public static Parser<String> whitespace() {
    return Parser.satisfy(c -> c == ' ' || c == '\t')
        .many()
        .map(chars -> chars.stream().map(String::valueOf).collect(Collectors.joining()));
  }

  /// Matches the literal string `s`, then skips optional trailing whitespace.
  public static Parser<String> token(String s) {
    return Parser.string(s).thenDrop(whitespace());
  }

  /// Parses `pa` surrounded by `left` and `right` delimiters; returns only the middle value.
  public static <L, A, R> Parser<A> between(Parser<L> left, Parser<A> pa, Parser<R> right) {
    return left.thenKeep(pa).thenDrop(right);
  }
}
