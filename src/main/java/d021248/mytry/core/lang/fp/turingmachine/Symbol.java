package d021248.mytry.core.lang.fp.turingmachine;

/// The tape alphabet of a Turing Machine.
///
/// A two-symbol alphabet is sufficient to simulate any TM (Church-Turing thesis).
/// `Blank` represents the initial, unwritten tape cell; `Mark` represents a written cell.
///
/// Both variants are structureless records — equality and hash are derived automatically,
/// and the two constants make transition tables readable without `new`.
///
/// For machines that need a richer alphabet, extend this sealed hierarchy.
public sealed interface Symbol permits Symbol.Blank, Symbol.Mark {

  /// The unwritten cell — the tape's default value.
  record Blank() implements Symbol {}

  /// A written cell — the only non-blank symbol in a two-symbol alphabet.
  record Mark() implements Symbol {}

  /// Canonical blank constant — use instead of {@code new Blank()}.
  Symbol BLANK = new Blank();

  /// Canonical mark constant — use instead of {@code new Mark()}.
  Symbol MARK = new Mark();
}
