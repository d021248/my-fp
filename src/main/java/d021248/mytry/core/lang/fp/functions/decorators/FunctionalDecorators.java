package d021248.mytry.core.lang.fp.functions.decorators;

import d021248.mytry.core.utils.Stopwatch;
import java.util.HashMap;
import java.util.function.Function;

/// Function decorators — composable wrappers over `Function<A, B>`.
///
/// Each decorator takes a function and returns a new function with added behaviour,
/// leaving the original untouched. Decorators compose freely via `andThen` or nesting.
///
/// | Decorator | Adds |
/// |---|---|
/// | `memoize` | caches results by input; calls the underlying function at most once per distinct
/// input |
/// | `withLogging` | prints start/done around each call |
/// | `withTiming` | prints elapsed time in µs after each call |
/// | `withRetry` | retries on `RuntimeException` up to `maxAttempts` times |
/// | `withFallback` | returns a static fallback value when the function throws |
public interface FunctionalDecorators {

  static <A, B> Function<A, B> memoize(Function<A, B> f) {
    HashMap<A, B> cache = new HashMap<>();
    return input -> cache.computeIfAbsent(input, f);
  }

  static <A, B> Function<A, B> withLogging(String name, Function<A, B> f) {
    return input -> {
      System.out.println("Starting [%s] with input: %s".formatted(name, input));
      var result = f.apply(input);
      System.out.println("Done [%s] -> result: %s".formatted(name, result));
      return result;
    };
  }

  static <A, B> Function<A, B> withTiming(String name, Function<A, B> f) {
    return input -> {
      var sw = Stopwatch.start();
      var result = f.apply(input);
      System.out.println("[%s] %s".formatted(name, sw.elapsed()));
      return result;
    };
  }

  static <A, B> Function<A, B> withRetry(int maxAttempts, Function<A, B> f) {
    return input -> {
      RuntimeException last = null;
      for (int attempt = 0; attempt < maxAttempts; attempt++) {
        try {
          return f.apply(input);
        } catch (RuntimeException e) {
          last = e;
        }
      }
      throw last != null ? last : new IllegalArgumentException("maxAttempts must be >= 1");
    };
  }

  static <A, B> Function<A, B> withFallback(B fallback, Function<A, B> f) {
    return input -> {
      try {
        return f.apply(input);
      } catch (RuntimeException _) {
        return fallback;
      }
    };
  }
}
