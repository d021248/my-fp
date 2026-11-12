package d021248.mytry.core.lang.fp.optics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AffineTraversalTest {

  // ── domain ────────────────────────────────────────────────────────────────

  sealed interface Shape permits Shape.Circle, Shape.Rect {
    record Circle(double radius) implements Shape {}

    record Rect(double width, double height) implements Shape {}
  }

  record Container(String name, Shape shape) {}

  private static final Prism<Shape, Double> circlePrism =
      Prism.of(
          s -> s instanceof Shape.Circle c ? Optional.of(c.radius()) : Optional.empty(),
          Shape.Circle::new);

  private static final Lens<Container, Shape> shapeLens =
      Lens.of(Container::shape, (c, s) -> new Container(c.name(), s));

  // AffineTraversal.of — Container → Optional<Double> (circle radius if present)
  private static final AffineTraversal<Container, Double> containerToRadius =
      AffineTraversal.fromLensAndPrism(shapeLens, circlePrism);

  // ── factory: AffineTraversal.of ───────────────────────────────────────────

  @Test
  @DisplayName("AffineTraversal.of: getOption returns focused value when present")
  void ofGetOptionPresent() {
    var at =
        AffineTraversal.<Shape, Double>of(
            s -> s instanceof Shape.Circle c ? Optional.of(c.radius()) : Optional.empty(),
            (s, r) -> s instanceof Shape.Circle ? new Shape.Circle(r) : s);

    assertThat(at.getOption(new Shape.Circle(3.0))).contains(3.0);
  }

  @Test
  @DisplayName("AffineTraversal.of: getOption returns empty when not matching")
  void ofGetOptionAbsent() {
    var at =
        AffineTraversal.<Shape, Double>of(
            s -> s instanceof Shape.Circle c ? Optional.of(c.radius()) : Optional.empty(),
            (s, r) -> s instanceof Shape.Circle ? new Shape.Circle(r) : s);

    assertThat(at.getOption(new Shape.Rect(2.0, 3.0))).isEmpty();
  }

  // ── fromLensAndPrism ──────────────────────────────────────────────────────

  @Test
  @DisplayName("fromLensAndPrism: getOption drills through lens then prism")
  void fromLensAndPrismGetOptionPresent() {
    var c = new Container("box", new Shape.Circle(5.0));
    assertThat(containerToRadius.getOption(c)).contains(5.0);
  }

  @Test
  @DisplayName("fromLensAndPrism: getOption returns empty when prism misses")
  void fromLensAndPrismGetOptionAbsent() {
    var c = new Container("box", new Shape.Rect(2.0, 3.0));
    assertThat(containerToRadius.getOption(c)).isEmpty();
  }

  @Test
  @DisplayName("fromLensAndPrism: set updates the focused value when prism matches")
  void fromLensAndPrismSetPresent() {
    var c = new Container("box", new Shape.Circle(5.0));
    var updated = containerToRadius.set(c, 9.0);
    assertThat(containerToRadius.getOption(updated)).contains(9.0);
    assertThat(updated.name()).isEqualTo("box");
  }

  @Test
  @DisplayName("fromLensAndPrism: set is a no-op when prism misses")
  void fromLensAndPrismSetAbsent() {
    var c = new Container("box", new Shape.Rect(2.0, 3.0));
    assertThat(containerToRadius.set(c, 99.0)).isEqualTo(c);
  }

  // ── modify ────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("modify: applies function to focused value when present")
  void modifyPresent() {
    var c = new Container("box", new Shape.Circle(4.0));
    var updated = containerToRadius.modify(c, r -> r * 2);
    assertThat(containerToRadius.getOption(updated)).contains(8.0);
  }

  @Test
  @DisplayName("modify: returns original when prism does not match")
  void modifyAbsent() {
    var c = new Container("box", new Shape.Rect(2.0, 3.0));
    assertThat(containerToRadius.modify(c, r -> r * 99)).isEqualTo(c);
  }

  // ── andThen composition ───────────────────────────────────────────────────

  @Test
  @DisplayName("andThen: composes two AffineTraversals — both must match for access")
  void andThenBothMatch() {
    // outer: Container → Optional<Shape> (always succeeds — fake AffineTraversal
    // over Lens)
    AffineTraversal<Container, Shape> containerShape =
        AffineTraversal.of(c -> Optional.of(c.shape()), (c, s) -> new Container(c.name(), s));
    // inner: Shape → Optional<Double> (circle radius)
    AffineTraversal<Shape, Double> shapeRadius =
        AffineTraversal.of(
            s -> s instanceof Shape.Circle cc ? Optional.of(cc.radius()) : Optional.empty(),
            (s, r) -> s instanceof Shape.Circle ? new Shape.Circle(r) : s);

    var composed = containerShape.andThen(shapeRadius);
    var circle = new Container("c", new Shape.Circle(7.0));
    var rect = new Container("r", new Shape.Rect(1.0, 2.0));

    assertThat(composed.getOption(circle)).contains(7.0);
    assertThat(composed.getOption(rect)).isEmpty();
  }

  @Test
  @DisplayName("andThen: set propagates through both levels when both match")
  void andThenSetBothMatch() {
    AffineTraversal<Container, Shape> containerShape =
        AffineTraversal.of(c -> Optional.of(c.shape()), (c, s) -> new Container(c.name(), s));
    AffineTraversal<Shape, Double> shapeRadius =
        AffineTraversal.of(
            s -> s instanceof Shape.Circle cc ? Optional.of(cc.radius()) : Optional.empty(),
            (s, r) -> s instanceof Shape.Circle ? new Shape.Circle(r) : s);

    var composed = containerShape.andThen(shapeRadius);
    var original = new Container("c", new Shape.Circle(7.0));
    var updated = composed.set(original, 42.0);

    assertThat(composed.getOption(updated)).contains(42.0);
  }
}
