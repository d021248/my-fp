/// Anti-pattern: recursive memoization via `HashMap.computeIfAbsent`.
///
/// `HashMap.computeIfAbsent` sets an internal modification guard before calling
/// the mapping function. If the mapping function triggers another call to
/// `computeIfAbsent` on the **same map** (reentrant call), the guard fires and
/// throws `ConcurrentModificationException` — even in a single thread.
///
/// The fix: compute the value first, then `put` it separately, so the map is
/// never re-entered during computation.
///
/// @since 1.0
package d021248.mytry.core.lang.fp.functions.decorators.broken;
