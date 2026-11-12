package d021248.mytry.core.lang.fp.zipper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ListZipperTest {

  @Nested
  @DisplayName("Construction")
  class ConstructionTests {

    @Test
    @DisplayName("of(empty list) returns an empty Optional")
    void ofEmptyReturnsEmpty() {
      assertThat(ListZipper.of(List.<String>of())).isEmpty();
    }

    @Test
    @DisplayName("of(list) focuses on the first element")
    void ofFocusesOnFirst() {
      var z = ListZipper.of(List.of("a", "b", "c")).orElseThrow();
      assertThat(z.focus()).isEqualTo("a");
    }

    @Test
    @DisplayName("of(list) has no elements to the left of the first element")
    void ofHasEmptyLeft() {
      var z = ListZipper.of(List.of("a", "b")).orElseThrow();
      assertThat(z.left()).isEmpty();
    }

    @Test
    @DisplayName("toList on a fresh zipper reconstructs the original list")
    void toListRoundTrips() {
      var list = List.of("x", "y", "z");
      assertThat(ListZipper.of(list).orElseThrow().toList()).isEqualTo(list);
    }
  }

  @Nested
  @DisplayName("Navigation")
  class NavigationTests {

    @Test
    @DisplayName("moveRight focuses the second element")
    void moveRightFocusesSecond() {
      var z = ListZipper.of(List.of("a", "b", "c")).orElseThrow();
      assertThat(z.moveRight().orElseThrow().focus()).isEqualTo("b");
    }

    @Test
    @DisplayName("moveRight at end returns empty")
    void moveRightAtEndIsEmpty() {
      var z = ListZipper.of(List.of("only")).orElseThrow();
      assertThat(z.moveRight()).isEmpty();
    }

    @Test
    @DisplayName("moveLeft after moveRight returns to original focus")
    void moveLeftAfterMoveRight() {
      var z = ListZipper.of(List.of("a", "b", "c")).orElseThrow();
      assertThat(z.moveRight().orElseThrow().moveLeft().orElseThrow().focus()).isEqualTo("a");
    }

    @Test
    @DisplayName("moveLeft at start returns empty")
    void moveLeftAtStartIsEmpty() {
      var z = ListZipper.of(List.of("a", "b")).orElseThrow();
      assertThat(z.moveLeft()).isEmpty();
    }

    @Test
    @DisplayName("toList is preserved after moveRight then moveLeft")
    void toListPreservedAfterNavigation() {
      var list = List.of("a", "b", "c");
      var z = ListZipper.of(list).orElseThrow();
      var roundTripped = z.moveRight().orElseThrow().moveLeft().orElseThrow();
      assertThat(roundTripped.toList()).isEqualTo(list);
    }
  }

  @Nested
  @DisplayName("Modification")
  class ModificationTests {

    @Test
    @DisplayName("modify transforms the focused element")
    void modifyTransformsFocus() {
      var z = ListZipper.of(List.of("hello", "world")).orElseThrow();
      assertThat(z.modify(String::toUpperCase).focus()).isEqualTo("HELLO");
    }

    @Test
    @DisplayName("replace substitutes the focused element")
    void replaceSubstitutesFocus() {
      var z = ListZipper.of(List.of("old")).orElseThrow();
      assertThat(z.replace("new").focus()).isEqualTo("new");
    }

    @Test
    @DisplayName("insertRight adds an element after the focus in toList")
    void insertRightAddsAfterFocus() {
      var z = ListZipper.of(List.of("a", "c")).orElseThrow();
      assertThat(z.insertRight("b").toList()).containsExactly("a", "b", "c");
    }

    @Test
    @DisplayName("delete on a two-element list removes focus and refocuses on right")
    void deleteRemovesFocusAndMoveRight() {
      var z = ListZipper.of(List.of("del", "keep")).orElseThrow();
      var after = z.delete().orElseThrow();
      assertThat(after.focus()).isEqualTo("keep");
      assertThat(after.toList()).containsExactly("keep");
    }

    @Test
    @DisplayName("delete on a single-element list returns empty")
    void deleteOnSingleElementIsEmpty() {
      var z = ListZipper.of(List.of("only")).orElseThrow();
      assertThat(z.delete()).isEmpty();
    }
  }

  @Nested
  @DisplayName("Comonad operations")
  class ComonadTests {

    @Test
    @DisplayName("extract returns the focused element")
    void extractReturnsFocus() {
      var z = ListZipper.of(List.of("a", "b", "c")).orElseThrow().moveRight().orElseThrow();
      assertThat(z.extract()).isEqualTo("b");
    }

    @Test
    @DisplayName("extend with extract is the identity (comonad law)")
    void extendWithExtractIsIdentity() {
      var original = List.of("a", "b", "c");
      var z = ListZipper.of(original).orElseThrow();
      assertThat(z.extend(ListZipper::extract).toList()).isEqualTo(original);
    }

    @Test
    @DisplayName("extend annotates each element with its left-list size")
    void extendAnnotatesPosition() {
      var z = ListZipper.of(List.of("a", "b", "c")).orElseThrow();
      var positions = z.extend(ctx -> ctx.left().size());
      assertThat(positions.toList()).containsExactly(0, 1, 2);
    }

    @Test
    @DisplayName("map transforms every element")
    void mapTransformsAll() {
      var z = ListZipper.of(List.of("a", "b", "c")).orElseThrow();
      assertThat(z.map(String::toUpperCase).toList()).containsExactly("A", "B", "C");
    }
  }
}
