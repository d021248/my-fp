package d021248.mytry.core.lang.fp.typelevel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TypeLevelTest {

  // ── HList construction ─────────────────────────────────────────────────────

  @Nested
  @DisplayName("HList construction")
  class HListConstructionTests {

    @Test
    @DisplayName("nil() returns an HNil instance")
    void nilReturnsHNil() {
      assertThat(HList.nil()).isInstanceOf(HNil.class);
    }

    @Test
    @DisplayName("cons adds an element as the head")
    void consAddsHead() {
      var list = HList.nil().cons("hello");
      assertThat(list.head()).isEqualTo("hello");
    }

    @Test
    @DisplayName("cons twice produces the correct head")
    void consTwiceProducesCorrectHead() {
      var list = HList.nil().cons(42).cons("first");
      assertThat(list.head()).isEqualTo("first");
    }

    @Test
    @DisplayName("cons three times produces the correct head")
    void consThreeTimesCorrectHead() {
      var list = HList.nil().cons(true).cons(42).cons("first");
      assertThat(list.head()).isEqualTo("first");
    }
  }

  // ── Index zero ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Get.zeroIdx — index 0 extracts the head")
  class ZeroIdxTests {

    @Test
    @DisplayName("zeroIdx extracts the head of a single-element list")
    void zeroIdxExtractsSingleHead() {
      var list = HList.nil().cons("hello");
      Get<HCons<String, HNil>, Nat.Zero, String> idx0 = Get.zeroIdx();
      assertThat(idx0.extract(list)).isEqualTo("hello");
    }

    @Test
    @DisplayName("zeroIdx extracts the head of a two-element list")
    void zeroIdxExtractsFirstOfTwo() {
      var list = HList.nil().cons(42).cons("first");
      Get<HCons<String, HCons<Integer, HNil>>, Nat.Zero, String> idx0 = Get.zeroIdx();
      assertThat(idx0.extract(list)).isEqualTo("first");
    }

    @Test
    @DisplayName("zeroIdx extracts the head of a three-element list")
    void zeroIdxExtractsFirstOfThree() {
      HCons<String, HCons<Integer, HCons<Boolean, HNil>>> list =
          HList.nil().cons(true).cons(42).cons("Hello");
      Get<HCons<String, HCons<Integer, HCons<Boolean, HNil>>>, Nat.Zero, String> idx0 =
          Get.zeroIdx();
      assertThat(idx0.extract(list)).isEqualTo("Hello");
    }
  }

  // ── Successor indices ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("Get.succIdx — accesses deeper elements via induction")
  class SuccIdxTests {

    @Test
    @DisplayName("succIdx(zeroIdx) extracts the second element")
    void succIdxExtractsSecondElement() {
      var list = HList.nil().cons(42).cons("first");
      Get<HCons<String, HCons<Integer, HNil>>, Nat.Succ<Nat.Zero>, Integer> idx1 =
          Get.succIdx(Get.zeroIdx());
      assertThat(idx1.extract(list)).isEqualTo(42);
    }

    @Test
    @DisplayName("succIdx(succIdx(zeroIdx)) extracts the third element")
    void succIdxChainExtractsThirdElement() {
      HCons<String, HCons<Integer, HCons<Boolean, HNil>>> list =
          HList.nil().cons(true).cons(42).cons("Hello");
      Get<
              HCons<String, HCons<Integer, HCons<Boolean, HNil>>>,
              Nat.Succ<Nat.Succ<Nat.Zero>>,
              Boolean>
          idx2 = Get.succIdx(Get.succIdx(Get.zeroIdx()));
      assertThat(idx2.extract(list)).isTrue();
    }

    @Test
    @DisplayName("all three indices on ['Hello', 42, true] return correct typed values")
    void allThreeIndicesCorrect() {
      HCons<String, HCons<Integer, HCons<Boolean, HNil>>> list =
          HList.nil().cons(true).cons(42).cons("Hello");

      Get<HCons<String, HCons<Integer, HCons<Boolean, HNil>>>, Nat.Zero, String> idx0 =
          Get.zeroIdx();
      Get<HCons<String, HCons<Integer, HCons<Boolean, HNil>>>, Nat.Succ<Nat.Zero>, Integer> idx1 =
          Get.succIdx(Get.zeroIdx());
      Get<
              HCons<String, HCons<Integer, HCons<Boolean, HNil>>>,
              Nat.Succ<Nat.Succ<Nat.Zero>>,
              Boolean>
          idx2 = Get.succIdx(Get.succIdx(Get.zeroIdx()));

      String s = idx0.extract(list);
      Integer i = idx1.extract(list);
      Boolean b = idx2.extract(list);

      assertThat(s).isEqualTo("Hello");
      assertThat(i).isEqualTo(42);
      assertThat(b).isTrue();
    }
  }
}
