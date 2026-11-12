package d021248.mytry.core.lang.fp.functions.silentinvoker;

import java.lang.invoke.MethodType;
import java.util.concurrent.Callable;

/// Internal functional interface used by {@link java.lang.invoke.LambdaMetafactory}
/// to create a lambda that invokes {@link Callable} instances without checked exceptions.
///
/// This interface is package-private; use {@link CallUnchecked} instead.
///
/// @see [Hacking Lambda Expressions in
// Java](https://dzone.com/articles/hacking-lambda-expressions-in-java)
@FunctionalInterface
interface SilentInvoker {

  /// Method signature for the `invoke` method: `(Callable) → Object`.
  MethodType SIGNATURE = MethodType.methodType(Object.class, Callable.class);

  /// Invokes the callable and returns its result, propagating any exception unchecked.
  <V> V invoke(Callable<V> callable);
}
