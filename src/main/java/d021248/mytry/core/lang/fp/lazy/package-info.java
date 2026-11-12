/// Thread-safe memoized deferred computation via
/// {@link d021248.mytry.core.lang.fp.lazy.Lazy}.
///
/// The evaluation is guaranteed to run at most once regardless of how many
/// threads call `get()` concurrently; the result is cached forever.
///
/// @since 1.0
package d021248.mytry.core.lang.fp.lazy;
