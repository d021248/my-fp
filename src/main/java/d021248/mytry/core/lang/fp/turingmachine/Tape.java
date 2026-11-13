package d021248.mytry.core.lang.fp.turingmachine;

import d021248.mytry.core.lang.fp.zipper.ListZipper;
import java.util.ArrayList;
import java.util.List;

/// The Turing Machine tape — a conceptually infinite sequence of `Symbol` cells,
/// with a read/write head focused on one cell at a time.
///
/// ## Representation
///
/// Internally, a `Tape` wraps a {@link ListZipper}{@code <Symbol>}.
/// The zipper's `focus` is the cell under the head.
/// The zipper's `left` holds the cells to the left (nearest first, reversed).
/// The zipper's `right` holds the cells to the right (nearest first).
///
/// ## Infinity
///
/// A real tape is infinite in both directions.  Moving past either boundary
/// extends the tape on the fly with a `Blank` rather than returning an error —
/// the caller never sees an `Optional` or an exception.
///
/// ## Immutability
///
/// Every operation returns a new `Tape`; the original is unchanged.
/// This keeps the machine's step function pure and makes history trivially replayable.
public record Tape(ListZipper<Symbol> zipper) {

  // -------------------------------------------------------------------------
  // Construction
  // -------------------------------------------------------------------------

  /// Creates a tape that is entirely blank, with the head on the single initial cell.
  public static Tape blank() {
    return new Tape(new ListZipper<>(List.of(), Symbol.BLANK, List.of()));
  }

  /// Creates a tape whose initial content is `symbols`, head on the first cell.
  /// If `symbols` is empty, falls back to a blank tape.
  public static Tape of(List<Symbol> symbols) {
    if (symbols.isEmpty()) return blank();
    return new Tape(ListZipper.of(symbols).orElseThrow());
  }

  // -------------------------------------------------------------------------
  // Head operations
  // -------------------------------------------------------------------------

  /// Returns the symbol currently under the head.
  public Symbol read() {
    return zipper.focus();
  }

  /// Returns a tape with the head cell replaced by `symbol`; head does not move.
  public Tape write(Symbol symbol) {
    return new Tape(zipper.replace(symbol));
  }

  /// Moves the head one step in the given direction.
  /// Moving past a boundary silently extends the tape with `Blank`.
  public Tape move(Direction d) {
    return switch (d) {
      case L -> moveLeft();
      case R -> moveRight();
      case N -> this;
    };
  }

  // -------------------------------------------------------------------------
  // Display
  // -------------------------------------------------------------------------

  /// Returns a human-readable snapshot: left cells / [focus] / right cells.
  /// Blanks are shown as `_`, marks as `1`.
  public String display() {
    var sb = new StringBuilder();
    zipper.left().reversed().forEach(s -> sb.append(symbolChar(s)).append(' '));
    sb.append('[').append(symbolChar(zipper.focus())).append(']');
    zipper.right().forEach(s -> sb.append(' ').append(symbolChar(s)));
    return sb.toString();
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private Tape moveLeft() {
    return new Tape(
        zipper
            .moveLeft()
            .orElseGet(
                () -> {
                  // Left boundary: extend with a blank to the left.
                  var newRight = new ArrayList<Symbol>();
                  newRight.add(zipper.focus());
                  newRight.addAll(zipper.right());
                  return new ListZipper<>(List.of(), Symbol.BLANK, List.copyOf(newRight));
                }));
  }

  private Tape moveRight() {
    return new Tape(
        zipper
            .moveRight()
            .orElseGet(
                () -> {
                  // Right boundary: extend with a blank to the right.
                  var newLeft = new ArrayList<Symbol>();
                  newLeft.add(zipper.focus());
                  newLeft.addAll(zipper.left());
                  return new ListZipper<>(List.copyOf(newLeft), Symbol.BLANK, List.of());
                }));
  }

  private static char symbolChar(Symbol s) {
    return switch (s) {
      case Symbol.Blank _ -> '_';
      case Symbol.Mark _ -> '1';
    };
  }
}
