package d021248.mytry.core.lang.fp.parsing;

import module java.base;

/// A parser combinator that recognises part of a string and yields a typed value.
///
/// A `Parser<A>` is a function `String → Optional<ParseResult<A>>`:
/// - `Optional.empty()` means the parse failed.
/// - `Optional.of(result)` carries the parsed value and the unconsumed remainder.
///
/// ## Functor / Applicative / Monad
///
/// | Abstraction | Method | Meaning |
/// |---|---|---|
/// | Functor | `map` | transform the parsed value |
/// | Applicative | `ap` | apply a parser-wrapped function to a parser-wrapped value |
/// | Monad | `flatMap` | chain parsers where the second depends on the first |
///
/// ## Combinators
///
/// - `or` — ordered choice (try the left, fall back to the right)
/// - `many` / `many1` — zero-or-more / one-or-more repetition
/// - `thenKeep` / `thenDrop` — sequencing that discards one side
@FunctionalInterface
public interface Parser<A> {

  /// Attempts to parse a prefix of `input`.
  ///
  /// @param input the full remaining input string
  /// @return `Optional.of(result)` on success, `Optional.empty()` on failure
  Optional<ParseResult<A>> parse(String input);

  /// Holds a successfully parsed value together with the unconsumed input tail.
  record ParseResult<A>(A value, String remaining) {}

  // ── Static factories ───────────────────────────────────────────────────────

  /// Always succeeds without consuming any input.
  static <A> Parser<A> pure(A a) {
    return input -> Optional.of(new ParseResult<>(a, input));
  }

  /// Always fails.
  static <A> Parser<A> fail() {
    return _ -> Optional.empty();
  }

  /// Consumes and returns the next character; fails on empty input.
  static Parser<Character> item() {
    return input ->
        input.isEmpty()
            ? Optional.empty()
            : Optional.of(new ParseResult<>(input.charAt(0), input.substring(1)));
  }

  /// Consumes the next character if it satisfies `pred`; fails otherwise.
  static Parser<Character> satisfy(Predicate<Character> pred) {
    return input -> {
      if (input.isEmpty()) return Optional.empty();
      var c = input.charAt(0);
      return pred.test(c)
          ? Optional.of(new ParseResult<>(c, input.substring(1)))
          : Optional.empty();
    };
  }

  /// Matches exactly the character `c`.
  static Parser<Character> ch(char c) {
    return satisfy(x -> x == c);
  }

  /// Matches the literal string `s` as a prefix; fails if the input does not start with `s`.
  static Parser<String> string(String s) {
    return input ->
        input.startsWith(s)
            ? Optional.of(new ParseResult<>(s, input.substring(s.length())))
            : Optional.empty();
  }

  /// Matches a single ASCII decimal digit (`0`–`9`).
  static Parser<Character> digit() {
    return satisfy(Character::isDigit);
  }

  /// Matches a single ASCII letter.
  static Parser<Character> letter() {
    return satisfy(Character::isLetter);
  }

  // ── Instance combinators ──────────────────────────────────────────────────

  /// Transforms the parsed value with `f` (functor `fmap`).
  default <B> Parser<B> map(Function<A, B> f) {
    return input -> parse(input).map(r -> new ParseResult<>(f.apply(r.value()), r.remaining()));
  }

  /// Feeds the parsed value into `f` and runs the resulting parser (monad `>>=`).
  default <B> Parser<B> flatMap(Function<A, Parser<B>> f) {
    return input -> parse(input).flatMap(r -> f.apply(r.value()).parse(r.remaining()));
  }

  /// Applies a parser-wrapped function to this parser's result (applicative `<*>`).
  default <B> Parser<B> ap(Parser<Function<A, B>> pf) {
    return pf.flatMap(this::map);
  }

  /// Ordered choice: tries `this`; returns `other`'s result if `this` fails.
  default Parser<A> or(Parser<A> other) {
    return input -> {
      var result = parse(input);
      return result.isPresent() ? result : other.parse(input);
    };
  }

  /// Parses zero or more occurrences (greedy, never fails).
  default Parser<List<A>> many() {
    return input -> {
      var items = new ArrayList<A>();
      var remaining = input;
      while (true) {
        var result = parse(remaining);
        if (result.isEmpty()) break;
        items.add(result.get().value());
        remaining = result.get().remaining();
      }
      return Optional.of(new ParseResult<>(Collections.unmodifiableList(items), remaining));
    };
  }

  /// Parses one or more occurrences; fails if zero are found.
  default Parser<List<A>> many1() {
    return flatMap(
        first ->
            many()
                .map(
                    rest -> {
                      var all = new ArrayList<A>();
                      all.add(first);
                      all.addAll(rest);
                      return Collections.unmodifiableList(all);
                    }));
  }

  /// Sequences `this` then `pb`; keeps only `pb`'s result (analogue of `*>`).
  default <B> Parser<B> thenKeep(Parser<B> pb) {
    return flatMap(_ -> pb);
  }

  /// Sequences `this` then `pb`; keeps only `this`'s result (analogue of `<*`).
  default <B> Parser<A> thenDrop(Parser<B> pb) {
    return flatMap(a -> pb.map(_ -> a));
  }
}
