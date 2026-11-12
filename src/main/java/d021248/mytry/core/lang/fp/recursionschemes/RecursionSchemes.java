package d021248.mytry.core.lang.fp.recursionschemes;

import java.util.function.Function;

/// Recursion schemes: catamorphism, anamorphism, hylomorphism, paramorphism,
/// and apomorphism.
///
/// A **recursion scheme** is a higher-order function that captures a canonical
/// pattern of structural recursion — fold, unfold, or both — and separates it
/// from the per-node logic (the algebra or coalgebra).
///
/// ## How it works
///
/// 1. Replace the recursive type with a *base functor* `F<A>` where `A` stands
///    for the child positions. `ExprF<A>` does this for arithmetic expressions.
/// 2. Take the fixed point `Fix(F<Fix>)` to recover a proper recursive tree.
/// 3. Define `fmap` to lift `A → B` to `F<A> → F<B>`.
/// 4. Express every traversal in terms of `cata`, `ana`, `hylo`, `para`, or `apo`.
///
/// ## Schemes
///
/// | Scheme | Direction | Algebra / Coalgebra | Nickname |
/// |--------|-----------|---------------------|----------|
/// | `cata` | bottom-up fold | `F<A> → A` | catamorphism |
/// | `ana`  | top-down unfold | `A → F<A>` | anamorphism |
/// | `hylo` | unfold then fold | both | hylomorphism |
/// | `para` | bottom-up fold with subtree | `F<(Fix, A)> → A` | paramorphism |
/// | `apo`  | top-down unfold with early exit | `A → F<Either<Fix,A>>` | apomorphism |
///
/// ## Example
///
/// ```java
/// // Build a tree:  (2 * 3) + 4
/// var expr = Fix.add(Fix.mul(Fix.num(2), Fix.num(3)), Fix.num(4));
///
/// // Evaluate it — no explicit recursion in the algebra
/// int result = RecursionSchemes.cata(e -> switch (e) {
///     case ExprF.Num<Integer> n -> n.value();
///     case ExprF.Add<Integer> a -> a.left() + a.right();
///     case ExprF.Mul<Integer> m -> m.left() * m.right();
/// }, expr);
/// System.out.println(result); // 10
/// ```
public final class RecursionSchemes {

  private RecursionSchemes() {}

  // ─── Base functor ──────────────────────────────────────────────────────

  /// The base functor for arithmetic expressions.
  ///
  /// By replacing recursive self-references with the type parameter `A`,
  /// `ExprF<A>` becomes non-recursive. The fixed point `Fix(ExprF<Fix>)` is
  /// the full recursive tree.
  public sealed interface ExprF<A> {

    /// A numeric literal.
    record Num<A>(int value) implements ExprF<A> {}

    /// An addition of two sub-expressions.
    record Add<A>(A left, A right) implements ExprF<A> {}

    /// A multiplication of two sub-expressions.
    record Mul<A>(A left, A right) implements ExprF<A> {}
  }

  // ─── Fixed point ──────────────────────────────────────────────────────

  /// The fixed point of `ExprF`: a fully-recursive arithmetic expression tree.
  ///
  /// Use the factory methods to construct trees without double-wrapping:
  ///
  /// ```java
  /// var expr = Fix.add(Fix.mul(Fix.num(2), Fix.num(3)), Fix.num(4));
  /// ```
  public record Fix(ExprF<Fix> out) {

    /// Constructs a numeric literal node.
    public static Fix num(int value) {
      return new Fix(new ExprF.Num<>(value));
    }

    /// Constructs an addition node.
    public static Fix add(Fix left, Fix right) {
      return new Fix(new ExprF.Add<>(left, right));
    }

    /// Constructs a multiplication node.
    public static Fix mul(Fix left, Fix right) {
      return new Fix(new ExprF.Mul<>(left, right));
    }
  }

  // ─── Functor instance for ExprF ────────────────────────────────────────

  /// Lifts `f :: A → B` to `ExprF<A> → ExprF<B>` (the Functor `fmap`).
  public static <A, B> ExprF<B> fmap(Function<A, B> f, ExprF<A> expr) {
    return switch (expr) {
      case ExprF.Num<A> n -> new ExprF.Num<>(n.value());
      case ExprF.Add<A> a -> new ExprF.Add<>(f.apply(a.left()), f.apply(a.right()));
      case ExprF.Mul<A> m -> new ExprF.Mul<>(f.apply(m.left()), f.apply(m.right()));
    };
  }

  // ─── Recursion schemes ─────────────────────────────────────────────────

