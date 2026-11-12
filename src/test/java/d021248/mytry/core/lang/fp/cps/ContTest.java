package d021248.mytry.core.lang.fp.cps;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ContTest {

  // ── pure ──────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("pure wraps a value and eval returns it unchanged")
  void pureEvalReturnsValue() {
    assertThat(Cont.eval(Cont.<Integer, Integer>pure(42))).isEqualTo(42);
  }

  @Test
  @DisplayName("pure with null value evals to null")
  void pureWithNull() {
    assertThat(Cont.eval(Cont.<String, String>pure(null))).isNull();
  }

  // ── map ───────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("map transforms the result value")
  void mapTransformsValue() {
    var result = Cont.eval(Cont.<Integer, String>pure(5).map(n -> n * 2).map(n -> "result=" + n));
    assertThat(result).isEqualTo("result=10");
  }

  @Test
  @DisplayName("map with identity function is a no-op")
  void mapWithIdentityIsNoop() {
    assertThat(Cont.eval(Cont.<String, String>pure("hello").map(s -> s))).isEqualTo("hello");
  }

  // ── flatMap ───────────────────────────────────────────────────────────────

  @Test
  @DisplayName("flatMap sequences two Cont computations")
  void flatMapSequences() {
    var result =
        Cont.eval(
            Cont.<Integer, Integer>pure(3)
                .flatMap(n -> Cont.pure(n * 10))
                .flatMap(n -> Cont.pure(n + 1)));
    assertThat(result).isEqualTo(31);
  }

  @Test
  @DisplayName("flatMap satisfies left identity: pure(a).flatMap(f) == f(a)")
  void flatMapLeftIdentity() {
    var direct = Cont.eval(Cont.<Integer, Integer>pure(5));
    var viaFlatMap = Cont.eval(Cont.<Integer, Integer>pure(1).flatMap(_ -> Cont.pure(5)));
    assertThat(viaFlatMap).isEqualTo(direct);
  }

  // ── callcc ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("callcc")
  class CallCcTests {

    @Test
    @DisplayName("callcc that never escapes behaves like pure")
    void callccWithoutEscapeActsLikePure() {
      var result = Cont.eval(Cont.<Integer, Integer>callcc(k -> Cont.pure(42)));
      assertThat(result).isEqualTo(42);
    }

    @Test
    @DisplayName("callcc escape exits the callcc body; code after return is unreachable")
    void callccEscapeExitsCallccBody() {
      var sideEffects = new ArrayList<String>();

      var result =
          Cont.eval(
              Cont.<Integer, Integer>callcc(
                  escape -> {
                    sideEffects.add("before escape");
                    return escape.apply(99);
                  }));

      assertThat(result).isEqualTo(99);
      assertThat(sideEffects).containsExactly("before escape");
    }

    @Test
    @DisplayName("callcc escaped value flows through subsequent map steps in the outer chain")
    void callccEscapedValueFlowsThroughOuterMaps() {
      var result =
          Cont.eval(
              Cont.<Integer, Integer>callcc(escape -> escape.apply(99))
                  .map(n -> n + 1)
                  .map(n -> n * 2));
      assertThat(result).isEqualTo(200);
    }

    @Test
    @DisplayName("callcc without escape executes all subsequent steps")
    void callccWithoutEscapeExecutesAllSteps() {
      var steps = new ArrayList<String>();

      var result =
          Cont.eval(
              Cont.<Integer, Integer>callcc(
                      _ -> {
                        steps.add("callcc body");
                        return Cont.pure(10);
                      })
                  .map(
                      n -> {
                        steps.add("map n*2");
                        return n * 2;
                      }));

      assertThat(result).isEqualTo(20);
      assertThat(steps).containsExactly("callcc body", "map n*2");
    }

    @Test
    @DisplayName("sum-until-negative via callcc returns partial sum")
    void sumUntilNegativeViaCallcc() {
      var numbers = List.of(1, 2, 3, -1, 4, 5);

      Cont<Integer, Integer> computation =
          Cont.callcc(
              escape -> {
                var acc = new int[] {0};
                for (int n : numbers) {
                  if (n < 0) return escape.apply(acc[0]);
                  acc[0] += n;
                }
                return Cont.pure(acc[0]);
              });

      assertThat(Cont.eval(computation)).isEqualTo(6);
    }

    @Test
    @DisplayName("sum-until-negative with no negative returns full sum")
    void sumUntilNegativeWithNoNegative() {
      var numbers = List.of(1, 2, 3, 4);

      Cont<Integer, Integer> computation =
          Cont.callcc(
              escape -> {
                var acc = new int[] {0};
                for (int n : numbers) {
                  if (n < 0) return escape.apply(acc[0]);
                  acc[0] += n;
                }
                return Cont.pure(acc[0]);
              });

      assertThat(Cont.eval(computation)).isEqualTo(10);
    }
  }
}
