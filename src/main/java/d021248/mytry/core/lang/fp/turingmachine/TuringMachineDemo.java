package d021248.mytry.core.lang.fp.turingmachine;

import static d021248.mytry.core.lang.fp.turingmachine.Direction.*;
import static d021248.mytry.core.lang.fp.turingmachine.Symbol.*;
import static d021248.mytry.core.lang.fp.turingmachine.TmState.*;

import java.util.List;

/// Three classic Turing Machine programs with step-by-step trace output.
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
/// For n = 3 the answer is 6.  The machine below achieves it in 13 steps.
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
///
/// ## 3 — 4-state Busy Beaver
///
/// For n = 4 the answer is 13 marks — achieved in 107 steps (Brady 1964).
///
/// Transition table:
///
/// | State | Read | Write | Move | Next |
/// |-------|------|-------|------|------|
/// | A     | 0    | 1     | R    | B    |
/// | A     | 1    | 1     | L    | B    |
/// | B     | 0    | 1     | L    | A    |
/// | B     | 1    | 0     | L    | C    |
/// | C     | 0    | 1     | R    | HALT |
/// | C     | 1    | 1     | L    | D    |
/// | D     | 0    | 1     | R    | D    |
/// | D     | 1    | 0     | R    | A    |
///
/// ## 4 — 5-state Busy Beaver
///
/// For n = 5 the answer is 4,098 marks — achieved in 47,176,870 steps
/// (Marxen &amp; Buntrock 1989).  At this scale `trace()` is infeasible;
/// the demo uses `runStats()` which iterates without storing intermediate configurations.
///
/// Transition table:
///
/// | State | Read | Write | Move | Next |
/// |-------|------|-------|------|------|
/// | A     | 0    | 1     | R    | B    |
/// | A     | 1    | 1     | L    | C    |
/// | B     | 0    | 1     | R    | C    |
/// | B     | 1    | 1     | R    | B    |
/// | C     | 0    | 1     | R    | D    |
/// | C     | 1    | 0     | L    | E    |
/// | D     | 0    | 1     | L    | A    |
/// | D     | 1    | 1     | L    | D    |
/// | E     | 0    | 1     | R    | HALT |
/// | E     | 1    | 0     | L    | A    |
public class TuringMachineDemo {

  public static void main(String[] args) {
    System.out.println("=== Unary increment: 3 → 4 ===");
    runUnaryIncrement();

    System.out.println("=== 3-state Busy Beaver ===");
    runBusyBeaver3();

    System.out.println("=== 4-state Busy Beaver ===");
    runBusyBeaver4();

    System.out.println("=== 5-state Busy Beaver ===");
    runBusyBeaver5();
  }

  // ---------------------------------------------------------------------------
  // Unary increment
  // ---------------------------------------------------------------------------

  static void runUnaryIncrement() {
    var scan = (TmState.Run) run("SCAN");

    var tm =
        TuringMachine.builder()
            .on(scan, MARK, scan, MARK, R)
            .on(scan, BLANK, halt(), MARK, N)
            .build();

    var tape = Tape.of(List.of(MARK, MARK, MARK));
    var initial = Configuration.initial(scan, tape);

    var steps = printTrace(tm, initial);
    long marks = markCount(steps.getLast());
    System.out.printf("  → %d marks%n%n", marks);
  }

  // ---------------------------------------------------------------------------
  // 3-state Busy Beaver
  // ---------------------------------------------------------------------------

  static void runBusyBeaver3() {
    var stateA = (TmState.Run) run("A");
    var stateB = (TmState.Run) run("B");
    var stateC = (TmState.Run) run("C");

    var tm =
        TuringMachine.builder()
            .on(stateA, BLANK, run("B"), MARK, R)
            .on(stateA, MARK, run("C"), MARK, L)
            .on(stateB, BLANK, run("A"), MARK, L)
            .on(stateB, MARK, run("B"), MARK, R)
            .on(stateC, BLANK, run("B"), MARK, L)
            .on(stateC, MARK, halt(), MARK, R)
            .build();

    var initial = Configuration.initial(stateA);
    var steps = printTrace(tm, initial);
    long marks = markCount(steps.getLast());
    System.out.printf("  → %d marks  (expected 6)%n%n", marks);
  }

