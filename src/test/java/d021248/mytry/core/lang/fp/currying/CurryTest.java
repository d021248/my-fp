package d021248.mytry.core.lang.fp.currying;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CurryTest {

  // -------------------------------------------------------------------------
  // Fn2: partial
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Fn2.partial fixes the first argument, producing a Function<B,R>")
  void fn2PartialFixesFirstArg() {
    Curry.Fn2<Integer, Integer, Integer> add = Curry.fn2(Integer::sum);
    Function<Integer, Integer> add10 = add.partial(10);
    assertThat(add10.apply(5)).isEqualTo(15);
    assertThat(add10.apply(0)).isEqualTo(10);
  }

  // -------------------------------------------------------------------------
  // Fn2: curried
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Fn2.curried produces the same result as direct application")
  void fn2CurriedMatchesDirectApplication() {
    Curry.Fn2<String, Integer, String> repeat = Curry.fn2(String::repeat);
    var curried = repeat.curried();
    assertThat(curried.apply("ab").apply(3)).isEqualTo(repeat.apply("ab", 3));
  }

  // -------------------------------------------------------------------------
  // Fn2: flip
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Fn2.flip swaps the two arguments")
  void fn2FlipSwapsArguments() {
    Curry.Fn2<String, String, String> concat = Curry.fn2((a, b) -> a + b);
    assertThat(concat.apply("hello", " world")).isEqualTo("hello world");
    assertThat(concat.flip().apply(" world", "hello")).isEqualTo("hello world");
  }

  // -------------------------------------------------------------------------
  // Fn3: partial one arg
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Fn3.partial with one arg fixes the first argument")
  void fn3PartialOneArgFixesFirst() {
    Curry.Fn3<Integer, Integer, Integer, Integer> clamp =
        Curry.fn3((lo, hi, x) -> Math.max(lo, Math.min(hi, x)));
    var clampFrom0 = clamp.partial(0);
    assertThat(clampFrom0.apply(100, -5)).isEqualTo(0);
    assertThat(clampFrom0.apply(100, 50)).isEqualTo(50);
    assertThat(clampFrom0.apply(100, 150)).isEqualTo(100);
  }

  // -------------------------------------------------------------------------
  // Fn3: partial two args
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Fn3.partial with two args produces a single-arg clamp function")
  void fn3PartialTwoArgsProducesExpectedClip() {
    Curry.Fn3<Integer, Integer, Integer, Integer> clamp =
        Curry.fn3((lo, hi, x) -> Math.max(lo, Math.min(hi, x)));
    Function<Integer, Integer> clip = clamp.partial(0, 100);

    assertThat(clip.apply(-5)).isEqualTo(0);
    assertThat(clip.apply(0)).isEqualTo(0);
    assertThat(clip.apply(50)).isEqualTo(50);
    assertThat(clip.apply(100)).isEqualTo(100);
    assertThat(clip.apply(150)).isEqualTo(100);
  }

  // -------------------------------------------------------------------------
  // Fn3: curried
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Fn3.curried matches direct application with three arguments")
  void fn3CurriedMatchesDirectApplication() {
    Curry.Fn3<Integer, Integer, Integer, Integer> sum3 = Curry.fn3((a, b, c) -> a + b + c);
    int direct = sum3.apply(1, 2, 3);
    int curried = sum3.curried().apply(1).apply(2).apply(3);
    assertThat(curried).isEqualTo(direct);
  }

  // -------------------------------------------------------------------------
  // Composition via andThen
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Partially-applied Fn3 composes with andThen for normalisation pipeline")
  void partialApplicationComposesWithAndThen() {
    Curry.Fn3<Integer, Integer, Integer, Integer> clamp =
        Curry.fn3((lo, hi, x) -> Math.max(lo, Math.min(hi, x)));
    // normalise: clamp 0..100 then scale to 0..255
    Function<Integer, Integer> normalise = clamp.partial(0, 100).andThen(x -> x * 255 / 100);

    assertThat(normalise.apply(-10)).isEqualTo(0);
    assertThat(normalise.apply(0)).isEqualTo(0);
    assertThat(normalise.apply(50)).isEqualTo(127);
    assertThat(normalise.apply(100)).isEqualTo(255);
    assertThat(normalise.apply(200)).isEqualTo(255);
  }
}
