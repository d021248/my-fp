package d021248.mytry.core.lang.fp.functions.decorators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FunctionalDecoratorsTest {

  // ── memoize ───────────────────────────────────────────────────────────────

  @Test
  @DisplayName("memoize returns correct result")
  void memoizeReturnsCorrectResult() {
    var double_ = FunctionalDecorators.memoize((Integer n) -> n * 2);
    assertThat(double_.apply(5)).isEqualTo(10);
    assertThat(double_.apply(0)).isEqualTo(0);
  }

  @Test
  @DisplayName("memoize calls the underlying function only once per distinct input")
  void memoizeCallsFunctionOncePerInput() {
    var calls = new AtomicInteger();
    Function<Integer, Integer> counted =
        n -> {
          calls.incrementAndGet();
          return n * 2;
        };
    var memoized = FunctionalDecorators.memoize(counted);

    memoized.apply(3);
    memoized.apply(3);
    memoized.apply(3);
    assertThat(calls.get()).isEqualTo(1);

    memoized.apply(7);
    assertThat(calls.get()).isEqualTo(2);
  }

  @Test
  @DisplayName("memoize cache is isolated per memoize() call")
  void memoizeCacheIsIsolatedPerCall() {
    var calls = new AtomicInteger();
    Function<Integer, Integer> counted =
        n -> {
          calls.incrementAndGet();
          return n;
        };

    var a = FunctionalDecorators.memoize(counted);
    var b = FunctionalDecorators.memoize(counted);

    a.apply(1);
    b.apply(1);
    assertThat(calls.get()).isEqualTo(2);
  }

  // ── withLogging ───────────────────────────────────────────────────────────

  @Test
  @DisplayName("withLogging returns the same result as the wrapped function")
  void withLoggingReturnsCorrectResult() {
    var logged = FunctionalDecorators.withLogging("test", (Integer n) -> n + 1);
    assertThat(logged.apply(4)).isEqualTo(5);
  }

  @Test
  @DisplayName("withLogging invokes the wrapped function exactly once per call")
  void withLoggingInvokesWrappedFunctionOnce() {
    var calls = new ArrayList<Integer>();
    Function<Integer, Integer> tracked =
        n -> {
          calls.add(n);
          return n;
        };
    var logged = FunctionalDecorators.withLogging("test", tracked);

    logged.apply(10);
    logged.apply(20);

    assertThat(calls).isEqualTo(List.of(10, 20));
  }

  @Test
  @DisplayName("withLogging inside memoize logs only on cache miss")
  void withLoggingInsideMemoizeLogsOnlyOnCacheMiss() {
    var computeCount = new AtomicInteger();
    Function<Integer, Integer> counted =
        n -> {
          computeCount.incrementAndGet();
          return n * 3;
        };
    var fast = FunctionalDecorators.memoize(FunctionalDecorators.withLogging("x", counted));

    fast.apply(5);
    fast.apply(5);
    fast.apply(5);

    assertThat(computeCount.get()).isEqualTo(1);
  }

  // ── withTiming ────────────────────────────────────────────────────────────

  @Test
  @DisplayName("withTiming returns the same result as the wrapped function")
  void withTimingReturnsCorrectResult() {
    var timed = FunctionalDecorators.withTiming("test", (Integer n) -> n * 2);
    assertThat(timed.apply(6)).isEqualTo(12);
  }

  @Test
  @DisplayName("withTiming invokes the wrapped function exactly once per call")
  void withTimingInvokesWrappedFunctionOnce() {
    var calls = new AtomicInteger();
    var timed =
        FunctionalDecorators.withTiming(
            "test",
            (Integer n) -> {
              calls.incrementAndGet();
              return n;
            });

    timed.apply(1);
    timed.apply(2);

    assertThat(calls.get()).isEqualTo(2);
  }

  // ── withRetry ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("withRetry returns result when function succeeds on the first attempt")
  void withRetrySucceedsImmediately() {
    var retried = FunctionalDecorators.withRetry(3, (Integer n) -> n + 1);
    assertThat(retried.apply(9)).isEqualTo(10);
  }

  @Test
  @DisplayName("withRetry retries and succeeds when function fails then succeeds")
  void withRetrySucceedsAfterFailures() {
    var attempts = new AtomicInteger();
    Function<String, String> flaky =
        s -> {
          if (attempts.incrementAndGet() < 3) throw new RuntimeException("transient");
          return s.toUpperCase();
        };
    var retried = FunctionalDecorators.withRetry(5, flaky);
    assertThat(retried.apply("hello")).isEqualTo("HELLO");
    assertThat(attempts.get()).isEqualTo(3);
  }

  @Test
  @DisplayName("withRetry propagates exception when all attempts fail")
  void withRetryPropagatesOnExhaustion() {
    Function<String, String> alwaysFails =
        _ -> {
          throw new RuntimeException("always");
        };
    var retried = FunctionalDecorators.withRetry(3, alwaysFails);
    assertThatRuntimeException().isThrownBy(() -> retried.apply("x")).withMessage("always");
  }

  // ── withFallback ──────────────────────────────────────────────────────────

  @Test
  @DisplayName("withFallback returns function result when no exception is thrown")
  void withFallbackReturnsNormalResult() {
    var safe = FunctionalDecorators.<String, Integer>withFallback(-1, Integer::parseInt);
    assertThat(safe.apply("42")).isEqualTo(42);
  }

  @Test
  @DisplayName("withFallback returns fallback value when function throws")
  void withFallbackReturnsFallbackOnException() {
    var safe = FunctionalDecorators.<String, Integer>withFallback(-1, Integer::parseInt);
    assertThat(safe.apply("oops")).isEqualTo(-1);
  }

  @Test
  @DisplayName("withFallback fallback can be null")
  void withFallbackAllowsNullFallback() {
    var safe =
        FunctionalDecorators.<String, String>withFallback(
            null,
            _ -> {
              throw new RuntimeException();
            });
    assertThat(safe.apply("x")).isNull();
  }
}
