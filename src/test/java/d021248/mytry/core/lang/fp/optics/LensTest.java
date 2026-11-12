package d021248.mytry.core.lang.fp.optics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LensTest {

  // ── Test domain ───────────────────────────────────────────────────────────

  record Inner(String value, int count) {}

  record Outer(String name, Inner inner) {}

  // ── Lenses under test ─────────────────────────────────────────────────────

  static final Lens<Outer, Inner> outerInner =
      Lens.of(Outer::inner, (o, i) -> new Outer(o.name(), i));

  static final Lens<Inner, String> innerValue =
      Lens.of(Inner::value, (i, v) -> new Inner(v, i.count()));

  static final Lens<Inner, Integer> innerCount =
      Lens.of(Inner::count, (i, c) -> new Inner(i.value(), c));

  static final Lens<Outer, String> outerInnerValue = outerInner.andThen(innerValue);

  // ── get ───────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("get returns the focused field value")
  void getReturnsFocusedValue() {
    var outer = new Outer("root", new Inner("hello", 3));
    assertThat(outerInner.get(outer)).isEqualTo(new Inner("hello", 3));
    assertThat(innerValue.get(new Inner("hello", 3))).isEqualTo("hello");
  }

  // ── set ───────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("set replaces the focused field and leaves siblings unchanged")
  void setReplacesFocusedFieldOnly() {
    var outer = new Outer("root", new Inner("hello", 3));
    var updated = outerInner.set(outer, new Inner("world", 7));
    assertThat(updated.name()).isEqualTo("root"); // unchanged
    assertThat(updated.inner()).isEqualTo(new Inner("world", 7));
  }

  // ── modify ────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("modify applies a function to the focused value")
  void modifyTransformsFocusedValue() {
    var inner = new Inner("hello", 3);
    var modified = innerValue.modify(inner, String::toUpperCase);
    assertThat(modified.value()).isEqualTo("HELLO");
    assertThat(modified.count()).isEqualTo(3); // unchanged
  }

  @Test
  @DisplayName("modify with identity function is a no-op")
  void modifyWithIdentityIsNoop() {
    var inner = new Inner("hello", 3);
    assertThat(innerValue.modify(inner, v -> v)).isEqualTo(inner);
  }

  // ── andThen (composition) ─────────────────────────────────────────────────

  @Nested
  @DisplayName("andThen composes two lenses")
  class Composition {

    @Test
    @DisplayName("composed get drills through both levels")
    void composedGetDrillsThrough() {
      var outer = new Outer("root", new Inner("deep", 5));
      assertThat(outerInnerValue.get(outer)).isEqualTo("deep");
    }

    @Test
    @DisplayName("composed set updates only the target leaf, leaving all other fields unchanged")
    void composedSetUpdatesLeafOnly() {
      var outer = new Outer("root", new Inner("old", 5));
      var updated = outerInnerValue.set(outer, "new");
      assertThat(updated.name()).isEqualTo("root"); // unchanged
      assertThat(updated.inner().count()).isEqualTo(5); // unchanged
      assertThat(updated.inner().value()).isEqualTo("new"); // updated
    }

    @Test
    @DisplayName("composed modify applies the function at the leaf")
    void composedModifyAppliesAtLeaf() {
      var outer = new Outer("root", new Inner("hello", 5));
      var updated = outerInnerValue.modify(outer, String::toUpperCase);
      assertThat(outerInnerValue.get(updated)).isEqualTo("HELLO");
      assertThat(updated.inner().count()).isEqualTo(5); // sibling unchanged
    }
  }

  // ── Lens laws ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Lens laws")
  class LensLaws {

    @Test
    @DisplayName("get-set: get(set(s, a)) == a")
    void getSetLaw() {
      var inner = new Inner("initial", 1);
      var result = innerValue.get(innerValue.set(inner, "new"));
      assertThat(result).isEqualTo("new");
    }

    @Test
    @DisplayName("set-get: set(s, get(s)) == s")
    void setGetLaw() {
      var inner = new Inner("hello", 3);
      var result = innerValue.set(inner, innerValue.get(inner));
      assertThat(result).isEqualTo(inner);
    }

    @Test
    @DisplayName("set-set: set(set(s, a1), a2) == set(s, a2) — last write wins")
    void setSetLaw() {
      var inner = new Inner("original", 1);
      var result1 = innerValue.set(innerValue.set(inner, "first"), "second");
      var result2 = innerValue.set(inner, "second");
      assertThat(result1).isEqualTo(result2);
    }
  }
}
