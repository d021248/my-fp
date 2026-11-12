package d021248.mytry.core.lang.fp.trampoline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TrampolineTest {

  // -------------------------------------------------------------------------
  // Factorial
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("factorial computes correct values for 0, 1, 5, 10, and 20")
  void factorialSmallValues() {
    assertThat(TrampolineDemo.factorial(0)).isEqualTo(1L);
    assertThat(TrampolineDemo.factorial(1)).isEqualTo(1L);
    assertThat(TrampolineDemo.factorial(5)).isEqualTo(120L);
    assertThat(TrampolineDemo.factorial(10)).isEqualTo(3_628_800L);
    assertThat(TrampolineDemo.factorial(20)).isEqualTo(2_432_902_008_176_640_000L);
  }

  @Test
  @DisplayName("factorial(100_000) does not throw StackOverflowError")
  void factorialLargeInputDoesNotThrowStackOverflow() {
    // Direct recursion would StackOverflow around 10_000 — trampoline handles any
    // depth
    assertDoesNotThrow(() -> TrampolineDemo.factorial(100_000));
  }

  // -------------------------------------------------------------------------
  // isEven / isOdd (mutual recursion)
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("isEven returns true for 0 and false for 1 (base cases)")
  void isEvenBaseCase() {
    assertThat(TrampolineDemo.isEven(0)).isTrue();
    assertThat(TrampolineDemo.isEven(1)).isFalse();
  }

  @Test
  @DisplayName("isEven correctly classifies small even and odd numbers")
  void isEvenSmallValues() {
    assertThat(TrampolineDemo.isEven(2)).isTrue();
    assertThat(TrampolineDemo.isEven(42)).isTrue();
    assertThat(TrampolineDemo.isEven(99)).isFalse();
  }

  @Test
  @DisplayName("isEven(100_000) and isEven(100_001) do not throw StackOverflowError")
  void isEvenLargeInputNoStackOverflow() {
    assertDoesNotThrow(() -> TrampolineDemo.isEven(100_000));
    assertThat(TrampolineDemo.isEven(100_000)).isTrue();
    assertThat(TrampolineDemo.isEven(100_001)).isFalse();
  }

  // -------------------------------------------------------------------------
  // Sum
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("sum computes correct totals for 0, 1, 100, and 1_000")
  void sumKnownValues() {
    assertThat(TrampolineDemo.sum(0)).isEqualTo(0L);
    assertThat(TrampolineDemo.sum(1)).isEqualTo(1L);
    assertThat(TrampolineDemo.sum(100)).isEqualTo(5_050L);
    assertThat(TrampolineDemo.sum(1_000)).isEqualTo(500_500L);
  }

  @Test
  @DisplayName("sum(1_000_000) does not throw StackOverflowError and equals 500_000_500_000")
  void sumLargeInputNoStackOverflow() {
    assertDoesNotThrow(() -> TrampolineDemo.sum(1_000_000));
    assertThat(TrampolineDemo.sum(1_000_000)).isEqualTo(500_000_500_000L);
  }

  // -------------------------------------------------------------------------
  // Bounce API directly
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Bounce.run returns the value of a Done immediately")
  void bounceRunReturnsDoneValueImmediately() {
    var result = Bounce.run(Bounce.done(42));
    assertThat(result).isEqualTo(42);
  }

  @Test
  @DisplayName("Bounce.run evaluates a chain of five nested More steps")
  void bounceRunEvaluatesChainOfMore() {
    // 5 steps — each More delegates to the next
    Bounce<Integer> b =
        Bounce.more(
            () ->
                Bounce.more(
                    () ->
                        Bounce.more(() -> Bounce.more(() -> Bounce.more(() -> Bounce.done(99))))));
    assertThat(Bounce.run(b)).isEqualTo(99);
  }
}
