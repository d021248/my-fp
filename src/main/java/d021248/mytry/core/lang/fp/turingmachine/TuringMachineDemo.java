package d021248.mytry.core.lang.fp.turingmachine;

import static d021248.mytry.core.lang.fp.turingmachine.Direction.*;
import static d021248.mytry.core.lang.fp.turingmachine.Symbol.*;
import static d021248.mytry.core.lang.fp.turingmachine.TmState.*;

import java.util.List;

/// Two classic Turing Machine programs, shown as executable documentation.
///
/// ## 1 — Unary increment
///
/// Input:  `1 1 1`  (the number 3 in unary, tape = [MARK, MARK, MARK])
/// Output: `1 1 1 1` (the number 4)
///
/// The head starts at the leftmost mark and scans right.
/// When it finds a blank it writes a mark and halts.
///
/// ## 2 — 3-state Busy Beaver
///
/// The Busy Beaver problem asks: what is the maximum number of marks a
/// halting n-state, 2-symbol TM can write on a blank tape?
///
/// For n = 3 the answer is 6.  The machine below achieves it in 14 steps.
///
/// Transition table (Radó 1962):
///
/// | State | Read | Write | Move | Next |
/// |-------|------|-------|------|------|
/// | A     | 0    | 1     | R    | B    |
/// | A     | 1    | 1     | L    | C    |
/// | B     | 0    | 1     | L    | A    |
/// | B     | 1    | 1     | R    | B    |
/// | C     | 0    | 1     | L    | B    |
/// | C     | 1    | 1     | R    | HALT |
public class TuringMachineDemo {

  public static void main(String[] args) {
    System.out.println("=== Unary increment: 3 → 4 ===");
    runUnaryIncrement();

    System.out.println();
    System.out.println("=== 3-state Busy Beaver ===");
    runBusyBeaver3();
  }

  // ---------------------------------------------------------------------------
  // Unary increment
  // ---------------------------------------------------------------------------

  static void runUnaryIncrement() {
    var scan = (TmState.Run) run("SCAN");

    var tm =
        TuringMachine.builder()
            .on(scan, MARK, scan, MARK, R) // keep scanning right
            .on(scan, BLANK, halt(), MARK, N) // write the new mark and halt
            .build();

    var tape = Tape.of(List.of(MARK, MARK, MARK));
    var config = Configuration.initial(scan, tape);

    System.out.println("Before: " + config.tape().display());
    var result = tm.run(config);
    System.out.println("After:  " + result.tape().display());
    System.out.println("Halted: " + result.halted());
  }

  // ---------------------------------------------------------------------------
  // 3-state Busy Beaver
  // ---------------------------------------------------------------------------

  static void runBusyBeaver3() {
    var A = (TmState.Run) run("A");
    var B = (TmState.Run) run("B");
    var C = (TmState.Run) run("C");

    var tm =
        TuringMachine.builder()
            .on(A, BLANK, run("B"), MARK, R)
            .on(A, MARK, run("C"), MARK, L)
            .on(B, BLANK, run("A"), MARK, L)
            .on(B, MARK, run("B"), MARK, R)
            .on(C, BLANK, run("B"), MARK, L)
            .on(C, MARK, halt(), MARK, R)
            .build();

    var config = Configuration.initial(A);
    var result = tm.run(config);

    System.out.println("Tape:   " + result.tape().display());
    long marks =
        result.tape().zipper().toList().stream().filter(s -> s instanceof Symbol.Mark).count();
    System.out.println("Marks:  " + marks + "  (expected 6)");
    System.out.println("Halted: " + result.halted());
  }
}