  // ---------------------------------------------------------------------------
  // 4-state Busy Beaver
  // ---------------------------------------------------------------------------

  static void runBusyBeaver4() {
    var stateA = (TmState.Run) run("A");
    var stateB = (TmState.Run) run("B");
    var stateC = (TmState.Run) run("C");
    var stateD = (TmState.Run) run("D");

    var tm =
        TuringMachine.builder()
            .on(stateA, BLANK, run("B"), MARK, R)
            .on(stateA, MARK, run("B"), MARK, L)
            .on(stateB, BLANK, run("A"), MARK, L)
            .on(stateB, MARK, run("C"), BLANK, L)
            .on(stateC, BLANK, halt(), MARK, R)
            .on(stateC, MARK, run("D"), MARK, L)
            .on(stateD, BLANK, run("D"), MARK, R)
            .on(stateD, MARK, run("A"), BLANK, R)
            .build();

    var initial = Configuration.initial(stateA);
    var steps = printTrace(tm, initial);
    long marks = markCount(steps.getLast());
    System.out.printf(
        "  → %d marks in %d steps  (expected 13 marks, 107 steps)%n%n", marks, steps.size() - 1);
  }

  private static List<Configuration> printTrace(TuringMachine tm, Configuration initial) {
    System.out.println();
    System.out.printf(" %4s │ %-8s │ %s%n", "Step", "State", "Tape");
    System.out.println("──────┼──────────┼────────────────────────────────");

    var configs = tm.trace(initial).toList();
    for (int i = 0; i < configs.size(); i++) {
      var cfg = configs.get(i);
      var stateName =
          switch (cfg.state()) {
            case TmState.Run(var name) -> name;
            case TmState.Halt _ -> "HALT";
          };
      System.out.printf(" %4d │ %-8s │ %s%n", i, stateName, cfg.tape().display());
    }
    System.out.println();
    return configs;
  }

  private static long markCount(Configuration config) {
    return config.tape().zipper().toList().stream().filter(s -> s instanceof Symbol.Mark).count();
  }

  // ---------------------------------------------------------------------------
  // 5-state Busy Beaver
  // ---------------------------------------------------------------------------

  static void runBusyBeaver5() {
    var stateA = (TmState.Run) run("A");
    var stateB = (TmState.Run) run("B");
    var stateC = (TmState.Run) run("C");
    var stateD = (TmState.Run) run("D");
    var stateE = (TmState.Run) run("E");

    var tm =
        TuringMachine.builder()
            .on(stateA, BLANK, run("B"), MARK, R)
            .on(stateA, MARK, run("C"), MARK, L)
            .on(stateB, BLANK, run("C"), MARK, R)
            .on(stateB, MARK, run("B"), MARK, R)
            .on(stateC, BLANK, run("D"), MARK, R)
            .on(stateC, MARK, run("E"), BLANK, L)
            .on(stateD, BLANK, run("A"), MARK, L)
            .on(stateD, MARK, run("D"), MARK, L)
            .on(stateE, BLANK, halt(), MARK, R)
            .on(stateE, MARK, run("A"), BLANK, L)
            .build();

    System.out.println();
    System.out.println("  47M+ steps — HashMap tape, no trace...");
    System.out.println();

    var t0 = System.nanoTime();
    var result = tm.runFast(stateA);
    var elapsedMs = (System.nanoTime() - t0) / 1_000_000L;

    System.out.printf("  Steps:  %,12d  (expected 47,176,870)%n", result.steps());
    System.out.printf("  Marks:  %,12d  (expected 4,098)%n", result.marks());
    System.out.printf("  Time:   %,12d ms%n%n", elapsedMs);
  }
}
