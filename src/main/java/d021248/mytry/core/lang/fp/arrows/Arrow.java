package d021248.mytry.core.lang.fp.arrows;

import module java.base;

/// A composable computation from `A` to `B` — a generalisation of `Function<A,B>`.
///
/// Where a plain function `A → B` can only be composed end-to-end, an **Arrow**
/// also supports *parallel* composition through `first`, `second`, and `fanout`.
///
/// ## Core operations
///
/// | Method | Description |
/// |---|---|
/// | `arr(f)` | lift an ordinary `Function` into an `Arrow` |
/// | `apply(a)` | run the arrow |
/// | `andThen(g)` | sequential composition (this then g) |
/// | `first()` | run this arrow on the first element of a pair, pass second through |
/// | `second()` | run this arrow on the second element of a pair, pass first through |
/// | `fanout(g)` | apply both arrows to the same input; return a pair of results |
///
/// ## Pair
///
/// The `Pair<X,Y>` record carries two typed values without boxing into `Object[]`.
/// It plays the role of Haskell's `(,)` type.
///
/// ## The `loop` combinator
///
/// Haskell's `ArrowLoop` introduces `loop :: Arrow a => a (b,d) (c,d) -> a b c`,
/// which threads an internal state through a feedback cycle.  In Java this
/// requires a mutable reference or `AtomicReference` to break the circular
/// dependency; it is documented here for completeness but not implemented.
@FunctionalInterface
public interface Arrow<A, B> {

  /// Holds two typed values.
  record Pair<X, Y>(X first, Y second) {}

  // ── Running ───────────────────────────────────────────────────────────────

  /// Runs this arrow on `input` and returns the result.
  B apply(A input);

  // ── Static factories ──────────────────────────────────────────────────────

  /// Lifts an ordinary function `f` into an arrow.
  static <A, B> Arrow<A, B> arr(Function<A, B> f) {
    return f::apply;
  }

  /// The identity arrow: returns its input unchanged.
  static <A> Arrow<A, A> identity() {
    return a -> a;
  }

  // ── Instance combinators ──────────────────────────────────────────────────

  /// Sequential composition: run `this`, then feed the result into `g`.
  default <C> Arrow<A, C> andThen(Arrow<B, C> g) {
    return a -> g.apply(apply(a));
  }

  /// Applies `this` to the first element of a pair; the second passes through unchanged.
  default <C> Arrow<Pair<A, C>, Pair<B, C>> first() {
    return pair -> new Pair<>(apply(pair.first()), pair.second());
  }

  /// Applies `this` to the second element of a pair; the first passes through unchanged.
  default <C> Arrow<Pair<C, A>, Pair<C, B>> second() {
    return pair -> new Pair<>(pair.first(), apply(pair.second()));
  }

  /// Fan-out: applies both `this` and `g` to the same input and returns a pair of results.
  default <C> Arrow<A, Pair<B, C>> fanout(Arrow<A, C> g) {
    return a -> new Pair<>(apply(a), g.apply(a));
  }
}
