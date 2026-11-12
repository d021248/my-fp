package d021248.mytry.core.lang.fp.monads;

import java.util.function.BinaryOperator;
import java.util.function.Function;

/// The Writer monad: a computation that produces a value `A` together with an
/// accumulated log `W`.
///
/// `Writer<W, A>` holds a `(W, A)` pair. Sequential computations combine their
/// logs using a supplied monoid operation — typically `String::concat` for text
/// logs or list append for list-based logs.
///
/// ## Operations
///
/// | Operation              | Meaning |
/// |------------------------|---------|
/// | `of(log, value)`       | Construct a writer explicitly |
/// | `pure(value, empty)`   | Return value with an empty log |
/// | `tell(log)`            | Emit a log entry; result is `null` |
/// | `map(f)`               | Transform the value; log unchanged |
/// | `flatMap(f, combine)`  | Sequence, combining logs with `combine` |
///
/// ## Example
///
/// ```java
/// Writer<String, Integer> result =
///     Writer.of("start; ", 10)
///           .flatMap(n -> Writer.of("doubled; ", n * 2), String::concat)
///           .flatMap(n -> Writer.of("done; ",   n + 1), String::concat);
///
/// System.out.println(result.log());   // start; doubled; done;
/// System.out.println(result.value()); // 21
/// ```
public record Writer<W, A>(W log, A value) {

  // ─── Constructors ──────────────────────────────────────────────────────

  /// Constructs a writer with the given log entry and value.
  public static <W, A> Writer<W, A> of(W log, A value) {
    return new Writer<>(log, value);
  }

  /// Constructs a writer that returns `value` with an empty log.
  public static <W, A> Writer<W, A> pure(A value, W emptyLog) {
    return new Writer<>(emptyLog, value);
  }

  /// Constructs a writer that emits `log` and produces a null result.
  public static <W> Writer<W, Void> tell(W log) {
    return new Writer<>(log, null);
  }

  // ─── Functor / Monad ──────────────────────────────────────────────────

  /// Transforms the value without changing the accumulated log.
  public <B> Writer<W, B> map(Function<A, B> f) {
    return new Writer<>(log, f.apply(value));
  }

  /// Sequences two writer computations, combining their logs with `combine`.
  ///
  /// @param f       next computation given the current value
  /// @param combine monoid operation that joins two log values
  public <B> Writer<W, B> flatMap(Function<A, Writer<W, B>> f, BinaryOperator<W> combine) {
    var next = f.apply(value);
    return new Writer<>(combine.apply(log, next.log), next.value);
  }
}
