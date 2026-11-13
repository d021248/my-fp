package d021248.mytry.core.lang.fp.turingmachine;

/// The control state of a Turing Machine — a sum type with exactly two variants.
///
/// `Run` carries the state's name so transition tables remain readable as plain text.
/// `Halt` is the single accepting state: once entered, the machine stops.
///
/// ## Design note
///
/// Encoding `halt` as a distinct type (rather than a sentinel string) makes the
/// type checker enforce exhaustiveness: a `switch` over `TmState` *must* handle
/// both `Halt` and `Run` — the compiler rejects a transition loop that forgets
/// the termination case.
public sealed interface TmState permits TmState.Halt, TmState.Run {

  /// The machine has finished — no further transitions are defined.
  record Halt() implements TmState {}

  /// The machine is running in the named state.
  record Run(String name) implements TmState {}

  // -------------------------------------------------------------------------
  // Factory helpers — avoids `new` at call sites
  // -------------------------------------------------------------------------

  static TmState halt() {
    return new Halt();
  }

  static TmState run(String name) {
    return new Run(name);
  }
}
