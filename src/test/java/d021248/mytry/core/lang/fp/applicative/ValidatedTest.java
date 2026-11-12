package d021248.mytry.core.lang.fp.applicative;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ValidatedTest {

  @Nested
  @DisplayName("Valid cases")
  class ValidCases {

    @Test
    @DisplayName("valid() creates a successful Validated")
    void validCreatesSuccess() {
      assertThat(Validated.valid(42).isValid()).isTrue();
    }

    @Test
    @DisplayName("map transforms the value inside a Valid")
    void mapTransformsValue() {
      var result = Validated.<String, Integer>valid(5).map(n -> n * 2);
      assertThat(((Validated.Valid<String, Integer>) result).value()).isEqualTo(10);
    }

    @Test
    @DisplayName("ap with Valid function and Valid value applies the function")
    void apAppliesFunction() {
      Validated<String, Function<Integer, String>> vf = Validated.valid(n -> "val=" + n);
      var result = Validated.<String, Integer>valid(7).ap(vf);
      assertThat(((Validated.Valid<String, String>) result).value()).isEqualTo("val=7");
    }

    @Test
    @DisplayName("flatMap sequences two successful steps")
    void flatMapSequencesSuccess() {
      var result = Validated.<String, Integer>valid(3).flatMap(n -> Validated.valid(n + 1));
      assertThat(((Validated.Valid<String, Integer>) result).value()).isEqualTo(4);
    }
  }

  @Nested
  @DisplayName("Invalid cases")
  class InvalidCases {

    @Test
    @DisplayName("invalid(E) creates an Invalid with a single error")
    void invalidCreatesSingleError() {
      var result = Validated.<String, Integer>invalid("oops");
      assertThat(result.isValid()).isFalse();
      assertThat(((Validated.Invalid<String, Integer>) result).errors()).containsExactly("oops");
    }

    @Test
    @DisplayName("map on Invalid propagates the errors unchanged")
    void mapPropagatesErrors() {
      var result = Validated.<String, Integer>invalid("err").map(n -> n * 2);
      assertThat(((Validated.Invalid<String, Integer>) result).errors()).containsExactly("err");
    }

    @Test
    @DisplayName("ap with Invalid function and Valid value returns function errors only")
    void apInvalidFnValidValue() {
      Validated<String, Function<Integer, String>> vf = Validated.invalid("fn-error");
      var result = Validated.<String, Integer>valid(7).ap(vf);
      assertThat(((Validated.Invalid<String, String>) result).errors()).containsExactly("fn-error");
    }

    @Test
    @DisplayName("ap with Valid function and Invalid value returns value errors only")
    void apValidFnInvalidValue() {
      Validated<String, Function<Integer, String>> vf = Validated.valid(n -> "v=" + n);
      var result = Validated.<String, Integer>invalid("val-error").ap(vf);
      assertThat(((Validated.Invalid<String, String>) result).errors())
          .containsExactly("val-error");
    }

    @Test
    @DisplayName("ap with BOTH Invalid accumulates errors from function AND value sides")
    void apBothInvalidAccumulatesAllErrors() {
      Validated<String, Function<Integer, String>> vf = Validated.invalid("fn-error");
      var result = Validated.<String, Integer>invalid("val-error").ap(vf);
      assertThat(((Validated.Invalid<String, String>) result).errors())
          .containsExactly("fn-error", "val-error");
    }

    @Test
    @DisplayName("flatMap on Invalid short-circuits without applying the function")
    void flatMapShortCircuits() {
      var called = new AtomicBoolean(false);
      var result =
          Validated.<String, Integer>invalid("first-error")
              .flatMap(
                  n -> {
                    called.set(true);
                    return Validated.valid(n + 1);
                  });
      assertThat(called.get()).isFalse();
      assertThat(((Validated.Invalid<String, Integer>) result).errors())
          .containsExactly("first-error");
    }
  }

  @Nested
  @DisplayName("liftA2 — independent combination")
  class LiftA2Cases {

    @Test
    @DisplayName("liftA2 with both valid produces the combined value")
    void liftA2BothValid() {
      var result =
          Validated.liftA2(
              (a, b) -> a + ":" + b,
              Validated.<String, String>valid("Alice"),
              Validated.<String, Integer>valid(30));
      assertThat(((Validated.Valid<String, String>) result).value()).isEqualTo("Alice:30");
    }

    @Test
    @DisplayName("liftA2 with first invalid returns first errors only")
    void liftA2FirstInvalid() {
      var result =
          Validated.liftA2(
              (a, b) -> a + b,
              Validated.<String, String>invalid("err-a"),
              Validated.<String, String>valid("ok"));
      assertThat(((Validated.Invalid<String, String>) result).errors()).containsExactly("err-a");
    }

    @Test
    @DisplayName("liftA2 with second invalid returns second errors only")
    void liftA2SecondInvalid() {
      var result =
          Validated.liftA2(
              (a, b) -> a + b,
              Validated.<String, String>valid("ok"),
              Validated.<String, String>invalid("err-b"));
      assertThat(((Validated.Invalid<String, String>) result).errors()).containsExactly("err-b");
    }

    @Test
    @DisplayName("liftA2 with BOTH invalid accumulates all errors")
    void liftA2BothInvalidAccumulatesAll() {
      var result =
          Validated.liftA2(
              (a, b) -> a + b,
              Validated.<String, String>invalid("err-a"),
              Validated.<String, String>invalid("err-b"));
      assertThat(((Validated.Invalid<String, String>) result).errors())
          .containsExactlyInAnyOrder("err-a", "err-b");
    }
  }
}
