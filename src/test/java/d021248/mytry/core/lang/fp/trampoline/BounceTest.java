package d021248.mytry.core.lang.fp.trampoline;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BounceTest {

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------

  /// Tail-recursive factorial via trampoline.
  private static Bounce<Long> factStep(int n, long acc) {
    if (n <= 1) return Bounce.done(acc);
    return Bounce.more(() -> factStep(n - 1, (long) n * acc));
  }

  /// Tail-recursive Fibonacci via trampoline.
  private static Bounce<Long> fibStep(int n, long a, long b) {
    if (n == 0) return Bounce.done(a);
    return Bounce.more(() -> fibStep(n - 1, b, a + b));
  }

  // -----------------------------------------------------------------------
  // Bounce.done
  // -----------------------------------------------------------------------

  @Test
  @DisplayName("Bounce.done returns the value immediately without any More steps")
  void doneReturnsValueImmediately() {
    assertThat(Bounce.run(Bounce.done(42))).isEqualTo(42);
  }

  @Test
  @DisplayName("Bounce.done with null value runs successfully")
  void doneWithNullValue() {
    Object result = Bounce.run(Bounce.done(null));
    assertThat(result).isNull();
  }

  // -----------------------------------------------------------------------
  // Bounce.more
  // -----------------------------------------------------------------------

  @Test
  @DisplayName("Bounce.more defers evaluation to the supplier")
  void moreDeferEvaluation() {
    Bounce<String> lazy = Bounce.more(() -> Bounce.done("lazy"));
    assertThat(Bounce.run(lazy)).isEqualTo("lazy");
  }

  // -----------------------------------------------------------------------
  // Trampoline recursion
  // -----------------------------------------------------------------------

  @Test
  @DisplayName("Trampoline computes factorial of 10 correctly")
  void trampolineFactorial10() {
    assertThat(Bounce.run(factStep(10, 1L))).isEqualTo(3628800L);
  }

  @Test
  @DisplayName("Trampoline computes factorial of 20 without stack overflow")
  void trampolineFactorial20() {
    // 20! = 2432902008176640000
    assertThat(Bounce.run(factStep(20, 1L))).isEqualTo(2432902008176640000L);
  }

  @Test
  @DisplayName("Trampoline runs deep recursion (10_000 steps) without stack overflow")
  void trampolineDeepRecursionNoStackOverflow() {
    // Sum 1..10000 via trampoline
    Bounce<Long> sumStep = sumTo(10_000L, 0L);
    assertThat(Bounce.run(sumStep)).isEqualTo(50_005_000L);
  }

  @Test
  @DisplayName("Trampoline computes Fibonacci of 10 correctly")
  void trampolineFibonacci10() {
    assertThat(Bounce.run(fibStep(10, 0L, 1L))).isEqualTo(55L);
  }

  // Helper for deep-recursion test
  private static Bounce<Long> sumTo(long n, long acc) {
    if (n <= 0) return Bounce.done(acc);
    return Bounce.more(() -> sumTo(n - 1, acc + n));
  }
}
