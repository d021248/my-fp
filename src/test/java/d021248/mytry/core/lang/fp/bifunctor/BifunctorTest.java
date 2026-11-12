package d021248.mytry.core.lang.fp.bifunctor;

import static org.assertj.core.api.Assertions.assertThat;

import d021248.mytry.core.lang.fp.bifunctor.Bifunctor.Either;
import d021248.mytry.core.lang.fp.bifunctor.Bifunctor.Pair;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BifunctorTest {

  // ── Pair ──────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Pair (product type)")
  class PairTests {

    @Test
    @DisplayName("bimap transforms both components independently")
    void bimapTransformsBothComponents() {
      var pair = new Pair<>("hello", 3);
      var result = pair.bimap(String::toUpperCase, n -> n * 2);
      assertThat(result.first()).isEqualTo("HELLO");
      assertThat(result.second()).isEqualTo(6);
    }

    @Test
    @DisplayName("bimap returns a Pair (not just Bifunctor)")
    void bimapReturnsPairType() {
      var pair = new Pair<>("a", 1);
      Bifunctor<Integer, String> result = pair.bimap(String::length, Object::toString);
      assertThat(result).isInstanceOf(Pair.class);
    }

    @Test
    @DisplayName("mapFirst transforms only the first component")
    void mapFirstTransformsOnlyFirst() {
      var pair = new Pair<>("hello", 42);
      var result = pair.mapFirst(String::length);
      assertThat(result).isInstanceOf(Pair.class);
      var asPair = (Pair<Integer, Integer>) result;
      assertThat(asPair.first()).isEqualTo(5);
      assertThat(asPair.second()).isEqualTo(42);
    }

    @Test
    @DisplayName("mapSecond transforms only the second component")
    void mapSecondTransformsOnlySecond() {
      var pair = new Pair<>("hello", 4);
      var result = pair.mapSecond(n -> n * 3);
      assertThat(result).isInstanceOf(Pair.class);
      var asPair = (Pair<String, Integer>) result;
      assertThat(asPair.first()).isEqualTo("hello");
      assertThat(asPair.second()).isEqualTo(12);
    }

    @Nested
    @DisplayName("Pair Bifunctor laws")
    class PairLaws {

      @Test
      @DisplayName("Identity law: bimap(identity, identity) == identity")
      void identityLaw() {
        var pair = new Pair<>("x", 7);
        var result = pair.bimap(Function.identity(), Function.identity());
        assertThat(result).isEqualTo(pair);
      }

      @Test
      @DisplayName("Composition law: bimap(f.andThen(h), g.andThen(k)) == bimap(f,g).bimap(h,k)")
      void compositionLaw() {
        var pair = new Pair<>("hello", 3);

        Function<String, String> f = String::toUpperCase;
        Function<String, Integer> h = String::length;
        Function<Integer, Integer> g = n -> n * 2;
        Function<Integer, String> k = Object::toString;

        var direct = pair.bimap(f.andThen(h), g.andThen(k));
        var composed = pair.bimap(f, g).bimap(h, k);
        assertThat(direct).isEqualTo(composed);
      }
    }
  }

  // ── Either ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Either (sum type)")
  class EitherTests {

    @Nested
    @DisplayName("Either.Left")
    class LeftTests {

      @Test
      @DisplayName("bimap applies f to the left value and ignores g")
      void bimapAppliesFToLeft() {
        Either<String, Integer> left = Either.left("error");
        var result = left.bimap(String::toUpperCase, n -> n * 99);
        assertThat(result).isInstanceOf(Either.Left.class);
        var asLeft = (Either.Left<String, Integer>) result;
        assertThat(asLeft.value()).isEqualTo("ERROR");
      }

      @Test
      @DisplayName("mapFirst transforms the left value")
      void mapFirstTransformsLeft() {
        Either<String, Integer> left = Either.left("err");
        var result = left.mapFirst(String::length);
        assertThat(result).isInstanceOf(Either.Left.class);
        var asLeft = (Either.Left<Integer, Integer>) result;
        assertThat(asLeft.value()).isEqualTo(3);
      }

      @Test
      @DisplayName("mapSecond is a no-op on Left")
      void mapSecondIsNoopOnLeft() {
        Either<String, Integer> left = Either.left("error");
        var result = left.mapSecond(n -> n * 99);
        assertThat(result).isInstanceOf(Either.Left.class);
        var asLeft = (Either.Left<String, Integer>) result;
        assertThat(asLeft.value()).isEqualTo("error");
      }

      @Test
      @DisplayName("isLeft returns true; isRight returns false")
      void leftPredicates() {
        Either<String, Integer> left = Either.left("x");
        assertThat(left.isLeft()).isTrue();
        assertThat(left.isRight()).isFalse();
      }

      @Test
      @DisplayName("Left identity law: bimap(identity, identity) == identity")
      void leftIdentityLaw() {
        Either<String, Integer> left = Either.left("value");
        var result = left.bimap(Function.identity(), Function.identity());
        assertThat(result).isEqualTo(left);
      }

      @Test
      @DisplayName(
          "Left composition law: bimap(f.andThen(h), g.andThen(k)) == bimap(f,g).bimap(h,k)")
      void leftCompositionLaw() {
        Either<String, Integer> left = Either.left("hello");
        Function<String, String> f = String::toUpperCase;
        Function<String, Integer> h = String::length;
        Function<Integer, Integer> g = n -> n * 2;
        Function<Integer, String> k = Object::toString;

        var direct = left.bimap(f.andThen(h), g.andThen(k));
        var composed = left.bimap(f, g).bimap(h, k);
        assertThat(direct).isEqualTo(composed);
      }
    }

    @Nested
    @DisplayName("Either.Right")
    class RightTests {

      @Test
      @DisplayName("bimap applies g to the right value and ignores f")
      void bimapAppliesGToRight() {
        Either<String, Integer> right = Either.right(21);
        var result = right.bimap(s -> s + "!", n -> n * 2);
        assertThat(result).isInstanceOf(Either.Right.class);
        var asRight = (Either.Right<String, Integer>) result;
        assertThat(asRight.value()).isEqualTo(42);
      }

      @Test
      @DisplayName("mapSecond transforms the right value")
      void mapSecondTransformsRight() {
        Either<String, Integer> right = Either.right(5);
        var result = right.mapSecond(n -> n + 10);
        assertThat(result).isInstanceOf(Either.Right.class);
        var asRight = (Either.Right<String, Integer>) result;
        assertThat(asRight.value()).isEqualTo(15);
      }

      @Test
      @DisplayName("mapFirst is a no-op on Right")
      void mapFirstIsNoopOnRight() {
        Either<String, Integer> right = Either.right(99);
        var result = right.mapFirst(s -> s + "!");
        assertThat(result).isInstanceOf(Either.Right.class);
        var asRight = (Either.Right<String, Integer>) result;
        assertThat(asRight.value()).isEqualTo(99);
      }

      @Test
      @DisplayName("isRight returns true; isLeft returns false")
      void rightPredicates() {
        Either<String, Integer> right = Either.right(1);
        assertThat(right.isRight()).isTrue();
        assertThat(right.isLeft()).isFalse();
      }

      @Test
      @DisplayName("Right identity law: bimap(identity, identity) == identity")
      void rightIdentityLaw() {
        Either<String, Integer> right = Either.right(42);
        var result = right.bimap(Function.identity(), Function.identity());
        assertThat(result).isEqualTo(right);
      }

      @Test
      @DisplayName(
          "Right composition law: bimap(f.andThen(h), g.andThen(k)) == bimap(f,g).bimap(h,k)")
      void rightCompositionLaw() {
        Either<String, Integer> right = Either.right(3);
        Function<String, String> f = String::toUpperCase;
        Function<String, Integer> h = String::length;
        Function<Integer, Integer> g = n -> n * 2;
        Function<Integer, String> k = Object::toString;

        var direct = right.bimap(f.andThen(h), g.andThen(k));
        var composed = right.bimap(f, g).bimap(h, k);
        assertThat(direct).isEqualTo(composed);
      }
    }
  }
}
