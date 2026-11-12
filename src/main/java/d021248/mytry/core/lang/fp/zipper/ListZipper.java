package d021248.mytry.core.lang.fp.zipper;

import module java.base;

/// A list with a "focus" — an element singled out for O(1) inspection and update.
///
/// A `ListZipper<A>` = `(reversed-left, focus, right)`:
///
/// - `left` — elements to the **left** of the focus, **nearest first** (reversed)
/// - `focus` — the currently selected element
/// - `right` — elements to the right, in normal order
///
/// Navigation (`moveLeft`, `moveRight`) and update (`modify`) are O(1).
/// Reconstruction (`toList`) is O(n).
///
/// ## Relationship to the Store comonad
///
/// `ListZipper<A>` is isomorphic to `Store<Integer, A>`: the position is an
/// index, `extract` returns the focused element, and `extend` applies a
/// context-sensitive function at every index — exactly the Store comonad.
///
/// ## Relationship to Lens
///
/// The focus IS a `Lens<ListZipper<A>, A>`: `get` returns the focus,
/// `set` calls `replace`.
public record ListZipper<A>(List<A> left, A focus, List<A> right) {

  /// Creates a zipper focused on the first element, or empty for an empty list.
  public static <A> Optional<ListZipper<A>> of(List<A> all) {
    if (all.isEmpty()) return Optional.empty();
    return Optional.of(new ListZipper<>(List.of(), all.getFirst(), all.subList(1, all.size())));
  }

  /// Moves the focus one step to the left, or empty if already at the start.
  public Optional<ListZipper<A>> moveLeft() {
    if (left.isEmpty()) return Optional.empty();
    var newFocus = left.getFirst();
    var newLeft = left.subList(1, left.size());
    var newRight = new ArrayList<A>();
    newRight.add(focus);
    newRight.addAll(right);
    return Optional.of(new ListZipper<>(newLeft, newFocus, List.copyOf(newRight)));
  }

  /// Moves the focus one step to the right, or empty if already at the end.
  public Optional<ListZipper<A>> moveRight() {
    if (right.isEmpty()) return Optional.empty();
    var newFocus = right.getFirst();
    var newRight = right.subList(1, right.size());
    var newLeft = new ArrayList<A>();
    newLeft.add(focus);
    newLeft.addAll(left);
    return Optional.of(new ListZipper<>(List.copyOf(newLeft), newFocus, newRight));
  }

  /// Replaces the focus by applying `f` to it.
  public ListZipper<A> modify(UnaryOperator<A> f) {
    return new ListZipper<>(left, f.apply(focus), right);
  }

  /// Replaces the focus with `value`.
  public ListZipper<A> replace(A value) {
    return new ListZipper<>(left, value, right);
  }

  /// Inserts `value` immediately to the right of the focus.
  public ListZipper<A> insertRight(A value) {
    var newRight = new ArrayList<A>();
    newRight.add(value);
    newRight.addAll(right);
    return new ListZipper<>(left, focus, List.copyOf(newRight));
  }

  /// Removes the focus and moves to the right, or to the left if at the end.
  public Optional<ListZipper<A>> delete() {
    if (!right.isEmpty()) {
      return Optional.of(new ListZipper<>(left, right.getFirst(), right.subList(1, right.size())));
    }
    if (!left.isEmpty()) {
      return Optional.of(
          new ListZipper<>(left.subList(1, left.size()), left.getFirst(), List.of()));
    }
    return Optional.empty();
  }

  /// Reconstructs the full list: reversed-left + [focus] + right.
  public List<A> toList() {
    var result = new ArrayList<A>();
    var leftReversed = new ArrayList<>(left);
    Collections.reverse(leftReversed);
    result.addAll(leftReversed);
    result.add(focus);
    result.addAll(right);
    return List.copyOf(result);
  }

  /// Returns the focused element (comonad `extract`).
  public A extract() {
    return focus;
  }

  /// Transforms every element (functor `map`).
  public <B> ListZipper<B> map(Function<A, B> f) {
    return new ListZipper<>(
        left.stream().map(f).toList(), f.apply(focus), right.stream().map(f).toList());
  }

  /// Applies a context-sensitive function at every position (comonad `extend`).
  ///
  /// At each position the function receives the zipper focused **at that position**,
  /// giving access to neighbours on both sides.
  ///
  /// The `left` of the result is nearest-first, preserving the zipper invariant.
  public <B> ListZipper<B> extend(Function<ListZipper<A>, B> f) {
    // Collect leftward positions: first iteration yields nearest-left, so
    // the resulting list is already in nearest-first order.
    var leftResults = new ArrayList<B>();
    var cursor = this;
    while (!cursor.left.isEmpty()) {
      cursor = cursor.moveLeft().orElseThrow();
      leftResults.add(f.apply(cursor));
    }
    // Collect rightward positions in encounter order (nearest-right first).
    var rightResults = new ArrayList<B>();
    cursor = this;
    while (!cursor.right.isEmpty()) {
      cursor = cursor.moveRight().orElseThrow();
      rightResults.add(f.apply(cursor));
    }
    return new ListZipper<>(List.copyOf(leftResults), f.apply(this), List.copyOf(rightResults));
  }
}
