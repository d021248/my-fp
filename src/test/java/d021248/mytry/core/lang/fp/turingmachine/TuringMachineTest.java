package d021248.mytry.core.lang.fp.turingmachine;

import static d021248.mytry.core.lang.fp.turingmachine.Direction.*;
import static d021248.mytry.core.lang.fp.turingmachine.Symbol.*;
import static d021248.mytry.core.lang.fp.turingmachine.TmState.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TuringMachineTest {

  // ---------------------------------------------------------------------------
  // Tape
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("Tape")
  class TapeTests {

    @Test
    @DisplayName("blank tape reads BLANK at head")
    void blankTapeReadsBlank() {
      assertThat(Tape.blank().read()).isEqualTo(BLANK);
    }

    @Test
    @DisplayName("write changes the focused cell")
    void writeChangesFocus() {
      assertThat(Tape.blank().write(MARK).read()).isEqualTo(MARK);
    }

    @Test
    @DisplayName("move R then move L returns to the original cell")
    void moveRightThenLeftIsIdentity() {
      var tape = Tape.blank().write(MARK).move(R).move(L);
      assertThat(tape.read()).isEqualTo(MARK);
    }

    @Test
    @DisplayName("moving left past the boundary extends with BLANK")
    void moveLeftPastBoundaryGivesBlanks() {
      var tape = Tape.blank().write(MARK).move(L);
      assertThat(tape.read()).isEqualTo(BLANK);
    }

    @Test
    @DisplayName("moving right past the boundary extends with BLANK")
    void moveRightPastBoundaryGivesBlanks() {
      var tape = Tape.blank().write(MARK).move(R);
      assertThat(tape.read()).isEqualTo(BLANK);
    }

    @Test
    @DisplayName("direction N leaves the head in place")
    void directionNIsNoOp() {
      var tape = Tape.blank().write(MARK).move(N);
      assertThat(tape.read()).isEqualTo(MARK);
    }

    @Test
    @DisplayName("Tape.of() places head on first symbol")
    void ofFocusesOnFirst() {
      var tape = Tape.of(List.of(MARK, BLANK, MARK));
      assertThat(tape.read()).isEqualTo(MARK);
    }
  }

  // ---------------------------------------------------------------------------
  // TmState
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("TmState")
  class TmStateTests {

    @Test
    @DisplayName("halt() creates a Halt instance")
    void haltCreatesHalt() {
      assertThat(halt()).isInstanceOf(TmState.Halt.class);
    }

    @Test
    @DisplayName("run(name) creates a Run instance with the given name")
    void runCreatesRun() {
      var s = run("A");
      assertThat(s).isInstanceOf(TmState.Run.class);
      assertThat(((TmState.Run) s).name()).isEqualTo("A");
    }
  }

  // ---------------------------------------------------------------------------
  // TuringMachine — unary increment
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("Unary increment")
  class UnaryIncrementTests {

    private TuringMachine buildMachine() {
      var scan = (TmState.Run) run("SCAN");
      return TuringMachine.builder()
          .on(scan, MARK, scan, MARK, R)
          .on(scan, BLANK, halt(), MARK, N)
          .build();
    }

    @Test
    @DisplayName("increment(0) → 1 mark")
    void increment0() {
      var tm = buildMachine();
      var result = tm.run(Configuration.initial(run("SCAN")));
      assertThat(markCount(result)).isEqualTo(1);
    }

    @Test
    @DisplayName("increment(3) → 4 marks")
    void increment3() {
      var tm = buildMachine();
      var tape = Tape.of(List.of(MARK, MARK, MARK));
      var result = tm.run(Configuration.initial(run("SCAN"), tape));
      assertThat(markCount(result)).isEqualTo(4);
    }

    @Test
    @DisplayName("machine halts after increment")
    void machineHalts() {
      var tm = buildMachine();
      var result = tm.run(Configuration.initial(run("SCAN")));
      assertThat(result.halted()).isTrue();
    }
  }

  // ---------------------------------------------------------------------------
  // TuringMachine — 3-state Busy Beaver
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("3-state Busy Beaver")
  class BusyBeaverTests {

    private TuringMachine buildMachine() {
      return TuringMachine.builder()
          .on((TmState.Run) run("A"), BLANK, run("B"), MARK, R)
          .on((TmState.Run) run("A"), MARK, run("C"), MARK, L)
          .on((TmState.Run) run("B"), BLANK, run("A"), MARK, L)
          .on((TmState.Run) run("B"), MARK, run("B"), MARK, R)
          .on((TmState.Run) run("C"), BLANK, run("B"), MARK, L)
          .on((TmState.Run) run("C"), MARK, halt(), MARK, R)
          .build();
    }

    @Test
    @DisplayName("writes exactly 6 marks on a blank tape")
    void writesSixMarks() {
      var tm = buildMachine();
      var result = tm.run(Configuration.initial(run("A")));
      assertThat(markCount(result)).isEqualTo(6);
    }

    @Test
    @DisplayName("halts after running")
    void halts() {
      var tm = buildMachine();
      var result = tm.run(Configuration.initial(run("A")));
      assertThat(result.halted()).isTrue();
    }
  }

  // ---------------------------------------------------------------------------
  // Implicit halt — missing transition
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("Implicit halt")
  class ImplicitHaltTests {

    @Test
    @DisplayName("machine halts when no transition is defined for current (state, symbol)")
    void implicitHaltOnMissingTransition() {
      var A = (TmState.Run) run("A");
      var tm =
          TuringMachine.builder()
              .on(A, MARK, run("A"), MARK, R) // only handles MARK, not BLANK
              .build();
      var result = tm.run(Configuration.initial(A)); // blank tape → no rule → halt
      assertThat(result.halted()).isTrue();
    }
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static long markCount(Configuration config) {
    return config.tape().zipper().toList().stream().filter(s -> s instanceof Symbol.Mark).count();
  }
}
