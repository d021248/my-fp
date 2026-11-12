package d021248.mytry.core.lang.fp.trampoline;

import java.util.function.Supplier;

/// A trampoline — Java's answer to the lack of tail-call optimisation.
///
/// ## The problem
///
/// The JVM does not perform TCO. Every recursive call pushes a frame; deep
/// recursion throws {@link StackOverflowError}. Functional languages with TCO
/// (Haskell, Scala, Erlang, F#) guarantee that tail-recursive functions run in
/// O(1) stack space.
///
/// ## The pattern
///
/// Instead of calling itself recursively, a function returns a description of
/// the *next step*. The trampoline driver loop executes steps one at a
/// time on the same stack frame until a final result is reached.
///
/// ```java
/// Bounce<Long> factStep(int n, long acc) {
///     if (n <= 1) return Bounce.done(acc);
///     return Bounce.more(() -> factStep(n - 1, (long) n * acc)); // no stack growth
/// }
///
/// long result = Bounce.run(factStep(100_000, 1L));
/// ```
///
/// ## Cost
///
/// Each step allocates a {@link More} record (one {@link Supplier} lambda).
/// This is much cheaper than a stack frame, and GC-friendly: the previous step
/// is released as soon as the next one is evaluated.
///
/// @param <T> the result type
public sealed interface Bounce<T> permits Bounce.Done, Bounce.More {

  /// Terminal step — holds the final result.
  record Done<T>(T value) implements Bounce<T> {}

  /// Continuation — holds the next step as a lazy thunk.
  record More<T>(Supplier<Bounce<T>> next) implements Bounce<T> {}

  // -------------------------------------------------------------------------
  // Factory helpers
  // -------------------------------------------------------------------------

  static <T> Bounce<T> done(T value) {
    return new Done<>(value);
  }

  static <T> Bounce<T> more(Supplier<Bounce<T>> next) {
    return new More<>(next);
  }

  // -------------------------------------------------------------------------
  // Driver
  // -------------------------------------------------------------------------

  /// Runs the trampoline loop until a {@link Done} is reached.
  /// Uses O(1) stack space regardless of how many steps are taken.
  static <T> T run(Bounce<T> bounce) {
    while (bounce instanceof More<T> more) {
      bounce = more.next().get();
    }
    return ((Done<T>) bounce).value();
  }
}
