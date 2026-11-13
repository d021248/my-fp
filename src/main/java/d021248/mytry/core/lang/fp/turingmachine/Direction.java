package d021248.mytry.core.lang.fp.turingmachine;

/// The head movement direction after a transition.
///
/// `L` moves the read/write head one cell to the left, `R` one cell to the right,
/// `N` leaves the head in place (useful for Mealy-style machines that only write).
public enum Direction {
  L,
  R,
  N
}
