package d021248.mytry.core.lang.fp.recursionschemes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RecursionSchemesTest {

  // ─── Shared algebras and coalgebras ────────────────────────────────────────

  private static int evalAlgebra(RecursionSchemes.ExprF<Integer> e) {
    return switch (e) {
      case RecursionSchemes.ExprF.Num<Integer> n -> n.value();
      case RecursionSchemes.ExprF.Add<Integer> a -> a.left() + a.right();
      case RecursionSchemes.ExprF.Mul<Integer> m -> m.left() * m.right();
    };
  }

  private static String prettyAlgebra(RecursionSchemes.ExprF<String> e) {
    return switch (e) {
      case RecursionSchemes.ExprF.Num<String> n -> String.valueOf(n.value());
      case RecursionSchemes.ExprF.Add<String> a -> "(%s+%s)".formatted(a.left(), a.right());
      case RecursionSchemes.ExprF.Mul<String> m -> "(%s*%s)".formatted(m.left(), m.right());
    };
  }

  // Fibonacci coalgebra: fib(n) = fib(n-1) + fib(n-2)
  // Both seeds (n-1, n-2) strictly decrease → terminates for all n >= 1.
  private static RecursionSchemes.ExprF<Integer> fibCoalgebra(int n) {
    if (n <= 2) return new RecursionSchemes.ExprF.Num<>(1);
    return new RecursionSchemes.ExprF.Add<>(n - 1, n - 2);
  }

  // ─── Catamorphism ──────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Catamorphism (cata)")
  class CataTests {

    @Test
    @DisplayName("evaluates a numeric leaf to its value")
    void cataEvaluatesNum() {
      assertThat(
              RecursionSchemes.cata(RecursionSchemesTest::evalAlgebra, RecursionSchemes.Fix.num(7)))
          .isEqualTo(7);
    }

    @Test
    @DisplayName("evaluates addition: 2 + 3 = 5")
    void cataEvaluatesAdd() {
      var expr = RecursionSchemes.Fix.add(RecursionSchemes.Fix.num(2), RecursionSchemes.Fix.num(3));
      assertThat(RecursionSchemes.cata(RecursionSchemesTest::evalAlgebra, expr)).isEqualTo(5);
    }

    @Test
    @DisplayName("evaluates nested expression (2*3)+4 = 10")
    void cataEvaluatesNested() {
      var expr =
          RecursionSchemes.Fix.add(
              RecursionSchemes.Fix.mul(RecursionSchemes.Fix.num(2), RecursionSchemes.Fix.num(3)),
              RecursionSchemes.Fix.num(4));
      assertThat(RecursionSchemes.cata(RecursionSchemesTest::evalAlgebra, expr)).isEqualTo(10);
    }

    @Test
    @DisplayName("pretty-prints (1+2) as a string")
    void cataPrettyPrints() {
      var expr = RecursionSchemes.Fix.add(RecursionSchemes.Fix.num(1), RecursionSchemes.Fix.num(2));
      assertThat(RecursionSchemes.cata(RecursionSchemesTest::prettyAlgebra, expr))
          .isEqualTo("(1+2)");
    }

    @Test
    @DisplayName("pretty-prints (2*3) as a string")
    void cataPrettyPrintsMul() {
      var expr = RecursionSchemes.Fix.mul(RecursionSchemes.Fix.num(2), RecursionSchemes.Fix.num(3));
      assertThat(RecursionSchemes.cata(RecursionSchemesTest::prettyAlgebra, expr))
          .isEqualTo("(2*3)");
    }
  }

  // ─── Anamorphism ───────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Anamorphism (ana)")
  class AnaTests {

    @Test
    @DisplayName("ana unfolds fib(1) to a single leaf with value 1")
    void anaUnfoldsFib1ToLeaf() {
      var tree = RecursionSchemes.ana(RecursionSchemesTest::fibCoalgebra, 1);
      assertThat(RecursionSchemes.cata(RecursionSchemesTest::evalAlgebra, tree)).isEqualTo(1);
    }

    @Test
    @DisplayName("ana unfolds fib(5) to a tree that evaluates to 5")
    void anaUnfoldsFib5() {
      var tree = RecursionSchemes.ana(RecursionSchemesTest::fibCoalgebra, 5);
      assertThat(RecursionSchemes.cata(RecursionSchemesTest::evalAlgebra, tree)).isEqualTo(5);
    }
  }

  // ─── Hylomorphism ──────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Hylomorphism (hylo)")
  class HyloTests {

    @Test
    @DisplayName("hylo computes fib(1) = 1 without building an intermediate Fix tree")
    void hyloFib1() {
      assertThat(
              RecursionSchemes.hylo(
                  RecursionSchemesTest::evalAlgebra, RecursionSchemesTest::fibCoalgebra, 1))
          .isEqualTo(1);
    }

    @Test
    @DisplayName("hylo computes fib(5) = 5")
    void hyloFib5() {
      assertThat(
              RecursionSchemes.hylo(
                  RecursionSchemesTest::evalAlgebra, RecursionSchemesTest::fibCoalgebra, 5))
          .isEqualTo(5);
    }

    @Test
    @DisplayName("hylo computes fib(10) = 55")
    void hyloFib10() {
      assertThat(
              RecursionSchemes.hylo(
                  RecursionSchemesTest::evalAlgebra, RecursionSchemesTest::fibCoalgebra, 10))
          .isEqualTo(55);
    }
  }

  // ─── Paramorphism ──────────────────────────────────────────────────────────

  // Para algebra: depth of the tree, using the Pair to verify original subtree is
  // accessible
  private static int depthParaAlgebra(RecursionSchemes.ExprF<RecursionSchemes.Pair<Integer>> e) {
    return switch (e) {
      case RecursionSchemes.ExprF.Num<RecursionSchemes.Pair<Integer>> _ -> 0;
      case RecursionSchemes.ExprF.Add<RecursionSchemes.Pair<Integer>> a ->
          1 + Math.max(a.left().result(), a.right().result());
      case RecursionSchemes.ExprF.Mul<RecursionSchemes.Pair<Integer>> m ->
          1 + Math.max(m.left().result(), m.right().result());
    };
  }

  // Para algebra: count leaf nodes, verifying original Fix subtrees are
  // accessible
  private static int countLeavesParaAlgebra(
      RecursionSchemes.ExprF<RecursionSchemes.Pair<Integer>> e) {
    return switch (e) {
      case RecursionSchemes.ExprF.Num<RecursionSchemes.Pair<Integer>> _ -> 1;
      case RecursionSchemes.ExprF.Add<RecursionSchemes.Pair<Integer>> a ->
          a.left().result() + a.right().result();
      case RecursionSchemes.ExprF.Mul<RecursionSchemes.Pair<Integer>> m ->
          m.left().result() + m.right().result();
    };
  }

  @Nested
  @DisplayName("Paramorphism (para)")
  class ParaTests {

    @Test
    @DisplayName("para computes depth of a numeric leaf as 0")
    void paraDepthLeaf() {
      assertThat(
              RecursionSchemes.para(
                  RecursionSchemesTest::depthParaAlgebra, RecursionSchemes.Fix.num(7)))
          .isEqualTo(0);
    }

    @Test
    @DisplayName("para computes depth of (2*3)+4 as 2")
    void paraDepthTree() {
      var expr =
          RecursionSchemes.Fix.add(
              RecursionSchemes.Fix.mul(RecursionSchemes.Fix.num(2), RecursionSchemes.Fix.num(3)),
              RecursionSchemes.Fix.num(4));
      assertThat(RecursionSchemes.para(RecursionSchemesTest::depthParaAlgebra, expr)).isEqualTo(2);
    }

    @Test
    @DisplayName("para pairs the original subtree alongside each recursed result")
    void paraExposesOriginalSubtree() {
      // Algebra inspects the original Fix subtree via Pair.subtree
      var leafNode = RecursionSchemes.Fix.num(5);
      var counter =
          RecursionSchemes.<Integer>para(
              e ->
                  switch (e) {
                    case RecursionSchemes.ExprF.Num<RecursionSchemes.Pair<Integer>> n -> n.value();
                    case RecursionSchemes.ExprF.Add<RecursionSchemes.Pair<Integer>> a ->
                        a.left().result() + a.right().result();
                    case RecursionSchemes.ExprF.Mul<RecursionSchemes.Pair<Integer>> m ->
                        m.left().result() + m.right().result();
                  },
              leafNode);
      assertThat(counter).isEqualTo(5);
    }

    @Test
    @DisplayName("para counts leaf nodes in (2*3)+4 as 3")
    void paraCountsLeaves() {
      var expr =
          RecursionSchemes.Fix.add(
              RecursionSchemes.Fix.mul(RecursionSchemes.Fix.num(2), RecursionSchemes.Fix.num(3)),
              RecursionSchemes.Fix.num(4));
      assertThat(RecursionSchemes.para(RecursionSchemesTest::countLeavesParaAlgebra, expr))
          .isEqualTo(3);
    }
  }

  // ─── Apomorphism ───────────────────────────────────────────────────────────

  // Apo coalgebra: unfold n into n + (n-1) + ... + 1
  // Stop early by reusing Fix.num(n) for the left branch
  private static RecursionSchemes.ExprF<RecursionSchemes.Either<Integer>> addDownCoalgebra(int n) {
    if (n <= 1) return new RecursionSchemes.ExprF.Num<>(n);
    return new RecursionSchemes.ExprF.Add<>(
        RecursionSchemes.Either.stop(RecursionSchemes.Fix.num(n)),
        RecursionSchemes.Either.next(n - 1));
  }

  @Nested
  @DisplayName("Apomorphism (apo)")
  class ApoTests {

    @Test
    @DisplayName("apo with n=1 produces Fix.num(1), evaluates to 1")
    void apoN1() {
      var tree = RecursionSchemes.apo(RecursionSchemesTest::addDownCoalgebra, 1);
      assertThat(RecursionSchemes.cata(RecursionSchemesTest::evalAlgebra, tree)).isEqualTo(1);
    }

    @Test
    @DisplayName("apo with n=3 produces 3+2+1 = 6, stopping at each leaf via Either.stop")
    void apoN3() {
      var tree = RecursionSchemes.apo(RecursionSchemesTest::addDownCoalgebra, 3);
      assertThat(RecursionSchemes.cata(RecursionSchemesTest::evalAlgebra, tree)).isEqualTo(6);
    }

    @Test
    @DisplayName("apo with n=5 produces 5+4+3+2+1 = 15")
    void apoN5() {
      var tree = RecursionSchemes.apo(RecursionSchemesTest::addDownCoalgebra, 5);
      assertThat(RecursionSchemes.cata(RecursionSchemesTest::evalAlgebra, tree)).isEqualTo(15);
    }
  }
}
