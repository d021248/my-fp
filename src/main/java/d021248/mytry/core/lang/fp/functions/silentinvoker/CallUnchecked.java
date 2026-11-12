package d021248.mytry.core.lang.fp.functions.silentinvoker;

import java.util.concurrent.Callable;

/// Interface that provides a default method to invoke {@link Callable} instances
/// without declaring checked exceptions.
///
/// Useful for using methods that throw checked exceptions inside lambdas and
/// stream operations where a `throws` clause is not permitted.
///
/// **Warning:** All exceptions (checked and unchecked) propagate as-is, bypassing
/// the compiler's checked-exception rules via type erasure. Ensure appropriate
/// error handling at a higher level.
///
/// ```java
/// public class MyService implements CallUnchecked {
///     public List<URL> parseUrls(List<String> urlStrings) {
///         return urlStrings.stream()
///                 .map(url -> callUnchecked(() -> new URL(url)))
///                 .toList();
///     }
/// }
/// ```
///
/// @see [Hacking Lambda Expressions in
// Java](https://dzone.com/articles/hacking-lambda-expressions-in-java)
public interface CallUnchecked {

  /// Invokes the given callable, propagating any exception unchecked.
  ///
  /// @param <V>      the return type of the callable
  /// @param callable the callable to invoke
  /// @return the result of the callable invocation
  default <V> V callUnchecked(Callable<V> callable) {
    return SilentInvokerFactory.getSilentInvoker().invoke(callable);
  }
}
