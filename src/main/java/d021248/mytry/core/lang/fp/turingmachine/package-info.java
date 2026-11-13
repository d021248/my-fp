/// Turing Machine — universal computation from five primitives.
///
/// ## Components
///
/// | Class | Role |
/// |---|---|
/// | `Symbol` | Tape alphabet (sealed: `Blank` / `Mark`) |
/// | `Direction` | Head movement (`L` / `R` / `N`) |
/// | `TmState` | Control state (sealed: `Halt` / `Run`) |
/// | `Tape` | Infinite symbol sequence with a read/write head |
/// | `Configuration` | Runtime snapshot: (state, tape) |
/// | `TuringMachine` | Transition table + stack-safe `run` via `Bounce` |
///
/// ## Relationship to other packages
///
/// - `Tape` wraps `fp/zipper/ListZipper` — the zipper IS the tape.
/// - `TuringMachine.run` drives `fp/trampoline/Bounce` — no stack overflow
///   regardless of step count.
/// - `TmState` follows the sealed-hierarchy style of `adt/` (sum types via records).
package d021248.mytry.core.lang.fp.turingmachine;
