package d021248.mytry.core.lang.adt;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/// Classic sum type — a value is *either* a {@link Left} or a {@link Right}.
///
/// By convention, {@link Left} carries the "failure" or "alternative" value and
/// {@link Right} carries the "success" or "primary" value. The map/flatMap
/// operations are right-biased: they transform the {@link Right} and leave
/// {@link Left} untouched.
///
/// ## Comparison to Result
///
/// {@link d021248.mytry.core.lang.errorhandling.result.Result} is a specialised
/// `Either<Exception, T>`. `Either` is the general-purpose version with no
/// opinion about what the left type represents.
///
/// ## Monad operations
///
/// | Method | Signature | Behaviour |
/// |--------|-----------|-----------|
/// | `map` | `(R → R2) → Either<L, R2>` | transforms Right; Left passes through |
/// | `flatMap` | `(R → Either<L, R2>) → Either<L, R2>` | chains computations |
/// | `mapLeft` | `(L → L2) → Either<L2, R>` | transforms Left; Right passes through |
/// | `fold` | `(L → T, R → T) → T` | eliminates the Either into a single value |
/// | `swap` | `() → Either<R, L>` | Left ↔ Right exchange |
///
/// @param <L> the left ("alternative") type
/// @param <R> the right ("primary") type
public sealed interface Either<L, R> permits Either.Left, Either.Right {

  record Left<L, R>(L value) implements Either<L, R> {}

  record Right<L, R>(R value) implements Either<L, R> {}

  // ── factory ──────────────────────────────────────────────────────────

  static <L, R> Either<L, R> left(L value) {
    return new Left<>(value);
  }

  static <L, R> Either<L, R> right(R value) {
    return new Right<>(value);
  }

  // ── query ────────────────────────────────────────────────────────────

  default boolean isLeft() {
    return this instanceof Left<L, R>;
  }

  default boolean isRight() {
    return this instanceof Right<L, R>;
  }

  // ── transform (right-biased) ─────────────────────────────────────────

  /// Maps the Right value; Left passes through unchanged.
  default <R2> Either<L, R2> map(Function<? super R, ? extends R2> f) {
    return switch (this) {
      case Left(var v) -> new Left<>(v);
      case Right(var v) -> new Right<>(f.apply(v));
    };
  }

  /// Chains a function that itself returns an Either.
  default <R2> Either<L, R2> flatMap(Function<? super R, Either<L, R2>> f) {
    return switch (this) {
      case Left(var v) -> new Left<>(v);
      case Right(var v) -> f.apply(v);
    };
  }

  /// Maps the Left value; Right passes through unchanged.
  default <L2> Either<L2, R> mapLeft(Function<? super L, ? extends L2> f) {
    return switch (this) {
      case Left(var v) -> new Left<>(f.apply(v));
      case Right(var v) -> new Right<>(v);
    };
  }

  // ── elimination ──────────────────────────────────────────────────────

  /// Collapses this Either into a single value.
  default <T> T fold(
      Function<? super L, ? extends T> onLeft, Function<? super R, ? extends T> onRight) {
    return switch (this) {
      case Left(var v) -> onLeft.apply(v);
      case Right(var v) -> onRight.apply(v);
    };
  }

  /// Executes `onLeft` or `onRight` depending on the variant.
  default Either<L, R> peek(Consumer<? super L> onLeft, Consumer<? super R> onRight) {
    switch (this) {
      case Left(var v) -> onLeft.accept(v);
      case Right(var v) -> onRight.accept(v);
    }
    return this;
  }

  // ── swap ─────────────────────────────────────────────────────────────

  /// Exchanges Left and Right.
  default Either<R, L> swap() {
    return switch (this) {
      case Left(var v) -> new Right<>(v);
      case Right(var v) -> new Left<>(v);
    };
  }

  // ── extraction ───────────────────────────────────────────────────────

  /// Returns the Right value or throws.
  default R getRight() {
    return switch (this) {
      case Right(var v) -> v;
      case Left(_) -> throw new NoSuchElementException("getRight() on Left");
    };
  }

  /// Returns the Right value, or `fallback` if this is a Left.
  default R orElse(R fallback) {
    return switch (this) {
      case Right(var v) -> v;
      case Left(_) -> fallback;
    };
  }

  /// Converts to an Optional — present for Right, empty for Left.
  default Optional<R> toOptional() {
    return switch (this) {
      case Right(var v) -> Optional.of(v);
      case Left(_) -> Optional.empty();
    };
  }
}
