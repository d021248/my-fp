package d021248.mytry.core.lang.fp.tagless;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TaglessTest {

  private final EvalInterpreter eval = new EvalInterpreter();
  private final PrettyPrinter print = new PrettyPrinter();

  // ── lit ───────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("lit — integer literal")
  class LitTests {

    @Test
    @DisplayName("lit(5) evaluates to 5")
    void litEvaluatesToInt() {
      assertThat(eval.lit(5)).isEqualTo(5);
    }

    @Test
    @DisplayName("lit(5) pretty-prints to \"5\"")
    void litPrettyPrints() {
      assertThat(print.lit(5)).isEqualTo("5");
    }
  }

  // ── add ───────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("add — addition")
  class AddTests {

    @Test
    @DisplayName("add(lit(2), lit(3)) evaluates to 5")
    void addEvaluatesToSum() {
      assertThat(eval.add(eval.lit(2), eval.lit(3))).isEqualTo(5);
    }

    @Test
    @DisplayName("add(lit(2), lit(3)) pretty-prints to \"(2 + 3)\"")
    void addPrettyPrints() {
      assertThat(print.add(print.lit(2), print.lit(3))).isEqualTo("(2 + 3)");
    }
  }

  // ── mul ───────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("mul — multiplication")
  class MulTests {

    @Test
    @DisplayName("mul(lit(3), lit(4)) evaluates to 12")
    void mulEvaluatesToProduct() {
      assertThat(eval.mul(eval.lit(3), eval.lit(4))).isEqualTo(12);
    }

    @Test
    @DisplayName("mul(lit(3), lit(4)) pretty-prints to \"(3 * 4)\"")
    void mulPrettyPrints() {
      assertThat(print.mul(print.lit(3), print.lit(4))).isEqualTo("(3 * 4)");
    }
  }

  // ── neg ───────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("neg — arithmetic negation")
  class NegTests {

    @Test
    @DisplayName("neg(lit(7)) evaluates to -7")
    void negEvaluatesToNegation() {
      assertThat(eval.neg(eval.lit(7))).isEqualTo(-7);
    }

    @Test
    @DisplayName("neg(lit(7)) pretty-prints to \"(-7)\"")
    void negPrettyPrints() {
      assertThat(print.neg(print.lit(7))).isEqualTo("(-7)");
    }
  }

  // ── composite ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("composite expression: add(lit(2), mul(lit(3), lit(4)))")
  class CompositeTests {

    @Test
    @DisplayName("composite evaluates to 14")
    void compositeEvaluatesCorrectly() {
      var result = eval.add(eval.lit(2), eval.mul(eval.lit(3), eval.lit(4)));
      assertThat(result).isEqualTo(14);
    }

    @Test
    @DisplayName("composite pretty-prints to \"(2 + (3 * 4))\"")
    void compositePrettyPrints() {
      var result = print.add(print.lit(2), print.mul(print.lit(3), print.lit(4)));
      assertThat(result).isEqualTo("(2 + (3 * 4))");
    }

    @Test
    @DisplayName("same expression tree produces consistent results from both interpreters")
    void bothInterpretersProduce14andPrettyString() {
      // Build identically using a generic helper
      assertThat(TaglessDemo.buildExpr(eval)).isEqualTo(14);
      assertThat(TaglessDemo.buildExpr(print)).isEqualTo("(2 + (3 * 4))");
    }
  }
}
