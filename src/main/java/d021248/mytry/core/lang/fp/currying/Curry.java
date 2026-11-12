package d021248.mytry.core.lang.fp.currying;

import java.util.function.Function;

/// Currying and partial application in Java — without overload explosions.
///
/// In Haskell every function is automatically curried: `f a b c` is
/// syntactic sugar for `((f a) b) c`. Partial application is free.
/// In Scala named + default parameters cover the simpler case.
///
/// Java has neither, but with functional interfaces and lambdas we can express
/// the same ideas — more verbosely, but without duplicating business logic
/// across dozens of overloads.
///
/// ## Key types
///
///   - {@link Fn2} — a two-argument function with `partial` and
///       `curried` views
///   - {@link Fn3} — same for three arguments
public final class Curry {

  private Curry() {}

  // -------------------------------------------------------------------------
  // Fn2: (A, B) → R
  // -------------------------------------------------------------------------

  @FunctionalInterface
  public interface Fn2<A, B, R> {
    R apply(A a, B b);

    /// Fix the first argument, return a single-arg function.
    default Function<B, R> partial(A a) {
      return b -> apply(a, b);
    }

    /// Convert to curried form: A → (B → R).
    default Function<A, Function<B, R>> curried() {
      return a -> b -> apply(a, b);
    }

    /// Flip argument order: (B, A) → R.
    default Fn2<B, A, R> flip() {
      return (b, a) -> apply(a, b);
    }
  }

  // -------------------------------------------------------------------------
  // Fn3: (A, B, C) → R
  // -------------------------------------------------------------------------

  @FunctionalInterface
  public interface Fn3<A, B, C, R> {
    R apply(A a, B b, C c);

    /// Fix the first argument.
    default Fn2<B, C, R> partial(A a) {
      return (b, c) -> apply(a, b, c);
    }

    /// Fix the first two arguments.
    default Function<C, R> partial(A a, B b) {
      return c -> apply(a, b, c);
    }

    /// Convert to fully curried form: A → B → C → R.
    default Function<A, Function<B, Function<C, R>>> curried() {
      return a -> b -> c -> apply(a, b, c);
    }
  }

  // -------------------------------------------------------------------------
  // Utility constructors (make lambdas type-safe without explicit casts)
  // -------------------------------------------------------------------------

  public static <A, B, R> Fn2<A, B, R> fn2(Fn2<A, B, R> f) {
    return f;
  }

  public static <A, B, C, R> Fn3<A, B, C, R> fn3(Fn3<A, B, C, R> f) {
    return f;
  }
}
