package d021248.mytry.core.lang.fp.optics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OpticsTest {

  // ── domain ────────────────────────────────────────────────────────────────

  sealed interface Shape permits Shape.Circle, Shape.Rect {
    record Circle(double radius) implements Shape {}

    record Rect(double width, double height) implements Shape {}
  }

  sealed interface Outer permits Outer.Wrap, Outer.Empty {
    record Wrap(Shape shape) implements Outer {}

    record Empty() implements Outer {}
  }

  private static final Prism<Shape, Double> circlePrism =
      Prism.of(
          s -> s instanceof Shape.Circle c ? Optional.of(c.radius()) : Optional.empty(),
          Shape.Circle::new);

  private static final Prism<Shape, Shape.Rect> rectPrism =
      Prism.of(s -> s instanceof Shape.Rect r ? Optional.of(r) : Optional.empty(), r -> r);

  private static final Prism<Outer, Shape> wrapPrism =
      Prism.of(
          o -> o instanceof Outer.Wrap w ? Optional.of(w.shape()) : Optional.empty(),
          s -> new Outer.Wrap(s));

  // ── Prism laws ────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Prism law 1: getOption(review(a)) == Optional.of(a)")
  void prismLaw1ReviewThenGet() {
    var a = 5.0;
    assertThat(circlePrism.getOption(circlePrism.review(a))).isEqualTo(Optional.of(a));
  }

  @Test
  @DisplayName("Prism law 2: if getOption(s) == Some(a), then review(a) == s")
  void prismLaw2GetThenReview() {
    var s = new Shape.Circle(3.0);
    var optA = circlePrism.getOption(s);
    assertThat(optA).isPresent();
    assertThat(circlePrism.review(optA.get())).isEqualTo(s);
  }

  // ── Prism operations ──────────────────────────────────────────────────────

  @Test
  @DisplayName("Prism: getOption returns empty for non-matching variant")
  void prismMissReturnsEmpty() {
    assertThat(circlePrism.getOption(new Shape.Rect(2, 3))).isEmpty();
  }

  @Test
  @DisplayName("Prism: modify transforms the focused value")
  void prismModifyTransforms() {
    Shape.Circle c = new Shape.Circle(4.0);
    var result = circlePrism.modify(c, r -> r * 2);
    assertThat(circlePrism.getOption(result)).contains(8.0);
  }

  @Test
  @DisplayName("Prism: modify leaves non-matching variants unchanged")
  void prismModifyLeavesOtherVariantsAlone() {
    var rect = new Shape.Rect(2.0, 3.0);
    assertThat(circlePrism.modify(rect, r -> r * 99)).isEqualTo(rect);
  }

  @Test
  @DisplayName("Prism: matches returns true only for the targeted variant")
  void prismMatches() {
    assertThat(circlePrism.matches(new Shape.Circle(1.0))).isTrue();
    assertThat(circlePrism.matches(new Shape.Rect(1.0, 2.0))).isFalse();
  }

  @Test
  @DisplayName("Prism: rectPrism extracts Rect fields and review round-trips")
  void rectPrismRoundTrip() {
    var rect = new Shape.Rect(4.0, 5.0);
    assertThat(rectPrism.getOption(rect)).contains(rect);
    assertThat(rectPrism.getOption(new Shape.Circle(1.0))).isEmpty();
  }

  // ── Prism composition ─────────────────────────────────────────────────────

  @Test
  @DisplayName("Composed prisms focus into nested variants")
  void prismComposition() {
    var composed = wrapPrism.andThen(circlePrism);
    Outer outer = new Outer.Wrap(new Shape.Circle(7.0));

    assertThat(composed.getOption(outer)).contains(7.0);
    assertThat(composed.getOption(new Outer.Empty())).isEmpty();
  }

  // ── Traversal ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Traversal: getAll returns all elements of a list")
  void traversalGetAll() {
    var t = Traversal.<Integer>forList();
    assertThat(t.getAll(List.of(1, 2, 3))).containsExactly(1, 2, 3);
  }

  @Test
  @DisplayName("Traversal: modify transforms every element")
  void traversalModifyAll() {
    var t = Traversal.<Integer>forList();
    assertThat(t.modify(List.of(1, 2, 3), n -> n * 2)).containsExactly(2, 4, 6);
  }

  @Test
  @DisplayName("Traversal: set replaces all elements with the given value")
  void traversalSetAll() {
    var t = Traversal.<String>forList();
    assertThat(t.set(List.of("a", "b", "c"), "x")).containsExactly("x", "x", "x");
  }

  @Test
  @DisplayName("Traversal purity law: modify with identity returns original structure")
  void traversalPurityLaw() {
    var t = Traversal.<Integer>forList();
    var list = List.of(1, 2, 3);
    assertThat(t.modify(list, n -> n)).isEqualTo(list);
  }

  @Test
  @DisplayName("Traversal composition: nested list of lists — modify all inner elements")
  void traversalComposition() {
    var outer = Traversal.<List<Integer>>forList();
    var inner = Traversal.<Integer>forList();
    var nested = outer.andThen(inner);

    var data = List.of(List.of(1, 2), List.of(3, 4));
    assertThat(nested.getAll(data)).containsExactly(1, 2, 3, 4);
    assertThat(nested.modify(data, n -> n * 10))
        .isEqualTo(List.of(List.of(10, 20), List.of(30, 40)));
  }
}
