package d021248.mytry.core.lang.fp.comonad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ComonadTest {

  // ── Store ─────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Store comonad")
  class StoreTests {

    @Test
    @DisplayName("extract returns the value at the current focus position")
    void extractAtFocus() {
      var s = Store.<Integer, Integer>of(n -> n * n, 4);
      assertThat(s.extract()).isEqualTo(16);
    }

    @Test
    @DisplayName("peek reads a value at a given position without moving the focus")
    void peekDoesNotMoveFocus() {
      var s = Store.<Integer, Integer>of(n -> n * n, 4);
      assertThat(s.peek(3)).isEqualTo(9);
      assertThat(s.extract()).isEqualTo(16);
    }

    @Test
    @DisplayName("seek moves the focus to a new position")
    void seekMovesFocus() {
      var s = Store.<Integer, Integer>of(n -> n * n, 4);
      assertThat(s.seek(5).extract()).isEqualTo(25);
    }

    @Test
    @DisplayName("map transforms the value type")
    void mapTransformsValue() {
      var s = Store.<Integer, Integer>of(n -> n * n, 3).map(v -> "sq=" + v);
      assertThat(s.extract()).isEqualTo("sq=9");
      assertThat(s.peek(2)).isEqualTo("sq=4");
    }

    @Test
    @DisplayName("extend applies a context-sensitive function at every position")
    void extendNeighbourSum() {
      var s = Store.<Integer, Integer>of(n -> n * n, 3);
      // pos 3: 2²+3²+4² = 4+9+16 = 29
      var ns = s.extend(ctx -> ctx.peek(ctx.pos() - 1) + ctx.extract() + ctx.peek(ctx.pos() + 1));
      assertThat(ns.extract()).isEqualTo(29);
      // pos 5: 4²+5²+6² = 16+25+36 = 77
      assertThat(ns.seek(5).extract()).isEqualTo(77);
    }

    @Test
    @DisplayName("extend with extract is a no-op (comonad identity law)")
    void extendExtractIsNoop() {
      var s = Store.<Integer, Integer>of(n -> n + 1, 7);
      assertThat(s.extend(Store::extract).extract()).isEqualTo(s.extract());
    }

    @Test
    @DisplayName("extract after extend returns f applied to the store (comonad law)")
    void extractAfterExtend() {
      var s = Store.<Integer, Integer>of(n -> n * 2, 3);
      int expected = s.extract() + 100;
      assertThat(s.extend(ctx -> ctx.extract() + 100).extract()).isEqualTo(expected);
    }
  }

  // ── Env ───────────────────────────────────────────────────────────────────

  record Config(int precision) {}

  @Nested
  @DisplayName("Env comonad")
  class EnvTests {

    @Test
    @DisplayName("extract returns the value ignoring the environment")
    void extractReturnsValue() {
      var e = Env.of(new Config(4), "hello");
      assertThat(e.extract()).isEqualTo("hello");
    }

    @Test
    @DisplayName("ask returns the environment")
    void askReturnsEnv() {
      var e = Env.of(new Config(3), "x");
      assertThat(e.ask().precision()).isEqualTo(3);
    }

    @Test
    @DisplayName("map transforms the value without changing the environment")
    void mapTransformsValue() {
      var e = Env.of(new Config(2), 10).map(n -> n * 3);
      assertThat(e.extract()).isEqualTo(30);
      assertThat(e.ask().precision()).isEqualTo(2);
    }

    @Test
    @DisplayName("extend applies a context-sensitive function keeping the environment")
    void extendFormatsWithConfig() {
      var e = Env.of(new Config(4), Math.PI);
      var result =
          e.extend(
              ctx -> String.format(Locale.ROOT, "%." + ctx.ask().precision() + "f", ctx.extract()));
      assertThat(result.extract()).isEqualTo("3.1416");
    }

    @Test
    @DisplayName("local returns an Env with a modified environment")
    void localModifiesEnv() {
      var e = Env.of(new Config(4), Math.PI);
      var result =
          e.local(cfg -> new Config(2))
              .extend(
                  ctx ->
                      String.format(
                          Locale.ROOT, "%." + ctx.ask().precision() + "f", ctx.extract()));
      assertThat(result.extract()).isEqualTo("3.14");
    }

    @Test
    @DisplayName("extend with extract is a no-op (comonad identity law)")
    void extendExtractIsNoop() {
      var e = Env.of("env", 42);
      assertThat(e.extend(Env::extract).extract()).isEqualTo(e.extract());
    }

    @Test
    @DisplayName("doubled pi value is within tolerance of 2*PI")
    void mapDoublesPi() {
      var e = Env.of(new Config(6), Math.PI).map(v -> v * 2);
      assertThat(e.extract()).isCloseTo(2 * Math.PI, within(1e-10));
    }
  }

  // ── Traced ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Traced<M, A>")
  class TracedTests {

    @Test
    @DisplayName("extract evaluates the function at the monoid identity")
    void extractEvaluatesAtIdentity() {
      var t = Traced.of(String::length, "", String::concat);
      assertThat(t.extract()).isEqualTo(0);
    }

    @Test
    @DisplayName("ask evaluates the function at a given monoid element")
    void askEvaluatesAtGivenElement() {
      var t = Traced.of(String::length, "", String::concat);
      assertThat(t.ask("hello")).isEqualTo(5);
    }

    @Test
    @DisplayName("map applies g to every output of the traced value")
    void mapTransformsOutput() {
      var t = Traced.of(String::length, "", String::concat);
      var doubled = t.map(n -> n * 2);
      assertThat(doubled.ask("abc")).isEqualTo(6);
    }

    @Test
    @DisplayName("local pre-transforms the incoming index")
    void localPreTransformsIndex() {
      var t = Traced.of(String::length, "", String::concat);
      var withBang = t.local(s -> s + "!");
      assertThat(withBang.ask("hi")).isEqualTo(3); // "hi!" has length 3
    }

    @Test
    @DisplayName("extend: result at identity satisfies extract ∘ extend f == f")
    void extendExtractLaw() {
      var t = Traced.of(String::length, "", String::concat);
      // f = traced value that counts characters
      var extended = t.extend(w -> w.ask("!") + w.ask("!!"));
      // At identity: w.ask("!") = 1, w.ask("!!") = 2 → 3
      assertThat(extended.extract()).isEqualTo(3);
    }

    @Test
    @DisplayName("extend prepends the current index before querying sub-traced value")
    void extendPrependsIndex() {
      var t = Traced.of(String::length, "", String::concat);
      // extend: at each index m, the sub-traced value prepends m to future queries
      var extended = t.extend(w -> w.ask("AB")); // always asks for 2 more chars
      // extract() → ask("") → sub-traced from "" → ask("AB") → length("AB") = 2
      assertThat(extended.extract()).isEqualTo(2);
      // ask("XY") → sub-traced from "XY" → ask("XY" + "AB") → length("XYAB") = 4
      assertThat(extended.ask("XY")).isEqualTo(4);
    }
  }
}
