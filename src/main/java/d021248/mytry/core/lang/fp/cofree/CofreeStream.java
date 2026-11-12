package d021248.mytry.core.lang.fp.cofree;

import module java.base;

/// A lazy, potentially infinite stream — {@code Cofree} instantiated over the
/// identity functor.
///
/// ## Connection to the Cofree comonad
///
/// `Cofree F A` = `(A, F (Cofree F A))`: a value at every node, with children
/// determined by the functor `F`.  When `F` is the identity (`Supplier`), each
/// node has exactly one lazy successor, giving an infinite stream.  When `F` is
/// `List`, each node has many successors — a rose tree.
///
/// ## Comonad operations
///
/// - `extract()` — the value at the current head (dual of `pure`)
/// - `extend(f)` — replace every element with a context-sensitive computation
///   (dual of `flatMap`)
///
/// The comonad laws hold:
///
/// - `extend(ctx -> ctx.extract())` is the identity
/// - `extend(f).extract()` equals `f(this)`
public record CofreeStream<A>(A head, Supplier<CofreeStream<A>> tail) {

  /// Creates a stream with the given head and lazy tail.
  public static <A> CofreeStream<A> of(A head, Supplier<CofreeStream<A>> tail) {
    return new CofreeStream<>(head, tail);
  }

  /// Generates an infinite stream by repeatedly applying `f` to the seed.
  public static <A> CofreeStream<A> iterate(A seed, UnaryOperator<A> f) {
    return new CofreeStream<>(seed, () -> iterate(f.apply(seed), f));
  }

  /// Unfolds an infinite stream from a seed using a step function that
  /// produces a `(value, nextSeed)` pair at each position.
  public static <S, A> CofreeStream<A> unfold(S seed, Function<S, Pair<A, S>> step) {
    var pair = step.apply(seed);
    return new CofreeStream<>(pair.value(), () -> unfold(pair.nextSeed(), step));
  }

  /// A `(value, nextSeed)` pair used by {@link #unfold}.
  public record Pair<A, S>(A value, S nextSeed) {}

  /// Returns the value at the head of this stream (comonad `extract`).
  public A extract() {
    return head;
  }

  /// Transforms every element lazily (functor `map`).
  public <B> CofreeStream<B> map(Function<A, B> f) {
    return new CofreeStream<>(f.apply(head), () -> tail.get().map(f));
  }

  /// Applies a context-sensitive function at every position (comonad `extend`).
  ///
  /// `f` receives the stream _starting_ at each position, so it can look at
  /// the head, the tail, or any number of future elements.
  public <B> CofreeStream<B> extend(Function<CofreeStream<A>, B> f) {
    return new CofreeStream<>(f.apply(this), () -> tail.get().extend(f));
  }

  /// Collects the first `n` elements into an immutable list.
  public List<A> take(int n) {
    var result = new ArrayList<A>(n);
    var current = this;
    for (int i = 0; i < n; i++) {
      result.add(current.head);
      current = current.tail.get();
    }
    return List.copyOf(result);
  }

  /// Skips the first `n` elements.
  public CofreeStream<A> drop(int n) {
    var current = this;
    for (int i = 0; i < n; i++) {
      current = current.tail.get();
    }
    return current;
  }
}
