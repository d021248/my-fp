package d021248.mytry.core.lang.fp.turingmachine;

/// A snapshot of the Turing Machine at one point in time.
///
/// A configuration = (current state, tape contents + head position).
/// Two configurations are identical iff both their state and tape are identical.
///
/// ## Why separate from TuringMachine?
///
/// A `TuringMachine` is the *program* — an immutable transition table.
/// A `Configuration` is the *runtime state* — it changes with every step.
/// Keeping them apart makes it trivial to:
/// - replay a computation from any saved configuration,
/// - compare configurations for cycle detection,
/// - inspect intermediate states without modifying the machine.
public record Configuration(TmState state, Tape tape) {

  /// Creates the initial configuration: blank tape, head at cell 0, machine in `startState`.
  public static Configuration initial(TmState startState) {
    return new Configuration(startState, Tape.blank());
  }

  /// Creates the initial configuration with preloaded tape content.
  public static Configuration initial(TmState startState, Tape tape) {
    return new Configuration(startState, tape);
  }

  /// Returns `true` if the machine has reached the halt state.
  public boolean halted() {
    return state instanceof TmState.Halt;
  }
}
