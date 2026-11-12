package d021248.mytry.core.lang.fp.selective;

import module java.base;

import d021248.mytry.core.lang.adt.Either;

/// Selective applicative functor over `Optional`.
///
/// `SelectiveOpt<A>` wraps an `Optional<A>` and adds the `select` operation,
/// which fills the expressiveness gap between `Applicative` and `Monad`:
///
/// | Abstraction | Both branches | Second depends on first |
/// |-------------|---------------|-------------------------|
/// | Applicative | always        | no                      |
/// | **Selective** | **no**      | **conditionally**       |
/// | Monad       | no            | yes                     |
///
/// ## The `select` operation
///
/// ```
/// select :: f (Either a b) → f (a→b) → f b
/// ```
///
/// - If the wrapped `Either` is `Right(b)`: return `b` **immediately** — the
///   function `f` is never evaluated.
/// - If the wrapped `Either` is `Left(a)`: **require** the function and apply
///   it to `a`.
/// - If either argument is empty: propagate empty.
///
/// This "conditional effect" cannot be expressed with Applicative (which must
/// evaluate both arguments) without resorting to the full power of Monad.
///
/// ## Example
///
/// ```java
/// // Left — function is applied
/// var result1 = SelectiveOpt.select(
///     SelectiveOpt.pure(Either.left(5)),
///     SelectiveOpt.pure(n -> n * 2));
/// // result1 contains 10
///
/// // Right — function is skipped entirely (even if it would fail)
/// var result2 = SelectiveOpt.select(
///     SelectiveOpt.pure(Either.right("hello")),
///     SelectiveOpt.empty());
/// // result2 contains "hello"
/// ```
public final class SelectiveOpt<A> {

  private final Optional<A> inner;

  private SelectiveOpt(Optional<A> inner) {
    this.inner = inner;
  }

  // ─── Constructors ─────────────────────────────────────────────────────────

  /// Wraps `value` in a present `SelectiveOpt`.
  public static <A> SelectiveOpt<A> pure(A value) {
    return new SelectiveOpt<>(Optional.of(value));
  }

  /// Returns an absent `SelectiveOpt`.
  public static <A> SelectiveOpt<A> empty() {
    return new SelectiveOpt<>(Optional.empty());
  }

  // ─── Functor ─────────────────────────────────────────────────────────────

  /// Transforms the wrapped value; propagates empty.
  public <B> SelectiveOpt<B> map(Function<A, B> f) {
    return new SelectiveOpt<>(inner.map(f));
  }

  // ─── Applicative ─────────────────────────────────────────────────────────

  /// Sequential application: apply a wrapped function to a wrapped value.
  ///
  /// Both `f` and `a` must be present; returns empty if either is absent.
  public static <A, B> SelectiveOpt<B> ap(SelectiveOpt<Function<A, B>> f, SelectiveOpt<A> a) {
    return new SelectiveOpt<>(f.inner.flatMap(fn -> a.inner.map(fn)));
  }

  // ─── Selective ───────────────────────────────────────────────────────────

  /// The selective operation — conditionally applies a function.
  ///
  /// - `Right(b)` in `eitherF` → return `pure(b)`, `f` is not evaluated.
  /// - `Left(a)` in `eitherF` → require `f` and return `f.apply(a)`.
  /// - Either argument absent → return empty.
  public static <A, B> SelectiveOpt<B> select(
      SelectiveOpt<Either<A, B>> eitherF, SelectiveOpt<Function<A, B>> f) {
    return new SelectiveOpt<>(
        eitherF.inner.flatMap(
            e ->
                switch (e) {
                  case Either.Right<A, B>(var b) -> Optional.of(b);
                  case Either.Left<A, B>(var a) -> f.inner.map(fn -> fn.apply(a));
                }));
  }

  // ─── Accessors ───────────────────────────────────────────────────────────

  /// Returns the underlying `Optional<A>`.
  public Optional<A> toOptional() {
    return inner;
  }

  /// Returns `true` if a value is present.
  public boolean isPresent() {
    return inner.isPresent();
  }

  /// Returns `true` if no value is present.
  public boolean isEmpty() {
    return inner.isEmpty();
  }

  @Override
  public String toString() {
    return "SelectiveOpt[%s]".formatted(inner.map(Object::toString).orElse("empty"));
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof SelectiveOpt<?> other && inner.equals(other.inner);
  }

  @Override
  public int hashCode() {
    return inner.hashCode();
  }
}
