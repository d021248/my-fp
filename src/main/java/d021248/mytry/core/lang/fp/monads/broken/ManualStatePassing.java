package d021248.mytry.core.lang.fp.monads.broken;

/// Demonstrates manual state threading without the State monad.
///
/// Every function that needs to read or update state must accept the current
/// state as a parameter and return both a result and the new state as a
/// pair. This boilerplate grows with every additional step.
///
/// Compare with {@link d021248.mytry.core.lang.fp.state.State} which hides the
/// state-threading plumbing behind `flatMap`.
public final class ManualStatePassing {

  // ── State represented as a plain record ───────────────────────────────────

  record Step<A>(A value, int state) {}

  // ── Manual state-threading ────────────────────────────────────────────────

  static Step<String> tick(int state) {
    int next = state + 1;
    return new Step<>("tick #" + next, next);
  }

  static Step<String> tick3(int initial) {
    var r1 = tick(initial); // explicit pass-in
    var r2 = tick(r1.state()); // must thread state manually
    var r3 = tick(r2.state()); // and again
    return r3;
  }

  public static void main() {
    System.out.println("=== Manual State Threading ===");
    var result = tick3(0);
    System.out.println("Result: " + result.value());
    System.out.println("State : " + result.state());
    System.out.println();
    System.out.println("The plumbing (r1.state(), r2.state(), ...) is always visible.");
    System.out.println("State.flatMap() hides it in the monad pipeline.");
  }
}
