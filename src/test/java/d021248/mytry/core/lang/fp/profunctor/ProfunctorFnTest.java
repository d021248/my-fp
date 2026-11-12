package d021248.mytry.core.lang.fp.profunctor;

import static org.assertj.core.api.Assertions.assertThat;

import d021248.mytry.core.lang.adt.Either;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProfunctorFnTest {

  @Nested
  @DisplayName("ProfunctorFn.dimap — transforms input and output")
  class DimapTests {

    @Test
    @DisplayName("dimap(identity, identity) does not change behaviour")
    void dimapIdentityIdentity() {
      ProfunctorFn<String, Integer> len = ProfunctorFn.of(String::length);
      var dimapped = len.dimap(Function.identity(), Function.identity());
      assertThat(dimapped.apply("hello")).isEqualTo(5);
    }

    @Test
    @DisplayName("dimap pre-processes input before applying the function")
    void dimapPreProcessesInput() {
      ProfunctorFn<String, Integer> len = ProfunctorFn.of(String::length);
      var trimmedLen = len.dimap(String::trim, Function.identity());
      assertThat(trimmedLen.apply("  hi  ")).isEqualTo(2);
    }

    @Test
    @DisplayName("dimap post-processes output after applying the function")
    void dimapPostProcessesOutput() {
      ProfunctorFn<String, Integer> len = ProfunctorFn.of(String::length);
      var doubled = len.dimap(Function.identity(), n -> n * 2);
      assertThat(doubled.apply("hello")).isEqualTo(10);
    }

    @Test
    @DisplayName("dimap applies both pre- and post-processing")
    void dimapAppliesBoth() {
      ProfunctorFn<String, Integer> len = ProfunctorFn.of(String::length);
      var result = len.dimap(String::trim, n -> n * 2);
      assertThat(result.apply("  hi  ")).isEqualTo(4);
    }
  }

  @Nested
  @DisplayName("ProfunctorFn.contramap — transforms only the input")
  class ContramapTests {

    @Test
    @DisplayName("contramap pre-processes input before applying the function")
    void contramapPreProcesses() {
      ProfunctorFn<String, Integer> len = ProfunctorFn.of(String::length);
      Function<String, String> toLower = String::toLowerCase;
      var lowerLen = len.contramap(toLower);
      assertThat(lowerLen.apply("HELLO")).isEqualTo(5);
    }

    @Test
    @DisplayName("contramap with identity does not change behaviour")
    void contramapIdentityUnchanged() {
      ProfunctorFn<String, Integer> len = ProfunctorFn.of(String::length);
      assertThat(len.contramap(Function.identity()).apply("hi")).isEqualTo(2);
    }
  }

  @Nested
  @DisplayName("ProfunctorFn.rmap — transforms only the output")
  class RmapTests {

    @Test
    @DisplayName("rmap post-processes the output")
    void rmapPostProcesses() {
      ProfunctorFn<String, Integer> len = ProfunctorFn.of(String::length);
      var lenStr = len.rmap(n -> "len=" + n);
      assertThat(lenStr.apply("world")).isEqualTo("len=5");
    }

    @Test
    @DisplayName("rmap with identity does not change behaviour")
    void rmapIdentityUnchanged() {
      ProfunctorFn<String, Integer> len = ProfunctorFn.of(String::length);
      assertThat(len.rmap(Function.identity()).apply("abc")).isEqualTo(3);
    }
  }

  @Nested
  @DisplayName("StrongFn.first — lifts function to operate on first pair element")
  class StrongFirstTests {

    @Test
    @DisplayName("first transforms the first component of a pair")
    void firstTransformsFirstComponent() {
      var upper = StrongFn.<String, String>of(String::toUpperCase);
      var result = upper.first().apply(new StrongFn.Pair<>("hello", 42));
      assertThat(result.first()).isEqualTo("HELLO");
    }

    @Test
    @DisplayName("first leaves the second component unchanged")
    void firstLeavesSecondUnchanged() {
      var upper = StrongFn.<String, String>of(String::toUpperCase);
      var result = upper.first().apply(new StrongFn.Pair<>("hello", 99));
      assertThat(result.second()).isEqualTo(99);
    }
  }

  @Nested
  @DisplayName("StrongFn.second — lifts function to operate on second pair element")
  class StrongSecondTests {

    @Test
    @DisplayName("second transforms the second component of a pair")
    void secondTransformsSecondComponent() {
      var upper = StrongFn.<String, String>of(String::toUpperCase);
      var result = upper.<Integer>second().apply(new StrongFn.Pair<>(42, "world"));
      assertThat(result.second()).isEqualTo("WORLD");
    }

    @Test
    @DisplayName("second leaves the first component unchanged")
    void secondLeavesFirstUnchanged() {
      var upper = StrongFn.<String, String>of(String::toUpperCase);
      var result = upper.<Integer>second().apply(new StrongFn.Pair<>(42, "world"));
      assertThat(result.first()).isEqualTo(42);
    }
  }

  @Nested
  @DisplayName("ChoiceFn.left — lifts function to operate on left branch of Either")
  class ChoiceLeftTests {

    private final ChoiceFn<String, Integer> len = ChoiceFn.of(String::length);

    @Test
    @DisplayName("left transforms the Left branch")
    void leftTransformsLeftBranch() {
      var lifted = len.left();
      var result = lifted.apply(Either.left("hello"));
      assertThat(result).isEqualTo(Either.left(5));
    }

    @Test
    @DisplayName("left passes Right values through unchanged")
    void leftPassesRightThrough() {
      var lifted = len.<Double>left();
      var input = Either.<String, Double>right(3.14);
      assertThat(lifted.apply(input)).isEqualTo(Either.right(3.14));
    }
  }

  @Nested
  @DisplayName("ChoiceFn.right — lifts function to operate on right branch of Either")
  class ChoiceRightTests {

    private final ChoiceFn<String, Integer> len = ChoiceFn.of(String::length);

    @Test
    @DisplayName("right transforms the Right branch")
    void rightTransformsRightBranch() {
      var lifted = len.<Double>right();
      var result = lifted.apply(Either.right("world"));
      assertThat(result).isEqualTo(Either.right(5));
    }

    @Test
    @DisplayName("right passes Left values through unchanged")
    void rightPassesLeftThrough() {
      var lifted = len.<Double>right();
      var input = Either.<Double, String>left(2.71);
      assertThat(lifted.apply(input)).isEqualTo(Either.left(2.71));
    }
  }

  @Nested
  @DisplayName("Either — sum type used by ChoiceFn")
  class EitherTests {

    @Test
    @DisplayName("fold applies left function to Left branch")
    void foldLeft() {
      Either<String, Integer> e = Either.left("hi");
      assertThat(e.fold(String::length, n -> n * 10)).isEqualTo(2);
    }

    @Test
    @DisplayName("fold applies right function to Right branch")
    void foldRight() {
      Either<String, Integer> e = Either.right(7);
      assertThat(e.fold(String::length, n -> n * 10)).isEqualTo(70);
    }

    @Test
    @DisplayName("isLeft and isRight are mutually exclusive")
    void isLeftIsRight() {
      assertThat(Either.left("x").isLeft()).isTrue();
      assertThat(Either.left("x").isRight()).isFalse();
      assertThat(Either.right(1).isRight()).isTrue();
      assertThat(Either.right(1).isLeft()).isFalse();
    }
  }
}
