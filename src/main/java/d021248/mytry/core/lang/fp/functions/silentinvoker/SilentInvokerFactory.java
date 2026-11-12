package d021248.mytry.core.lang.fp.functions.silentinvoker;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.Callable;

/// Factory that creates a {@link SilentInvoker} singleton via
/// {@link LambdaMetafactory}.
///
/// Uses `invokedynamic` and method handles to generate a synthetic lambda at
/// class-initialization time; the lambda invokes `Callable.call()` without
/// declaring checked exceptions in its signature.
///
/// @see [Hacking Lambda Expressions in
// Java](https://dzone.com/articles/hacking-lambda-expressions-in-java)
class SilentInvokerFactory {

  private static final SilentInvoker SILENT_INVOKER = createSilentInvoker();

  private SilentInvokerFactory() {}

  private static SilentInvoker createSilentInvoker() {
    var lookup = MethodHandles.lookup();
    try {
      CallSite site =
          LambdaMetafactory.metafactory(
              lookup,
              "invoke",
              MethodType.methodType(SilentInvoker.class),
              SilentInvoker.SIGNATURE,
              lookup.findVirtual(Callable.class, "call", MethodType.methodType(Object.class)),
              SilentInvoker.SIGNATURE);
      return (SilentInvoker) site.getTarget().invokeExact();
    } catch (Throwable e) {
      throw new RuntimeException("Failed to create SilentInvoker", e);
    }
  }

  static SilentInvoker getSilentInvoker() {
    return SILENT_INVOKER;
  }
}
