package d021248.mytry.core.lang.fp.persistent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PersistentVectorTest {

  // ── factory ───────────────────────────────────────────────────────────────

  @Test
  @DisplayName("empty vector has size 0 and isEmpty()")
  void emptyVector() {
    var v = PersistentVector.<Integer>of();
    assertThat(v.size()).isZero();
    assertThat(v.isEmpty()).isTrue();
  }

  @Test
  @DisplayName("of(varargs) builds a vector with elements in order")
  void ofVarargs() {
    var v = PersistentVector.of(1, 2, 3, 4, 5);
    assertThat(v.size()).isEqualTo(5);
    assertThat(v.get(0)).isEqualTo(1);
    assertThat(v.get(4)).isEqualTo(5);
  }

  // ── get ───────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("get returns the correct element at each index")
  void getReturnsCorrectElement() {
    var v = PersistentVector.of("a", "b", "c");
    assertThat(v.get(0)).isEqualTo("a");
    assertThat(v.get(1)).isEqualTo("b");
    assertThat(v.get(2)).isEqualTo("c");
  }

  @Test
  @DisplayName("get throws IndexOutOfBoundsException for out-of-range index")
  void getThrowsForOutOfRange() {
    var v = PersistentVector.of(1, 2, 3);
    assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> v.get(3));
    assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> v.get(-1));
  }

  // ── append ────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("append returns a new vector; original is unchanged (persistence)")
  void appendPreservesOriginal() {
    var v1 = PersistentVector.of(1, 2, 3);
    var v2 = v1.append(4);

    assertThat(v1.size()).isEqualTo(3); // original unchanged
    assertThat(v2.size()).isEqualTo(4);
    assertThat(v2.get(3)).isEqualTo(4);
  }

  @Test
  @DisplayName("appending across a trie boundary (> 32 elements) works correctly")
  void appendAcrossTrieBoundary() {
    var count = 100;
    var v = PersistentVector.<Integer>of();
    for (int i = 0; i < count; i++) v = v.append(i);

    assertThat(v.size()).isEqualTo(count);
    for (int i = 0; i < count; i++) assertThat(v.get(i)).isEqualTo(i);
  }

  // ── set / update ──────────────────────────────────────────────────────────

  @Test
  @DisplayName("set returns a new vector with the element replaced; original unchanged")
  void setPreservesOriginal() {
    var v1 = PersistentVector.of(10, 20, 30);
    var v2 = v1.set(1, 99);

    assertThat(v1.get(1)).isEqualTo(20); // original unchanged
    assertThat(v2.get(1)).isEqualTo(99);
    assertThat(v2.get(0)).isEqualTo(10);
    assertThat(v2.get(2)).isEqualTo(30);
  }

  @Test
  @DisplayName("update applies a function to a single element; original unchanged")
  void update() {
    var v1 = PersistentVector.of(1, 2, 3);
    var v2 = v1.update(1, n -> n * 10);
    assertThat(v1.get(1)).isEqualTo(2);
    assertThat(v2.get(1)).isEqualTo(20);
  }

  // ── map / filter ──────────────────────────────────────────────────────────

  @Test
  @DisplayName("map applies a function to every element")
  void mapElements() {
    var v = PersistentVector.of(1, 2, 3, 4);
    var doubled = v.map(n -> n * 2);
    assertThat(doubled.stream().toList()).containsExactly(2, 4, 6, 8);
  }

  @Test
  @DisplayName("filter keeps only matching elements")
  void filterElements() {
    var v = PersistentVector.of(1, 2, 3, 4, 5, 6);
    var evens = v.filter(n -> n % 2 == 0);
    assertThat(evens.stream().toList()).containsExactly(2, 4, 6);
  }

  // ── iteration / stream ────────────────────────────────────────────────────

  @Test
  @DisplayName("iterator returns all elements in order")
  void iteratorOrder() {
    var v = PersistentVector.of(5, 4, 3, 2, 1);
    var list = v.stream().toList();
    assertThat(list).containsExactly(5, 4, 3, 2, 1);
  }

  @Test
  @DisplayName("large vector (1000 elements) get/set correctness")
  void largeVector() {
    int n = 1000;
    var v = PersistentVector.<Integer>of();
    for (int i = 0; i < n; i++) v = v.append(i);

    for (int i = 0; i < n; i++) assertThat(v.get(i)).isEqualTo(i);

    var modified = v.set(500, -1);
    assertThat(modified.get(500)).isEqualTo(-1);
    assertThat(v.get(500)).isEqualTo(500); // original unchanged
  }

  // ── equals / hashCode / toString ─────────────────────────────────────────

  @Test
  @DisplayName("two vectors with same elements are equal")
  void equalVectors() {
    var v1 = PersistentVector.of(1, 2, 3);
    var v2 = PersistentVector.of(1, 2, 3);
    assertThat(v1).isEqualTo(v2).hasSameHashCodeAs(v2);
  }

  @Test
  @DisplayName("toString shows elements in bracket notation")
  void toStringFormat() {
    assertThat(PersistentVector.of(1, 2, 3)).hasToString("[1, 2, 3]");
    assertThat(PersistentVector.<Integer>of()).hasToString("[]");
  }

  // ── structural sharing ────────────────────────────────────────────────────

  @Test
  @DisplayName("multiple versions from the same base vector are all valid simultaneously")
  void multipleVersionsCoexist() {
    var base = PersistentVector.of(1, 2, 3);
    var v1 = base.append(4);
    var v2 = base.append(99);
    var v3 = v1.set(0, -1);

    assertThat(base.stream().toList()).containsExactly(1, 2, 3);
    assertThat(v1.stream().toList()).containsExactly(1, 2, 3, 4);
    assertThat(v2.stream().toList()).containsExactly(1, 2, 3, 99);
    assertThat(v3.stream().toList()).containsExactly(-1, 2, 3, 4);
  }
}
