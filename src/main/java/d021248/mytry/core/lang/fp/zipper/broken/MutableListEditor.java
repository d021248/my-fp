package d021248.mytry.core.lang.fp.zipper.broken;

import module java.base;

/// A list editor using a mutable `ArrayList` and an integer index.
///
/// Problems compared to `ListZipper`:
///
/// - Mutable state — the list and index must be kept in sync manually.
/// - Fragile index arithmetic — off-by-one errors are easy.
/// - No comonad `extend` — applying a context-sensitive function over every
///   position requires a manual loop with careful index bookkeeping.
public final class MutableListEditor {

  private final List<String> items;
  private int cursor;

  /// Constructs a mutable editor initialised from the given items.
  public MutableListEditor(List<String> items) {
    this.items = new ArrayList<>(items);
    this.cursor = 0;
  }

  /// Moves the cursor one step to the right; returns `false` if already at the end.
  public boolean moveRight() {
    if (cursor < items.size() - 1) {
      cursor++;
      return true;
    }
    return false;
  }

  /// Moves the cursor one step to the left; returns `false` if already at the start.
  public boolean moveLeft() {
    if (cursor > 0) {
      cursor--;
      return true;
    }
    return false;
  }

  /// Returns the element at the current cursor position.
  public String focus() {
    return items.get(cursor);
  }

  /// Applies `f` to the element at the current cursor position.
  public void modify(UnaryOperator<String> f) {
    items.set(cursor, f.apply(items.get(cursor)));
  }

  /// Returns an immutable copy of the current list.
  public List<String> toList() {
    return List.copyOf(items);
  }

  public static void main() {
    System.out.println("=== Mutable list editor (broken) ===");
    var editor = new MutableListEditor(List.of("a", "b", "c", "d"));
    editor.moveRight();
    editor.moveRight();
    System.out.println("Focus at 'c': " + editor.focus());
    editor.modify(String::toUpperCase);
    System.out.println("After uppercase: " + editor.toList());
    System.out.println("Problem: mutable state, error-prone index arithmetic.");
  }
}
