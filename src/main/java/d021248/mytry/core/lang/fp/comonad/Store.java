package d021248.mytry.core.lang.fp.comonad;

import java.util.function.Function;

/// The Store comonad: a context-sensitive lookup paired with a focused position.
///
/// `Store<S, A>` holds:
/// - a *getter* `S → A` that computes a value for any position, and
/// - a *current position* `S` — the focus.
///
/// It models focusable data structures: spreadsheet cells, cellular automata,
/// or any "environment with a cursor".
///
/// ## Comonad operations
///
/// | Operation     | Description |
/// |---------------|-------------|
/// | `extract()`   | Value at the current focus (dual of `pure`) |
/// | `extend(f)`   | Apply a context-sensitive function at every position (dual of `flatMap`) |
/// | `duplicate()` | `Store<S, Store<S,A>>` — focus on the focusing itself |
/// | `seek(s)`     | Move the focus to position `s` |
/// | `peek(s)`     | Read the value at `s` without moving the focus |
/// | `map(f)`      | Transform the value type |
///
/// ## Example
///
/// ```java
/// // Squares table: position = index, value = index²
/// var squares = Store.<Integer, Integer>of(n -> n * n, 3);  // focus at 3
///
/// squares.extract()   // 9  (3²)
/// squares.peek(5)     // 25 (5²) — no focus change
///
/// // Extend: each cell becomes sum of its left and right neighbours' squares
/// var ns = squares.extend(s -> s.peek(s.pos() - 1) + s.extract() + s.peek(s.pos() + 1));
/// ns.extract()        // 4 + 9 + 16 = 29  (neighbours of 3)
/// ```
public record Store<S, A>(Function<S, A> getter, S pos) {

  /// Constructs a store with a getter function and an initial focus position.
  public static <S, A> Store<S, A> of(Function<S, A> getter, S pos) {
    return new Store<>(getter, pos);
  }

  // ─── Comonad operations ────────────────────────────────────────────────

  /// Returns the value at the current focus position.
  public A extract() {
    return getter.apply(pos);
  }

  /// Returns the value at position `s` without changing the focus.
  public A peek(S s) {
    return getter.apply(s);
  }

  /// Moves the focus to position `s`.
  public Store<S, A> seek(S s) {
    return new Store<>(getter, s);
  }

  /// Transforms the value type.
  public <B> Store<S, B> map(Function<A, B> f) {
    return new Store<>(s -> f.apply(getter.apply(s)), pos);
  }

  /// Applies a context-sensitive function at every possible position,
  /// producing a new store with the results.
  ///
  /// `f` receives a `Store<S, A>` focused at each position, so it can
  /// inspect neighbours via `peek` or `seek`.
  public <B> Store<S, B> extend(Function<Store<S, A>, B> f) {
    return new Store<>(s -> f.apply(seek(s)), pos);
  }

  /// Returns a store whose value at each position is the store focused there.
  public Store<S, Store<S, A>> duplicate() {
    return extend(s -> s);
  }
}
