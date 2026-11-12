package d021248.mytry.core.lang.fp.arrows;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ArrowTest {

  // ── arr ───────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Arrow.arr — lift a function into an arrow")
  class ArrTests {

    @Test
    @DisplayName(
        "arr lifts a function and apply produces the same result as calling the function directly")
    void arrLiftsFunction() {
      var doubled = Arrow.arr((Integer n) -> n * 2);
      assertThat(doubled.apply(5)).isEqualTo(10);
    }

    @Test
    @DisplayName("arr on identity function behaves as the identity arrow")
    void arrOnIdentityIsIdentity() {
      var identity = Arrow.arr((String s) -> s);
      assertThat(identity.apply("hello")).isEqualTo("hello");
    }
  }

  // ── apply ─────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("apply — run the arrow")
  class ApplyTests {

    @Test
    @DisplayName("apply returns the function result for the given input")
    void applyReturnsFunctionResult() {
      Arrow<Integer, String> toHex = n -> "0x%X".formatted(n);
      assertThat(toHex.apply(255)).isEqualTo("0xFF");
    }
  }

  // ── andThen ───────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("andThen — sequential composition")
  class AndThenTests {

    @Test
    @DisplayName("andThen composes two arrows left-to-right")
    void andThenComposesLeftToRight() {
      var addOne = Arrow.arr((Integer n) -> n + 1);
      var doubled = Arrow.arr((Integer n) -> n * 2);
      assertThat(addOne.andThen(doubled).apply(3)).isEqualTo(8); // (3+1)*2
    }

    @Test
    @DisplayName("andThen is associative: (f >=> g) >=> h equals f >=> (g >=> h)")
    void andThenIsAssociative() {
      var f = Arrow.arr((Integer n) -> n + 1);
      var g = Arrow.arr((Integer n) -> n * 2);
      var h = Arrow.arr((Integer n) -> n - 3);
      assertThat(f.andThen(g).andThen(h).apply(4)).isEqualTo(f.andThen(g.andThen(h)).apply(4));
    }

    @Test
    @DisplayName("identity arrow is a left unit of andThen")
    void identityIsLeftUnit() {
      var f = Arrow.arr((Integer n) -> n * 3);
      Arrow<Integer, Integer> id = Arrow.identity();
      assertThat(id.andThen(f).apply(5)).isEqualTo(f.apply(5));
    }

    @Test
    @DisplayName("identity arrow is a right unit of andThen")
    void identityIsRightUnit() {
      var f = Arrow.arr((Integer n) -> n * 3);
      Arrow<Integer, Integer> id = Arrow.identity();
      assertThat(f.andThen(id).apply(5)).isEqualTo(f.apply(5));
    }
  }

  // ── first ─────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("first — apply arrow to first element of a pair")
  class FirstTests {

    @Test
    @DisplayName("first applies the arrow to the first element, leaving the second intact")
    void firstTransformsFirstElement() {
      var times2 = Arrow.arr((Integer n) -> n * 2);
      var onFirst = times2.<String>first();
      var result = onFirst.apply(new Arrow.Pair<>(4, "unchanged"));
      assertThat(result.first()).isEqualTo(8);
      assertThat(result.second()).isEqualTo("unchanged");
    }

    @Test
    @DisplayName("first does not alter the second element of the pair")
    void firstDoesNotTouchSecond() {
      Arrow<String, String> upper = Arrow.arr(s -> s.toUpperCase());
      var onFirst = upper.<Integer>first();
      var result = onFirst.apply(new Arrow.Pair<>("hello", 99));
      assertThat(result.second()).isEqualTo(99);
    }
  }

  // ── second ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("second — apply arrow to second element of a pair")
  class SecondTests {

    @Test
    @DisplayName("second applies the arrow to the second element, leaving the first intact")
    void secondTransformsSecondElement() {
      var times3 = Arrow.arr((Integer n) -> n * 3);
      var onSecond = times3.<String>second();
      var result = onSecond.apply(new Arrow.Pair<>("unchanged", 5));
      assertThat(result.second()).isEqualTo(15);
      assertThat(result.first()).isEqualTo("unchanged");
    }
  }

  // ── fanout ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("fanout — apply two arrows to the same input")
  class FanoutTests {

    @Test
    @DisplayName("fanout returns a pair of both arrows' results")
    void fanoutReturnsPairOfBothResults() {
      Arrow<String, Integer> length = Arrow.arr(String::length);
      Arrow<String, String> upper = Arrow.arr(String::toUpperCase);
      var both = length.fanout(upper);
      var result = both.apply("hello");
      assertThat(result.first()).isEqualTo(5);
      assertThat(result.second()).isEqualTo("HELLO");
    }

    @Test
    @DisplayName(
        "fanout with identity on right returns a pair of the original and transformed value")
    void fanoutWithIdentityOnRight() {
      var doubled = Arrow.arr((Integer n) -> n * 2);
      Arrow<Integer, Integer> id = Arrow.identity();
      var fanoutResult = doubled.fanout(id).apply(7);
      assertThat(fanoutResult.first()).isEqualTo(14);
      assertThat(fanoutResult.second()).isEqualTo(7);
    }
  }
}
