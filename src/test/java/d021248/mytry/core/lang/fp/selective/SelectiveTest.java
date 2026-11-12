package d021248.mytry.core.lang.fp.selective;

import static org.assertj.core.api.Assertions.assertThat;

import d021248.mytry.core.lang.adt.Either;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SelectiveTest {

  // ── Either ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Either sum type")
  class EitherTests {

    @Test
    @DisplayName("left() constructs a Left value and isLeft() returns true")
    void leftIsLeft() {
      var e = Either.<Integer, String>left(42);
      assertThat(e.isLeft()).isTrue();
      assertThat(e.isRight()).isFalse();
      assertThat(((Either.Left<Integer, String>) e).value()).isEqualTo(42);
    }

    @Test
    @DisplayName("right() constructs a Right value and isRight() returns true")
    void rightIsRight() {
      var e = Either.<Integer, String>right("hello");
      assertThat(e.isRight()).isTrue();
      assertThat(e.isLeft()).isFalse();
      assertThat(((Either.Right<Integer, String>) e).value()).isEqualTo("hello");
    }
  }

  // ── SelectiveOpt ─────────────────────────────────────────────────────────

  @Nested
  @DisplayName("SelectiveOpt — select operation")
  class SelectTests {

    @Test
    @DisplayName("select with Left applies the function to the contained value")
    void selectLeftAppliesFunction() {
      var result =
          SelectiveOpt.select(
              SelectiveOpt.pure(Either.<Integer, Integer>left(5)), SelectiveOpt.pure(n -> n * 2));
      assertThat(result.toOptional()).contains(10);
    }

    @Test
    @DisplayName("select with Right returns the right value without evaluating the function")
    void selectRightSkipsFunction() {
      // even though the function SelectiveOpt is empty, Right short-circuits
      SelectiveOpt<Either<Void, String>> rightEither = SelectiveOpt.pure(Either.right("hello"));
      SelectiveOpt<Function<Void, String>> noFn = SelectiveOpt.empty();
      var result = SelectiveOpt.select(rightEither, noFn);
      assertThat(result.toOptional()).contains("hello");
    }

    @Test
    @DisplayName("select with empty Either propagates empty")
    void selectEmptyEitherPropagatesEmpty() {
      SelectiveOpt<Either<Integer, Integer>> emptyEither = SelectiveOpt.empty();
      var result = SelectiveOpt.select(emptyEither, SelectiveOpt.pure(n -> n + 1));
      assertThat(result.toOptional()).isEmpty();
    }

    @Test
    @DisplayName("select with Left but empty function returns empty")
    void selectLeftWithEmptyFunctionReturnsEmpty() {
      SelectiveOpt<Function<Integer, Integer>> emptyFn = SelectiveOpt.empty();
      var result =
          SelectiveOpt.select(SelectiveOpt.pure(Either.<Integer, Integer>left(7)), emptyFn);
      assertThat(result.toOptional()).isEmpty();
    }

    @Test
    @DisplayName("select with Right applies string transformation")
    void selectRightWithStringValue() {
      var result =
          SelectiveOpt.select(
              SelectiveOpt.pure(Either.<Integer, String>right("world")),
              SelectiveOpt.pure(n -> "number " + n));
      assertThat(result.toOptional()).contains("world");
    }

    @Test
    @DisplayName("select with Left applies string-producing function")
    void selectLeftWithStringFunction() {
      var result =
          SelectiveOpt.select(
              SelectiveOpt.pure(Either.<Integer, String>left(3)),
              SelectiveOpt.pure(n -> "count=" + n));
      assertThat(result.toOptional()).contains("count=3");
    }
  }

  // ── Functor / Applicative ─────────────────────────────────────────────────

  @Nested
  @DisplayName("SelectiveOpt — map (functor)")
  class MapTests {

    @Test
    @DisplayName("map transforms the wrapped value")
    void mapTransformsValue() {
      var r = SelectiveOpt.pure(5).map(n -> n * 3);
      assertThat(r.toOptional()).contains(15);
    }

    @Test
    @DisplayName("map on empty propagates empty")
    void mapOnEmptyIsEmpty() {
      SelectiveOpt<Integer> empty = SelectiveOpt.empty();
      assertThat(empty.map(n -> n + 1).toOptional()).isEmpty();
    }

    @Test
    @DisplayName("map with identity is a no-op")
    void mapWithIdentityIsNoop() {
      var r = SelectiveOpt.pure("hello").map(s -> s);
      assertThat(r.toOptional()).contains("hello");
    }
  }

  @Nested
  @DisplayName("SelectiveOpt — ap (applicative)")
  class ApTests {

    @Test
    @DisplayName("ap applies a wrapped function to a wrapped value")
    void apAppliesFunction() {
      var result = SelectiveOpt.ap(SelectiveOpt.pure((Integer n) -> n * 2), SelectiveOpt.pure(21));
      assertThat(result.toOptional()).contains(42);
    }

    @Test
    @DisplayName("ap with empty function returns empty")
    void apWithEmptyFunctionIsEmpty() {
      SelectiveOpt<Function<Integer, Integer>> emptyFn = SelectiveOpt.empty();
      var result = SelectiveOpt.ap(emptyFn, SelectiveOpt.pure(10));
      assertThat(result.toOptional()).isEmpty();
    }

    @Test
    @DisplayName("ap with empty value returns empty")
    void apWithEmptyValueIsEmpty() {
      SelectiveOpt<Integer> emptyVal = SelectiveOpt.empty();
      var result = SelectiveOpt.ap(SelectiveOpt.pure((Integer n) -> n + 1), emptyVal);
      assertThat(result.toOptional()).isEmpty();
    }

    @Test
    @DisplayName("ap with both empty returns empty")
    void apBothEmptyIsEmpty() {
      SelectiveOpt<Function<Integer, Integer>> emptyFn = SelectiveOpt.empty();
      SelectiveOpt<Integer> emptyVal = SelectiveOpt.empty();
      assertThat(SelectiveOpt.ap(emptyFn, emptyVal).toOptional()).isEmpty();
    }
  }

  // ── presence helpers ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("SelectiveOpt — presence helpers")
  class PresenceTests {

    @Test
    @DisplayName("pure creates a present SelectiveOpt")
    void pureIsPresent() {
      assertThat(SelectiveOpt.pure(1).isPresent()).isTrue();
      assertThat(SelectiveOpt.pure(1).isEmpty()).isFalse();
    }

    @Test
    @DisplayName("empty creates an absent SelectiveOpt")
    void emptyIsAbsent() {
      assertThat(SelectiveOpt.<Integer>empty().isEmpty()).isTrue();
      assertThat(SelectiveOpt.<Integer>empty().isPresent()).isFalse();
    }
  }
}
