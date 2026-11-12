package d021248.mytry.core.lang.fp.cofree;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CofreeStreamTest {

  @Nested
  @DisplayName("iterate — generates an infinite stream from a seed")
  class IterateTests {

    @Test
    @DisplayName("head equals the seed")
    void headEqualsSeed() {
      assertThat(CofreeStream.iterate(5, n -> n + 1).head()).isEqualTo(5);
    }

    @Test
    @DisplayName("take(5) on naturals from 0 returns [0, 1, 2, 3, 4]")
    void takeReturnsFirstNElements() {
      assertThat(CofreeStream.iterate(0, n -> n + 1).take(5)).containsExactly(0, 1, 2, 3, 4);
    }

    @Test
    @DisplayName("take(1) returns only the head")
    void take1ReturnsHead() {
      assertThat(CofreeStream.iterate(42, n -> n + 1).take(1)).containsExactly(42);
    }
  }

  @Nested
  @DisplayName("unfold — generates a stream from a seed and step function")
  class UnfoldTests {

    @Test
    @DisplayName("first 10 Fibonacci numbers are computed correctly")
    void fibonacciFirst10() {
      record FibState(long a, long b) {}
      var fibs =
          CofreeStream.unfold(
              new FibState(0L, 1L),
              s -> new CofreeStream.Pair<>(s.a(), new FibState(s.b(), s.a() + s.b())));
      assertThat(fibs.take(10)).containsExactly(0L, 1L, 1L, 2L, 3L, 5L, 8L, 13L, 21L, 34L);
    }

    @Test
    @DisplayName("unfold of powers of 2 produces [1, 2, 4, 8, 16] for first 5")
    void powersOfTwo() {
      var powers = CofreeStream.unfold(1L, n -> new CofreeStream.Pair<>(n, n * 2));
      assertThat(powers.take(5)).containsExactly(1L, 2L, 4L, 8L, 16L);
    }
  }

  @Nested
  @DisplayName("map — lazy functor transformation")
  class MapTests {

    @Test
    @DisplayName("map transforms every element lazily")
    void mapTransformsElements() {
      var doubled = CofreeStream.iterate(1, n -> n + 1).map(n -> n * 2);
      assertThat(doubled.take(5)).containsExactly(2, 4, 6, 8, 10);
    }

    @Test
    @DisplayName("map head equals f(original head)")
    void mapHeadEqualsAppliedFunction() {
      var s = CofreeStream.iterate(3, n -> n + 1).map(n -> "x" + n);
      assertThat(s.head()).isEqualTo("x3");
    }
  }

  @Nested
  @DisplayName("extend — context-sensitive transform at every position")
  class ExtendTests {

    @Test
    @DisplayName("extend with extract is the identity (comonad law)")
    void extendWithExtractIsIdentity() {
      var nats = CofreeStream.iterate(0, n -> n + 1);
      var extended = nats.extend(CofreeStream::extract);
      assertThat(extended.take(5)).isEqualTo(nats.take(5));
    }

    @Test
    @DisplayName("extend(f).extract() equals f(this) (comonad law)")
    void extractAfterExtendEqualsF() {
      var nats = CofreeStream.iterate(0, n -> n + 1);
      var doubled = nats.extend(ctx -> ctx.head() * 2);
      assertThat(doubled.extract()).isEqualTo(nats.head() * 2);
    }

    @Test
    @DisplayName("extend computes running pair-sum at every position")
    void extendRunningPairSum() {
      var nats = CofreeStream.iterate(0, n -> n + 1);
      var pairSums = nats.extend(ctx -> ctx.head() + ctx.tail().get().head());
      // 0+1=1, 1+2=3, 2+3=5, 3+4=7, 4+5=9
      assertThat(pairSums.take(5)).containsExactly(1, 3, 5, 7, 9);
    }
  }

  @Nested
  @DisplayName("drop — skips elements")
  class DropTests {

    @Test
    @DisplayName("drop(0) returns the same stream")
    void drop0ReturnsSame() {
      var s = CofreeStream.iterate(0, n -> n + 1);
      assertThat(s.drop(0).head()).isEqualTo(s.head());
    }

    @Test
    @DisplayName("drop(5) on naturals has head 5")
    void drop5HasHead5() {
      assertThat(CofreeStream.iterate(0, n -> n + 1).drop(5).head()).isEqualTo(5);
    }

    @Test
    @DisplayName("take(3) after drop(3) returns [3, 4, 5]")
    void dropThenTake() {
      assertThat(CofreeStream.iterate(0, n -> n + 1).drop(3).take(3)).containsExactly(3, 4, 5);
    }
  }
}