  /// Catamorphism (fold): reduces a `Fix` tree bottom-up using `algebra`.
  ///
  /// The algebra handles a single layer with already-evaluated children;
  /// `cata` handles the traversal.
  ///
  /// @param algebra maps a fully-evaluated layer `ExprF<A>` to a result `A`
  /// @param expr    the expression tree to fold
  public static <A> A cata(Function<ExprF<A>, A> algebra, Fix expr) {
    return algebra.apply(fmap(e -> cata(algebra, e), expr.out()));
  }

  /// Anamorphism (unfold): builds a `Fix` tree top-down from a seed.
  ///
  /// The coalgebra produces one layer from a seed; `ana` unfolds recursively.
  ///
  /// @param coalgebra maps a seed `A` to one layer `ExprF<A>` of the tree
  /// @param seed      the initial value to unfold
  public static <A> Fix ana(Function<A, ExprF<A>> coalgebra, A seed) {
    return new Fix(fmap(a -> ana(coalgebra, a), coalgebra.apply(seed)));
  }

  /// Hylomorphism (unfold then fold): equivalent to `cata(algebra, ana(coalgebra, seed))`
  /// but without materialising the intermediate `Fix` tree.
  ///
  /// @param algebra    reducer applied bottom-up
  /// @param coalgebra  expander applied top-down
  /// @param seed       initial value to unfold
  public static <A, B> B hylo(
      Function<ExprF<B>, B> algebra, Function<A, ExprF<A>> coalgebra, A seed) {
    return algebra.apply(fmap(a -> hylo(algebra, coalgebra, a), coalgebra.apply(seed)));
  }

  // ─── Para and Apo ──────────────────────────────────────────────────────

  /// A pair of an original `Fix` subtree and a computed result `A`.
  ///
  /// Used by `para` to give the algebra simultaneous access to the original
  /// subtree and the recursively-folded result.
  public record Pair<A>(Fix subtree, A result) {}

  /// A sum type used by `apo` to signal "stop here" vs "keep unfolding".
  ///
  /// This is a **scheme-local** type — it is not the general-purpose
  /// `Either<L,R>` from `core.lang.adt`. Here `Left` always carries a `Fix`
  /// subtree (meaning "stop; reuse this") and `Right` carries a seed `A`
  /// (meaning "keep unfolding"). The single type parameter `A` reflects the
  /// seed type only.
  ///
  /// - `Left(fix)` — stop unfolding; reuse the existing subtree as-is.
  /// - `Right(a)` — continue unfolding with seed `a`.
  public sealed interface Either<A>
      permits RecursionSchemes.Either.Left, RecursionSchemes.Either.Right {

    /// Stop: reuse the existing `Fix` subtree unchanged.
    record Left<A>(Fix fix) implements Either<A> {}

    /// Continue: unfold further with seed `a`.
    record Right<A>(A seed) implements Either<A> {}

    /// Smart constructor — stop and reuse `fix`.
    static <A> Either<A> stop(Fix fix) {
      return new Left<>(fix);
    }

    /// Smart constructor — continue unfolding with `seed`.
    static <A> Either<A> next(A seed) {
      return new Right<>(seed);
    }
  }

  /// Paramorphism (fold with original subtree): like `cata`, but the algebra
  /// receives a pair `(original Fix subtree, recursed result)` at every node.
  ///
  /// This lets you inspect the original structure (e.g. check whether a child
  /// is a literal) at the same time as consuming its evaluated form — something
  /// `cata` alone cannot do.
  ///
  /// @param algebra maps `ExprF<Pair<Fix, A>>` to a result `A`
  /// @param expr    the expression tree to fold
  public static <A> A para(Function<ExprF<Pair<A>>, A> algebra, Fix expr) {
    return algebra.apply(fmap(e -> new Pair<>(e, para(algebra, e)), expr.out()));
  }

  /// Apomorphism (unfold with early exit): like `ana`, but the coalgebra
  /// may stop early by returning `Either.stop(existingFix)` instead of
  /// always producing new seeds.
  ///
  /// This lets you "splice" an already-constructed `Fix` subtree into the
  /// result during unfolding, without having to unfold it further.
  ///
  /// @param coalgebra maps a seed `A` to one `ExprF` layer, where each
  ///                  position is `Either.stop(fix)` (reuse) or `Either.next(a)` (unfold)
  /// @param seed      the initial value to unfold
  public static <A> Fix apo(Function<A, ExprF<Either<A>>> coalgebra, A seed) {
    return new Fix(
        fmap(
            e ->
                switch (e) {
                  case Either.Left<A>(var fix) -> fix;
                  case Either.Right<A>(var next) -> apo(coalgebra, next);
                },
            coalgebra.apply(seed)));
  }
}
