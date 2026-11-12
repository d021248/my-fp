package d021248.mytry.core.lang.fp.persistent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

/// Property-based tests for [PersistentVector].
///
/// Verifies structural invariants that must hold for all inputs:
/// size accounting, index accuracy, and structural sharing.
class PersistentVectorPropertyTest {

  @Property
  @Label("PersistentVector.append increases size by 1 and sets the last element")
  void appendIncreasesSize(@ForAll("smallIntLists") List<Integer> elements, @ForAll int extra) {
    var v = buildVector(elements);
    var v2 = v.append(extra);
    assertThat(v2.size()).isEqualTo(v.size() + 1);
    assertThat(v2.get(v.size())).isEqualTo(extra);
  }

  @Property
  @Label("PersistentVector.append does not modify the original vector")
  void appendPreservesOriginal(@ForAll("smallIntLists") List<Integer> elements, @ForAll int extra) {
    var v = buildVector(elements);
    v.append(extra);
    assertThat(v.size()).isEqualTo(elements.size());
    for (int i = 0; i < elements.size(); i++) {
      assertThat(v.get(i)).isEqualTo(elements.get(i));
    }
  }

  @Property
  @Label("PersistentVector.set changes only the targeted index; size is unchanged")
  void setChangesOnlyTargetIndex(
      @ForAll("nonEmptyIntLists") List<Integer> elements,
      @ForAll @IntRange(min = 0) int replacement) {
    var v = buildVector(elements);
    int idx = replacement % v.size();
    var v2 = v.set(idx, replacement);

    assertThat(v2.size()).isEqualTo(v.size());
    assertThat(v2.get(idx)).isEqualTo(replacement);
    for (int i = 0; i < v.size(); i++) {
      if (i != idx) assertThat(v2.get(i)).isEqualTo(v.get(i));
    }
  }

  @Provide
  Arbitrary<List<Integer>> smallIntLists() {
    return Arbitraries.integers().list().ofMaxSize(100);
  }

  @Provide
  Arbitrary<List<Integer>> nonEmptyIntLists() {
    return Arbitraries.integers().list().ofMinSize(1).ofMaxSize(100);
  }

  private PersistentVector<Integer> buildVector(List<Integer> elements) {
    var v = PersistentVector.<Integer>of();
    for (var e : elements) v = v.append(e);
    return v;
  }
}
