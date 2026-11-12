package d021248.mytry.core.lang.fp.kleisli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class KleisliTest {

  // Shared Kleisli arrows
  private static final Kleisli<Double, Double> safeSqrt =
      Kleisli.of(n -> n < 0 ? Optional.empty() : Optional.of(Math.sqrt(n)));
  private static final Kleisli<Double, Double> safeReciprocal =
      Kleisli.of(n -> n == 0.0 ? Optional.empty() : Optional.of(1.0 / n));

  @Nested
  @DisplayName("run — applies the arrow to an input")
  class RunTests {

    @Test
    @DisplayName("safeSqrt of a positive number returns its square root")
    void safeSqrtPositive() {
      assertThat(safeSqrt.run(4.0)).contains(2.0);
    }

    @Test
    @DisplayName("safeSqrt of a negative number returns empty")
    void safeSqrtNegative() {
      assertThat(safeSqrt.run(-1.0)).isEmpty();
    }

    @Test
    @DisplayName("safeReciprocal of a non-zero number returns 1/n")
    void safeReciprocalNonZero() {
      assertThat(safeReciprocal.run(2.0)).contains(0.5);
    }

    @Test
    @DisplayName("safeReciprocal of zero returns empty")
    void safeReciprocalZero() {
      assertThat(safeReciprocal.run(0.0)).isEmpty();
    }
  }

  @Nested
  @DisplayName("andThen — Kleisli composition (>=>)")
  class AndThenTests {

    @Test
    @DisplayName("composed arrow applies both functions in sequence")
    void andThenComposesTwoArrows() {
      var sqrtRecip = safeSqrt.andThen(safeReciprocal);
      assertThat(sqrtRecip.run(4.0)).contains(0.5); // sqrt(4)=2, 1/2=0.5
    }

    @Test
    @DisplayName("empty from first arrow short-circuits to empty")
    void andThenEmptyFromFirstShortCircuits() {
      var sqrtRecip = safeSqrt.andThen(safeReciprocal);
      assertThat(sqrtRecip.run(-1.0)).isEmpty();
    }

    @Test
    @DisplayName("empty from second arrow propagates to empty")
    void andThenEmptyFromSecondPropagates() {
      var sqrtRecip = safeSqrt.andThen(safeReciprocal);
      // sqrt(0) = 0, reciprocal(0) = empty
      assertThat(sqrtRecip.run(0.0)).isEmpty();
    }

    @Test
    @DisplayName("three arrows compose in sequence")
    void andThenThreeArrows() {
      var safeLog =
          Kleisli.<Double, Double>of(n -> n <= 0 ? Optional.empty() : Optional.of(Math.log(n)));
      var pipeline = safeSqrt.andThen(safeLog).andThen(safeReciprocal);
      // sqrt(e^2) = e, log(e) = 1, 1/1 = 1
      var result = pipeline.run(Math.E * Math.E);
      assertThat(result).isPresent();
      assertThat(result.get()).isCloseTo(1.0, within(1e-10));
    }
  }

  @Nested
  @DisplayName("compose — reverse Kleisli composition")
  class ComposeTests {

    @Test
    @DisplayName("compose is andThen with arguments reversed")
    void composeIsReverseAndThen() {
      var directCompose = safeSqrt.andThen(safeReciprocal);
      var reverseCompose = safeReciprocal.compose(safeSqrt);
      assertThat(reverseCompose.run(4.0)).isEqualTo(directCompose.run(4.0));
    }
  }

  @Nested
  @DisplayName("map — functor over the output")
  class MapTests {

    @Test
    @DisplayName("map transforms the successful output")
    void mapTransformsOutput() {
      var sqrtString = safeSqrt.map(n -> "sqrt=" + n);
      assertThat(sqrtString.run(9.0)).contains("sqrt=3.0");
    }

    @Test
    @DisplayName("map propagates empty without calling the function")
    void mapPropagatesEmpty() {
      var called = new AtomicBoolean(false);
      var sqrtString =
          safeSqrt.map(
              n -> {
                called.set(true);
                return n.toString();
              });
      assertThat(sqrtString.run(-1.0)).isEmpty();
      assertThat(called.get()).isFalse();
    }
  }

  @Nested
  @DisplayName("Monad laws as Kleisli category laws")
  class MonadLawTests {

    @Test
    @DisplayName("left identity: identity >=> f equals f")
    void leftIdentity() {
      var f = safeSqrt;
      var lhs = Kleisli.<Double>identity().andThen(f);
      assertThat(lhs.run(4.0)).isEqualTo(f.run(4.0));
    }

    @Test
    @DisplayName("right identity: f >=> identity equals f")
    void rightIdentity() {
      var f = safeSqrt;
      var rhs = f.andThen(Kleisli.identity());
      assertThat(rhs.run(4.0)).isEqualTo(f.run(4.0));
    }

    @Test
    @DisplayName("associativity: (f >=> g) >=> h equals f >=> (g >=> h)")
    void associativity() {
      var safeLog =
          Kleisli.<Double, Double>of(n -> n <= 0 ? Optional.empty() : Optional.of(Math.log(n)));
      var lhs = (safeSqrt.andThen(safeLog)).andThen(safeReciprocal);
      var rhs = safeSqrt.andThen(safeLog.andThen(safeReciprocal));
      assertThat(lhs.run(Math.E * Math.E)).isEqualTo(rhs.run(Math.E * Math.E));
    }
  }
}
