package d021248.mytry.core.lang.fp.bifunctor;

import module java.base;

/// A type that can be mapped over both of its type parameters simultaneously.
///
/// `Bifunctor<A, B>` generalises `Functor` to two type parameters.
/// Where a `Functor<F>` provides `map: (A → B) → F<A> → F<B>`,
/// a `Bifunctor<A, B>` provides `bimap: (A → C, B → D) → Bifunctor<A,B> → Bifunctor<C,D>`.
///
/// ## Sealed hierarchy
///
/// ```
/// Bifunctor<A, B>
/// ├── Pair<A, B>            — product type: holds both A and B
/// └── Either<L, R>          — sum type: holds exactly one of L or R
///     ├── Either.Left<L, R>
///     └── Either.Right<L, R>
/// ```
///
/// ## Laws
///
/// 1. **Identity**: `bimap(identity(), identity())` ≡ `identity()`
/// 2. **Composition**: `bimap(f.andThen(h), g.andThen(k))` ≡
///    `bimap(f, g).bimap(h, k)`
///
/// ## Relation to Functor
///
/// `Pair` is a bifunctor; `Pair.mapSecond` recovers the standard `Functor`
/// behaviour over the second type parameter (first type fixed).
/// `Either` is also a bifunctor; in practice `mapSecond` acts like `map` for
/// the `Right` case (the conventional "success" side).
///
/// @param <A> the first type parameter
/// @param <B> the second type parameter
public sealed interface Bifunctor<A, B> permits Bifunctor.Pair, Bifunctor.Either {

  // ── Abstract operation ────────────────────────────────────────────────────

  /// Applies `f` to the first type and `g` to the second type simultaneously.
  <C, D> Bifunctor<C, D> bimap(Function<A, C> f, Function<B, D> g);

  // ── Derived convenience methods ───────────────────────────────────────────

  /// Maps only the first type parameter, leaving the second unchanged.
  default <C> Bifunctor<C, B> mapFirst(Function<A, C> f) {
    return bimap(f, b -> b);
  }

  /// Maps only the second type parameter, leaving the first unchanged.
  default <D> Bifunctor<A, D> mapSecond(Function<B, D> g) {
    return bimap(a -> a, g);
  }

  // ── Pair ─────────────────────────────────────────────────────────────────

  /// A product type that holds a value of type `A` and a value of type `B`.
  ///
  /// Both values are always present; `bimap` transforms each independently.
  ///
  /// @param <A> the first component type
  /// @param <B> the second component type
  record Pair<A, B>(A first, B second) implements Bifunctor<A, B> {

    /// Transforms both components independently, returning a new `Pair`.
    @Override
    public <C, D> Pair<C, D> bimap(Function<A, C> f, Function<B, D> g) {
      return new Pair<>(f.apply(first), g.apply(second));
    }
  }

  // ── Either ────────────────────────────────────────────────────────────────

  /// A sum type that holds exactly one of `L` (left) or `R` (right).
  ///
  /// By convention `Left` represents the "other" or error case, and `Right`
  /// represents the "happy path" — mirroring the `Result` / `Either` idiom.
  ///
  /// `bimap` applies `f` if the value is `Left`, and `g` if it is `Right`.
  ///
  /// @param <L> the left (first) type parameter
  /// @param <R> the right (second) type parameter
  sealed interface Either<L, R> extends Bifunctor<L, R>
      permits Bifunctor.Either.Left, Bifunctor.Either.Right {

    /// The left variant — the first type parameter carries the value.
    record Left<L, R>(L value) implements Either<L, R> {

      /// Applies `f` to the left value; `g` is not called.
      @Override
      public <C, D> Left<C, D> bimap(Function<L, C> f, Function<R, D> g) {
        return new Left<>(f.apply(value));
      }
    }

    /// The right variant — the second type parameter carries the value.
    record Right<L, R>(R value) implements Either<L, R> {

      /// Applies `g` to the right value; `f` is not called.
      @Override
      public <C, D> Right<C, D> bimap(Function<L, C> f, Function<R, D> g) {
        return new Right<>(g.apply(value));
      }
    }

    // ── Either factory helpers ─────────────────────────────────────────

    /// Creates a `Left` variant.
    static <L, R> Either<L, R> left(L value) {
      return new Left<>(value);
    }

    /// Creates a `Right` variant.
    static <L, R> Either<L, R> right(R value) {
      return new Right<>(value);
    }

    /// Returns true iff this is a `Left`.
    default boolean isLeft() {
      return this instanceof Left<L, R>;
    }

    /// Returns true iff this is a `Right`.
    default boolean isRight() {
      return this instanceof Right<L, R>;
    }
  }
}
