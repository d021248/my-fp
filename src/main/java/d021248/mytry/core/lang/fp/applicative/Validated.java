package d021248.mytry.core.lang.fp.applicative;

import module java.base;

/// Validates a value, accumulating ALL errors — not stopping at the first.
///
/// The Functor → Applicative → Monad hierarchy in one type:
///
/// | Level | Key operation | Effect |
/// |-------|--------------|--------|
/// | Functor | `map` | transform a valid value |
/// | **Applicative** | `ap`, `liftA2` | combine independent results |
/// | Monad | `flatMap` | sequence dependent steps |
///
/// The decisive difference between `ap` and `flatMap`: when both the function
/// and the argument are invalid, `ap` **merges** both error lists; `flatMap`
/// discards the second.
///
/// ## Example
///
/// ```java
/// var name  = validateName("");        // Invalid("name blank")
/// var age   = validateAge(-5);         // Invalid("age negative")
/// var email = validateEmail("bad");    // Invalid("bad email")
///
/// // ap accumulates all three errors
/// Validated.liftA2((n, a) -> ..., name, age);  // Invalid(["name blank", "age negative"])
/// ```
public sealed interface Validated<E, A> permits Validated.Valid, Validated.Invalid {

  /// A successful validation wrapping the result value.
  record Valid<E, A>(A value) implements Validated<E, A> {}

  /// A failed validation accumulating one or more error values.
  record Invalid<E, A>(List<E> errors) implements Validated<E, A> {}

  /// Wraps a value in a successful `Validated`.
  static <E, A> Validated<E, A> valid(A value) {
    return new Valid<>(value);
  }

  /// Wraps a single error in a failed `Validated`.
  static <E, A> Validated<E, A> invalid(E error) {
    return new Invalid<>(List.of(error));
  }

  /// Wraps a list of errors in a failed `Validated`.
  static <E, A> Validated<E, A> invalid(List<E> errors) {
    return new Invalid<>(List.copyOf(errors));
  }

  /// Returns `true` if this is a successful validation.
  default boolean isValid() {
    return this instanceof Valid;
  }

  /// Transforms a valid value; propagates failures unchanged.
  default <B> Validated<E, B> map(Function<A, B> f) {
    return switch (this) {
      case Valid<E, A> v -> Validated.valid(f.apply(v.value()));
      case Invalid<E, A> i -> Validated.invalid(i.errors());
    };
  }

  /// Applies a wrapped function to this value, **accumulating errors from both sides**.
  ///
  /// When both `vf` and `this` are invalid, their error lists are merged.
  /// This is what distinguishes Applicative (independent) from Monad (sequential).
  default <B> Validated<E, B> ap(Validated<E, Function<A, B>> vf) {
    return switch (vf) {
      case Valid<E, Function<A, B>> validFn ->
          switch (this) {
            case Valid<E, A> va -> Validated.valid(validFn.value().apply(va.value()));
            case Invalid<E, A> ia -> Validated.invalid(ia.errors());
          };
      case Invalid<E, Function<A, B>> invalidFn ->
          switch (this) {
            case Valid<E, A> _ -> Validated.invalid(invalidFn.errors());
            case Invalid<E, A> ia -> {
              var combined = new ArrayList<>(invalidFn.errors());
              combined.addAll(ia.errors());
              yield Validated.invalid(List.copyOf(combined));
            }
          };
    };
  }

  /// Sequences a dependent step; short-circuits on the first failure.
  ///
  /// Prefer `ap` / `liftA2` when steps are independent.
  default <B> Validated<E, B> flatMap(Function<A, Validated<E, B>> f) {
    return switch (this) {
      case Valid<E, A> v -> f.apply(v.value());
      case Invalid<E, A> i -> Validated.invalid(i.errors());
    };
  }

  /// Applies a pure binary function to two independent validations,
  /// accumulating errors from both when either or both fail.
  static <E, A, B, C> Validated<E, C> liftA2(
      BiFunction<A, B, C> f, Validated<E, A> va, Validated<E, B> vb) {
    return vb.ap(va.map(a -> b -> f.apply(a, b)));
  }
}
