package d021248.mytry.core.lang.fp.parsing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ParserTest {

  // ── Helpers ───────────────────────────────────────────────────────────────

  private static <A> Parser.ParseResult<A> parse(Parser<A> p, String input) {
    return p.parse(input).orElseThrow(() -> new AssertionError("parser failed on: " + input));
  }

  private static <A> void assertFails(Parser<A> p, String input) {
    assertThat(p.parse(input)).as("expected failure on: " + input).isEmpty();
  }

  // ── pure ──────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Parser.pure")
  class PureTests {

    @Test
    @DisplayName("pure always succeeds and consumes no input")
    void pureSucceedsWithoutConsuming() {
      var r = parse(Parser.pure(42), "hello");
      assertThat(r.value()).isEqualTo(42);
      assertThat(r.remaining()).isEqualTo("hello");
    }

    @Test
    @DisplayName("pure succeeds on empty input")
    void pureSucceedsOnEmpty() {
      var r = parse(Parser.pure("x"), "");
      assertThat(r.value()).isEqualTo("x");
    }
  }

  // ── fail ──────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Parser.fail")
  class FailTests {

    @Test
    @DisplayName("fail always returns empty")
    void failAlwaysReturnsEmpty() {
      assertFails(Parser.fail(), "anything");
    }

    @Test
    @DisplayName("fail returns empty on empty input")
    void failOnEmptyInput() {
      assertFails(Parser.fail(), "");
    }
  }

  // ── item ──────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Parser.item")
  class ItemTests {

    @Test
    @DisplayName("item consumes and returns the first character")
    void itemConsumesFirstChar() {
      var r = parse(Parser.item(), "abc");
      assertThat(r.value()).isEqualTo('a');
      assertThat(r.remaining()).isEqualTo("bc");
    }

    @Test
    @DisplayName("item fails on empty input")
    void itemFailsOnEmpty() {
      assertFails(Parser.item(), "");
    }
  }

  // ── ch ────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Parser.ch")
  class ChTests {

    @Test
    @DisplayName("ch matches the exact character and advances input")
    void chMatchesExactChar() {
      var r = parse(Parser.ch('x'), "xyz");
      assertThat(r.value()).isEqualTo('x');
      assertThat(r.remaining()).isEqualTo("yz");
    }

    @Test
    @DisplayName("ch fails when first character does not match")
    void chFailsOnMismatch() {
      assertFails(Parser.ch('z'), "abc");
    }

    @Test
    @DisplayName("ch fails on empty input")
    void chFailsOnEmpty() {
      assertFails(Parser.ch('a'), "");
    }
  }

  // ── string ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Parser.string")
  class StringTests {

    @Test
    @DisplayName("string matches a literal prefix and advances past it")
    void stringMatchesPrefix() {
      var r = parse(Parser.string("hello"), "hello world");
      assertThat(r.value()).isEqualTo("hello");
      assertThat(r.remaining()).isEqualTo(" world");
    }

    @Test
    @DisplayName("string succeeds when the full input is the prefix")
    void stringMatchesFullInput() {
      var r = parse(Parser.string("abc"), "abc");
      assertThat(r.value()).isEqualTo("abc");
      assertThat(r.remaining()).isEmpty();
    }

    @Test
    @DisplayName("string fails when input does not start with the literal")
    void stringFailsOnMismatch() {
      assertFails(Parser.string("xyz"), "abcxyz");
    }
  }

  // ── digit ─────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Parser.digit")
  class DigitTests {

    @Test
    @DisplayName("digit matches a single decimal digit character")
    void digitMatchesDigitChar() {
      var r = parse(Parser.digit(), "3abc");
      assertThat(r.value()).isEqualTo('3');
      assertThat(r.remaining()).isEqualTo("abc");
    }

    @Test
    @DisplayName("digit fails on a non-digit character")
    void digitFailsOnLetter() {
      assertFails(Parser.digit(), "abc");
    }
  }

  // ── letter ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Parser.letter")
  class LetterTests {

    @Test
    @DisplayName("letter matches a single alphabetic character")
    void letterMatchesAlphaChar() {
      var r = parse(Parser.letter(), "abc");
      assertThat(r.value()).isEqualTo('a');
    }

    @Test
    @DisplayName("letter fails on a digit character")
    void letterFailsOnDigit() {
      assertFails(Parser.letter(), "9abc");
    }
  }

  // ── map ───────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("map (functor)")
  class MapTests {

    @Test
    @DisplayName("map transforms the parsed value, leaving remaining unchanged")
    void mapTransformsParsedValue() {
      var r = parse(Parser.digit().map(c -> c - '0'), "5rest");
      assertThat(r.value()).isEqualTo(5);
      assertThat(r.remaining()).isEqualTo("rest");
    }

    @Test
    @DisplayName("map propagates failure when the underlying parser fails")
    void mapPropagatesFailure() {
      assertFails(Parser.digit().map(c -> c - '0'), "abc");
    }
  }

  // ── flatMap ───────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("flatMap (monad)")
  class FlatMapTests {

    @Test
    @DisplayName("flatMap chains two parsers sequentially")
    void flatMapChainsParsers() {
      // parse two digits and concatenate them as a string
      var twoDigits = Parser.digit().flatMap(a -> Parser.digit().map(b -> "" + a + b));
      var r = parse(twoDigits, "42rest");
      assertThat(r.value()).isEqualTo("42");
      assertThat(r.remaining()).isEqualTo("rest");
    }

    @Test
    @DisplayName("flatMap fails when the first parser fails")
    void flatMapFailsIfFirstFails() {
      var twoDigits = Parser.digit().flatMap(a -> Parser.digit().map(b -> "" + a + b));
      assertFails(twoDigits, "a2");
    }
  }

  // ── or ────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("or (ordered choice)")
  class OrTests {

    @Test
    @DisplayName("or returns the left result when the left parser succeeds")
    void orPrefersLeft() {
      var r = parse(Parser.ch('a').or(Parser.ch('b')), "abc");
      assertThat(r.value()).isEqualTo('a');
    }

    @Test
    @DisplayName("or falls back to the right parser when the left fails")
    void orFallsBackToRight() {
      var r = parse(Parser.ch('z').or(Parser.ch('a')), "abc");
      assertThat(r.value()).isEqualTo('a');
    }

    @Test
    @DisplayName("or fails when both alternatives fail")
    void orFailsWhenBothFail() {
      assertFails(Parser.ch('x').or(Parser.ch('y')), "abc");
    }
  }

  // ── many ──────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("many (zero or more)")
  class ManyTests {

    @Test
    @DisplayName("many collects all matching characters greedily")
    void manyCollectsAllMatching() {
      var r = parse(Parser.digit().many(), "123abc");
      assertThat(r.value()).containsExactly('1', '2', '3');
      assertThat(r.remaining()).isEqualTo("abc");
    }

    @Test
    @DisplayName("many returns an empty list when nothing matches")
    void manyReturnsEmptyListOnNoMatch() {
      var r = parse(Parser.digit().many(), "abc");
      assertThat(r.value()).isEmpty();
      assertThat(r.remaining()).isEqualTo("abc");
    }
  }

  // ── between ───────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Parsers.between")
  class BetweenTests {

    @Test
    @DisplayName("between extracts a value surrounded by delimiters")
    void betweenExtractsMiddleValue() {
      var quoted =
          Parsers.between(
              Parser.ch('"'),
              Parser.letter()
                  .many1()
                  .map(
                      chars ->
                          chars.stream()
                              .map(String::valueOf)
                              .collect(java.util.stream.Collectors.joining())),
              Parser.ch('"'));
      var r = parse(quoted, "\"hello\"tail");
      assertThat(r.value()).isEqualTo("hello");
      assertThat(r.remaining()).isEqualTo("tail");
    }
  }

  // ── integer ───────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Parsers.integer")
  class IntegerTests {

    @Test
    @DisplayName("integer parses one or more digits into an int")
    void integerParsesMultiDigitNumber() {
      var r = parse(Parsers.integer(), "42rest");
      assertThat(r.value()).isEqualTo(42);
      assertThat(r.remaining()).isEqualTo("rest");
    }

    @Test
    @DisplayName("integer parses a single digit")
    void integerParsesSingleDigit() {
      var r = parse(Parsers.integer(), "7");
      assertThat(r.value()).isEqualTo(7);
    }

    @Test
    @DisplayName("integer fails when input starts with a non-digit")
    void integerFailsOnNonDigit() {
      assertFails(Parsers.integer(), "abc");
    }
  }

  // ── thenKeep / thenDrop ───────────────────────────────────────────────────

  @Nested
  @DisplayName("thenKeep and thenDrop")
  class SequencingTests {

    @Test
    @DisplayName("thenKeep discards the left result and returns the right")
    void thenKeepReturnsRight() {
      var r = parse(Parser.ch('(').thenKeep(Parsers.integer()), "(42)");
      assertThat(r.value()).isEqualTo(42);
    }

    @Test
    @DisplayName("thenDrop discards the right result and returns the left")
    void thenDropReturnsLeft() {
      var r = parse(Parsers.integer().thenDrop(Parser.ch('!')), "99!");
      assertThat(r.value()).isEqualTo(99);
    }

    @Test
    @DisplayName("combined thenKeep and thenDrop form a bracketed integer parser")
    void combinedFormsBracketedIntParser() {
      var bracketed = Parser.ch('(').thenKeep(Parsers.integer()).thenDrop(Parser.ch(')'));
      var r = parse(bracketed, "(123)tail");
      assertThat(r.value()).isEqualTo(123);
      assertThat(r.remaining()).isEqualTo("tail");
    }
  }
}
