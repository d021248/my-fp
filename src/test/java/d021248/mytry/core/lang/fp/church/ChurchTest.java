package d021248.mytry.core.lang.fp.church;

import static d021248.mytry.core.lang.fp.church.Church.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ChurchTest {

  @Nested
  @DisplayName("Church.Bool")
  class BoolTests {

    @Test
    @DisplayName("TRUE selects the first branch")
    void trueSelectsFirstBranch() {
      assertThat(TRUE.select("yes", "no")).isEqualTo("yes");
    }

    @Test
    @DisplayName("FALSE selects the second branch")
    void falseSelectsSecondBranch() {
      assertThat(FALSE.select("yes", "no")).isEqualTo("no");
    }

    @Test
    @DisplayName("and(TRUE, FALSE) is FALSE")
    void andTrueAndFalseIsFalse() {
      assertThat(toBoolean(and(TRUE, FALSE))).isFalse();
    }

    @Test
    @DisplayName("and(TRUE, TRUE) is TRUE")
    void andTrueAndTrueIsTrue() {
      assertThat(toBoolean(and(TRUE, TRUE))).isTrue();
    }

    @Test
    @DisplayName("or(FALSE, TRUE) is TRUE")
    void orFalseAndTrueIsTrue() {
      assertThat(toBoolean(or(FALSE, TRUE))).isTrue();
    }

    @Test
    @DisplayName("or(FALSE, FALSE) is FALSE")
    void orFalseAndFalseIsFalse() {
      assertThat(toBoolean(or(FALSE, FALSE))).isFalse();
    }

    @Test
    @DisplayName("not(TRUE) is FALSE")
    void notTrueIsFalse() {
      assertThat(toBoolean(not(TRUE))).isFalse();
    }

    @Test
    @DisplayName("not(FALSE) is TRUE")
    void notFalseIsTrue() {
      assertThat(toBoolean(not(FALSE))).isTrue();
    }

    @Test
    @DisplayName("fromBoolean/toBoolean round-trip preserves value for true and false")
    void fromBooleanToBooleanRoundTrip() {
      assertThat(toBoolean(fromBoolean(true))).isTrue();
      assertThat(toBoolean(fromBoolean(false))).isFalse();
    }
  }

  @Nested
  @DisplayName("Church.Num")
  class NumTests {

    @Test
    @DisplayName("ZERO converts to 0")
    void zeroConvertsToZero() {
      assertThat(toInt(ZERO)).isEqualTo(0);
    }

    @Test
    @DisplayName("succ(ZERO) converts to 1")
    void succZeroConvertsToOne() {
      assertThat(toInt(succ(ZERO))).isEqualTo(1);
    }

    @Test
    @DisplayName("fromInt/toInt round-trip for 0 through 5")
    void fromIntToIntRoundTrip() {
      for (var i = 0; i <= 5; i++) {
        assertThat(toInt(fromInt(i))).isEqualTo(i);
      }
    }

    @Test
    @DisplayName("add(two, three) equals 5")
    void addTwoAndThreeEqualsFive() {
      assertThat(toInt(add(fromInt(2), fromInt(3)))).isEqualTo(5);
    }

    @Test
    @DisplayName("mul(two, three) equals 6")
    void mulTwoAndThreeEqualsSix() {
      assertThat(toInt(mul(fromInt(2), fromInt(3)))).isEqualTo(6);
    }

    @Test
    @DisplayName("add is commutative: add(m, n) equals add(n, m)")
    void addIsCommutative() {
      var m = fromInt(4);
      var n = fromInt(7);
      assertThat(toInt(add(m, n))).isEqualTo(toInt(add(n, m)));
    }

    @Test
    @DisplayName("isZero(ZERO) is true")
    void isZeroOfZeroIsTrue() {
      assertThat(isZero(ZERO)).isTrue();
    }

    @Test
    @DisplayName("isZero(succ(ZERO)) is false")
    void isZeroOfOneIsFalse() {
      assertThat(isZero(succ(ZERO))).isFalse();
    }

    @Test
    @DisplayName("pred(ZERO) is ZERO")
    void predOfZeroIsZero() {
      assertThat(toInt(pred(ZERO))).isEqualTo(0);
    }

    @Test
    @DisplayName("pred(succ(ZERO)) is ZERO")
    void predOfOneIsZero() {
      assertThat(toInt(pred(succ(ZERO)))).isEqualTo(0);
    }

    @Test
    @DisplayName("pred(fromInt(5)) is 4")
    void predOfFiveIsFour() {
      assertThat(toInt(pred(fromInt(5)))).isEqualTo(4);
    }
  }

  @Nested
  @DisplayName("Church.Maybe")
  class MaybeTests {

    @Test
    @DisplayName("NOTHING returns the default value")
    void nothingReturnsDefault() {
      assertThat(fromMaybe("default", NOTHING)).isEqualTo("default");
    }

    @Test
    @DisplayName("just(v) returns v")
    void justReturnsValue() {
      assertThat(fromMaybe("default", just("hello"))).isEqualTo("hello");
    }

    @Test
    @DisplayName("just applies the just-branch function")
    void justAppliesJustBranch() {
      Church.Maybe m = just("world");
      assertThat(m.apply("none", s -> ((String) s).toUpperCase())).isEqualTo("WORLD");
    }
  }

  @Nested
  @DisplayName("Church.ChurchEither")
  class ChurchEitherTests {

    @Test
    @DisplayName("churchLeft(v) applies left branch")
    void leftAppliesLeftBranch() {
      var e = churchLeft(42);
      assertThat(Church.<String>foldEither(n -> "left:" + n, n -> "right:" + n, e))
          .isEqualTo("left:42");
    }

    @Test
    @DisplayName("churchRight(v) applies right branch")
    void rightAppliesRightBranch() {
      var e = churchRight("hello");
      assertThat(Church.<String>foldEither(n -> "left:" + n, n -> "right:" + n, e))
          .isEqualTo("right:hello");
    }

    @Test
    @DisplayName("left and right are mutually exclusive case selectors")
    void leftAndRightAreDisjoint() {
      assertThat(Church.<Boolean>foldEither(n -> true, n -> false, churchLeft("x"))).isTrue();
      assertThat(Church.<Boolean>foldEither(n -> false, n -> true, churchRight("y"))).isTrue();
    }
  }

  @Nested
  @DisplayName("Church.Pair")
  class PairTests {

    @Test
    @DisplayName("fst returns the first component")
    void fstReturnsFirstComponent() {
      assertThat(fst(pair("hello", 42))).isEqualTo("hello");
    }

    @Test
    @DisplayName("snd returns the second component")
    void sndReturnsSecondComponent() {
      assertThat(snd(pair("hello", 42))).isEqualTo(42);
    }

    @Test
    @DisplayName("fst and snd work independently on the same pair")
    void bothComponentsAccessibleFromSamePair() {
      var p = pair(1, 2);
      assertThat(fst(p)).isEqualTo(1);
      assertThat(snd(p)).isEqualTo(2);
    }
  }

  @Nested
  @DisplayName("Y Combinator")
  class YCombinatorTests {

    @Test
    @DisplayName("Y factorial computes 5! = 120 without self-reference in the lambda")
    void yFactorialComputesFiveFactorial() {
      Function<Integer, Integer> factorial = Y(f -> n -> n <= 1 ? 1 : n * f.apply(n - 1));
      assertThat(factorial.apply(5)).isEqualTo(120);
    }

    @Test
    @DisplayName("Y factorial handles the base case: 0! = 1")
    void yFactorialHandlesBaseCase() {
      Function<Integer, Integer> factorial = Y(f -> n -> n <= 1 ? 1 : n * f.apply(n - 1));
      assertThat(factorial.apply(0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Y fibonacci computes fib(10) = 55")
    void yFibonacciComputesTenthFibonacci() {
      Function<Integer, Integer> fib = Y(f -> n -> n <= 1 ? n : f.apply(n - 1) + f.apply(n - 2));
      assertThat(fib.apply(10)).isEqualTo(55);
    }

    @Test
    @DisplayName("Y fibonacci handles the base cases fib(0)=0 and fib(1)=1")
    void yFibonacciHandlesBaseCases() {
      Function<Integer, Integer> fib = Y(f -> n -> n <= 1 ? n : f.apply(n - 1) + f.apply(n - 2));
      assertThat(fib.apply(0)).isEqualTo(0);
      assertThat(fib.apply(1)).isEqualTo(1);
    }
  }
}
