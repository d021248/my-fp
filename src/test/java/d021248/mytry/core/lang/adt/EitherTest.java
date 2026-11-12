package d021248.mytry.core.lang.adt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EitherTest {

  // ── construction & query ─────────────────────────────────────────────

  @Test
  @DisplayName("left() creates a Left")
  void leftCreation() {
    Either<String, Integer> e = Either.left("err");
    assertThat(e.isLeft()).isTrue();
    assertThat(e.isRight()).isFalse();
  }

  @Test
  @DisplayName("right() creates a Right")
  void rightCreation() {
    Either<String, Integer> e = Either.right(42);
    assertThat(e.isRight()).isTrue();
    assertThat(e.isLeft()).isFalse();
  }

  // ── map (right-biased) ───────────────────────────────────────────────

  @Nested
  @DisplayName("map()")
  class Map {
    @Test
    void mapsRight() {
      assertThat(Either.<String, Integer>right(5).map(n -> n * 2)).isEqualTo(Either.right(10));
    }

    @Test
    void leftPassesThrough() {
      assertThat(Either.<String, Integer>left("err").map(n -> n * 2)).isEqualTo(Either.left("err"));
    }
  }

  // ── flatMap ──────────────────────────────────────────────────────────

  @Nested
  @DisplayName("flatMap()")
  class FlatMap {
    @Test
    void chainsRight() {
      var result =
          Either.<String, Integer>right(10)
              .flatMap(n -> n > 5 ? Either.right(n) : Either.left("too small"));
      assertThat(result).isEqualTo(Either.right(10));
    }

    @Test
    void shortCircuitsOnLeft() {
      var result = Either.<String, Integer>left("err").flatMap(n -> Either.right(n * 2));
      assertThat(result).isEqualTo(Either.left("err"));
    }

    @Test
    void flatMapCanProduceLeft() {
      var result =
          Either.<String, Integer>right(3)
              .flatMap(n -> n > 5 ? Either.right(n) : Either.left("too small"));
      assertThat(result).isEqualTo(Either.left("too small"));
    }
  }

  // ── mapLeft ──────────────────────────────────────────────────────────

  @Test
  @DisplayName("mapLeft transforms the Left value")
  void mapLeft() {
    assertThat(Either.<String, Integer>left("err").mapLeft(String::toUpperCase))
        .isEqualTo(Either.left("ERR"));
  }

  @Test
  @DisplayName("mapLeft leaves Right unchanged")
  void mapLeftOnRight() {
    assertThat(Either.<String, Integer>right(42).mapLeft(String::toUpperCase))
        .isEqualTo(Either.right(42));
  }

  // ── fold ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("fold eliminates Left")
  void foldLeft() {
    var result = Either.<String, Integer>left("err").fold(l -> "Error: " + l, r -> "Value: " + r);
    assertThat(result).isEqualTo("Error: err");
  }

  @Test
  @DisplayName("fold eliminates Right")
  void foldRight() {
    var result = Either.<String, Integer>right(42).fold(l -> "Error: " + l, r -> "Value: " + r);
    assertThat(result).isEqualTo("Value: 42");
  }

  // ── swap ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("swap exchanges Left and Right")
  void swap() {
    assertThat(Either.<String, Integer>left("err").swap())
        .isEqualTo(Either.<Integer, String>right("err"));
    assertThat(Either.<String, Integer>right(42).swap())
        .isEqualTo(Either.<Integer, String>left(42));
  }

  // ── extraction ───────────────────────────────────────────────────────

  @Test
  @DisplayName("getRight returns value on Right")
  void getRight() {
    assertThat(Either.<String, Integer>right(42).getRight()).isEqualTo(42);
  }

  @Test
  @DisplayName("getRight throws on Left")
  void getRightOnLeft() {
    assertThatThrownBy(() -> Either.left("err").getRight())
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("orElse returns value on Right")
  void orElseRight() {
    assertThat(Either.<String, Integer>right(42).orElse(0)).isEqualTo(42);
  }

  @Test
  @DisplayName("orElse returns fallback on Left")
  void orElseLeft() {
    assertThat(Either.<String, Integer>left("err").orElse(0)).isEqualTo(0);
  }

  @Test
  @DisplayName("toOptional is present for Right")
  void toOptionalRight() {
    assertThat(Either.<String, Integer>right(42).toOptional()).contains(42);
  }

  @Test
  @DisplayName("toOptional is empty for Left")
  void toOptionalLeft() {
    assertThat(Either.<String, Integer>left("err").toOptional()).isEmpty();
  }

  // ── monad laws ───────────────────────────────────────────────────────

  @Nested
  @DisplayName("Monad laws")
  class MonadLaws {
    @Test
    @DisplayName("Left identity: right(a).flatMap(f) == f(a)")
    void leftIdentity() {
      var a = 5;
      var f =
          (java.util.function.Function<Integer, Either<String, String>>)
              n -> Either.right("val:" + n);
      assertThat(Either.<String, Integer>right(a).flatMap(f)).isEqualTo(f.apply(a));
    }

    @Test
    @DisplayName("Right identity: m.flatMap(Either::right) == m")
    void rightIdentity() {
      Either<String, Integer> m = Either.right(42);
      assertThat(m.flatMap(Either::right)).isEqualTo(m);
    }

    @Test
    @DisplayName("Associativity: m.flatMap(f).flatMap(g) == m.flatMap(a -> f(a).flatMap(g))")
    void associativity() {
      Either<String, Integer> m = Either.right(5);
      var f =
          (java.util.function.Function<Integer, Either<String, Integer>>) n -> Either.right(n + 1);
      var g =
          (java.util.function.Function<Integer, Either<String, Integer>>) n -> Either.right(n * 2);

      assertThat(m.flatMap(f).flatMap(g)).isEqualTo(m.flatMap(a -> f.apply(a).flatMap(g)));
    }
  }
}
