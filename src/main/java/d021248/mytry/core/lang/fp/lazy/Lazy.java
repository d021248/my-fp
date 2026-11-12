package d021248.mytry.core.lang.fp.lazy;

import java.util.function.Function;
import java.util.function.Supplier;

/// Thread-safe memoized deferred computation.
///
/// A `Lazy<T>` wraps a `Supplier<T>` and guarantees that the supplier is
/// called **at most once**, no matter how many threads call {@link #get()}
/// concurrently. The result is cached and returned on every subsequent call.
///
/// ## Null-safe sentinel
///
/// The implementation uses a private `UNSET` sentinel object instead of
/// `null` to distinguish "not yet evaluated" from "evaluated to null". A
/// supplier that legitimately returns `null` is therefore evaluated exactly
/// once, and `null` is cached and returned on every subsequent call.
///
/// ## Thread safety
///
/// Double-checked locking on a `volatile` field guarantees safe publication
/// under the Java Memory Model. The evaluation never repeats even under
/// concurrent access.
///
/// ## Example
/// ```java
/// Lazy<Config> config = Lazy.of(() -> loadFromDisk());
/// // nothing loaded yet
/// config.get(); // loads, result cached
/// config.get(); // returns cached value, supplier not called again
/// Lazy<String> upper = config.map(c -> c.host().toUpperCase());
/// upper.get(); // evaluates config (if not yet) then applies map fn
/// ```
///
/// @param <T> the type of the deferred value
public final class Lazy<T> {

  @SuppressWarnings("rawtypes")
  private static final Supplier NO_SUPPLIER =
      () -> {
        throw new AssertionError("unreachable");
      };

  private static final Object UNSET = new Object();

  private final Supplier<? extends T> supplier;
  private volatile Object value = UNSET;

  private Lazy(Supplier<? extends T> supplier) {
    this.supplier = supplier;
  }

  // -------------------------------------------------------------------------
  // Factory
  // -------------------------------------------------------------------------

  /// Returns a new lazy wrapper around `supplier`.
  ///
  /// The supplier is called at most once, on the first call to {@link #get()}.
  ///
  /// @param supplier the deferred computation; must not be null
  public static <T> Lazy<T> of(Supplier<? extends T> supplier) {
    if (supplier == null) throw new NullPointerException("supplier must not be null");
    return new Lazy<>(supplier);
  }

  /// Returns a `Lazy<T>` that is already evaluated and holds `value`.
  ///
  /// {@link #get()} returns `value` immediately; {@link #isEvaluated()} returns
  /// `true`.
  ///
  /// @param value the eager value (may be null)
  @SuppressWarnings("unchecked")
  public static <T> Lazy<T> eager(T value) {
    var lazy = new Lazy<T>(NO_SUPPLIER);
    lazy.value = value;
    return lazy;
  }

  // -------------------------------------------------------------------------
  // Core
  // -------------------------------------------------------------------------

  /// Returns the value, computing it on the first call and caching it forever.
  @SuppressWarnings("unchecked")
  public T get() {
    var v = value;
    if (v == UNSET) {
      synchronized (this) {
        v = value;
        if (v == UNSET) {
          value = v = supplier.get();
        }
      }
    }
    return (T) v;
  }

  /// Returns `true` if the value has already been computed and cached.
  public boolean isEvaluated() {
    return value != UNSET;
  }

  // -------------------------------------------------------------------------
  // Transform
  // -------------------------------------------------------------------------

  /// Returns a new `Lazy<U>` that applies `fn` to this value when first
  /// evaluated.
  ///
  /// The mapping is itself deferred: `fn` is not called until {@link #get()}
  /// is invoked on the returned `Lazy`.
  ///
  /// @param fn the mapping function; must not be null
  public <U> Lazy<U> map(Function<? super T, ? extends U> fn) {
    if (fn == null) throw new NullPointerException("fn must not be null");
    return Lazy.of(() -> fn.apply(this.get()));
  }

  /// Sequences this `Lazy` with another produced by `fn`.
  ///
  /// Allows chaining of deferred computations without nesting `Lazy<Lazy<T>>`.
  /// Neither this value nor `fn` is evaluated until {@link #get()} is called
  /// on the returned `Lazy`.
  ///
  /// @param fn produces the next `Lazy` from this value; must not be null
  public <U> Lazy<U> flatMap(Function<? super T, ? extends Lazy<? extends U>> fn) {
    if (fn == null) throw new NullPointerException("fn must not be null");
    return Lazy.of(() -> fn.apply(this.get()).get());
  }

  @Override
  public String toString() {
    var v = value;
    return v == UNSET ? "Lazy[unevaluated]" : "Lazy[%s]".formatted(v);
  }
}
