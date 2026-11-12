package d021248.mytry.core.utils;

import java.util.Locale;

/// Immutable wall-clock stopwatch based on `System.nanoTime()`.
///
/// `System.nanoTime()` is used (not `currentTimeMillis`) because it is monotonic
/// — unaffected by wall-clock adjustments — and has nanosecond resolution.
///
/// ## Example
///
/// ```java
/// var sw = Stopwatch.start();
/// doWork();
/// System.out.println(sw.elapsed()); // → "42 ms"  or  "1.23 s"
/// ```
///
/// `Stopwatch` is a record: immutable, safe to pass around, and printable.
/// Calling `elapsed()` multiple times on the same instance returns fresh durations each time
/// because the start point is fixed but the current time advances.
public record Stopwatch(long startNanos) {

  /// Captures the current time and returns a running stopwatch.
  public static Stopwatch start() {
    return new Stopwatch(System.nanoTime());
  }

  /// Returns the elapsed time in nanoseconds since this stopwatch was started.
  public long elapsedNanos() {
    return System.nanoTime() - startNanos;
  }

  /// Returns the elapsed time in whole milliseconds since this stopwatch was started.
  public long elapsedMillis() {
    return elapsedNanos() / 1_000_000L;
  }

  /// Returns a human-readable elapsed time string, auto-scaled to the most appropriate unit.
  ///
  /// | Range | Example output |
  /// |---|---|
  /// | < 1 ms | `"842 µs"` |
  /// | < 1 s | `"123 ms"` |
  /// | ≥ 1 s | `"1.23 s"` |
  public String elapsed() {
    var nanos = elapsedNanos();
    if (nanos < 1_000_000L) return "%d µs".formatted(nanos / 1_000L);
    if (nanos < 1_000_000_000L) return "%d ms".formatted(nanos / 1_000_000L);
    return String.format(Locale.ROOT, "%.2f s", nanos / 1_000_000_000.0);
  }
}
