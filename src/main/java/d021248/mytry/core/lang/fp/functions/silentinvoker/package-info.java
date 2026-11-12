/// Utilities for invoking {@link java.util.concurrent.Callable} instances
/// without declaring checked exceptions in lambda expressions.
///
/// ## Overview
///
/// This package implements a pattern that lets methods throwing checked
/// exceptions be used in contexts that cannot declare exceptions, such as
/// Java Streams.
///
/// ## Usage
///
/// Implement {@link d021248.mytry.core.lang.fp.lambda.silentinvoker.CallUnchecked}
/// and call `callUnchecked()`:
///
/// ```java
/// public class UrlParser implements CallUnchecked {
///     public List<URL> parseUrls(List<String> urlStrings) {
///         return urlStrings.stream()
///                 .map(url -> callUnchecked(() -> new URL(url)))
///                 .toList();
///     }
/// }
/// ```
///
/// ## How it works
///
/// {@link java.lang.invoke.LambdaMetafactory} creates a synthetic lambda at
/// class initialization that:
///
/// - Accepts a {@link java.util.concurrent.Callable}
/// - Invokes `Callable.call()` via method handles
/// - Does not declare checked exceptions in its signature
///
/// Type erasure means the JVM discards the `throws` clause at runtime, so
/// the original exception propagates unchecked.
///
/// ## Performance
///
/// The {@link d021248.mytry.core.lang.fp.lambda.silentinvoker.SilentInvoker}
/// instance is created once at class initialization and reused.
///
/// @see d021248.mytry.core.lang.fp.lambda.silentinvoker.CallUnchecked
/// @see [Hacking Lambda Expressions in
// Java](https://dzone.com/articles/hacking-lambda-expressions-in-java)
/// @since 1.0
package d021248.mytry.core.lang.fp.functions.silentinvoker;
