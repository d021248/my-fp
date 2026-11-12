package d021248.mytry.core.lang.fp.functions.silentinvoker;

import java.net.URI;
import java.net.URL;
import java.util.List;

/// Demonstrates {@link CallUnchecked} for suppressing checked exceptions inside lambdas.
///
/// Shows URL parsing in a stream — the `URL(String)` constructor declares
/// `MalformedURLException` (checked), which normally cannot appear in a lambda.
/// `callUnchecked` wraps it transparently.
///
/// **Note:** The deprecated `URL(String)` constructor is used intentionally here
/// to show the problem `callUnchecked` solves. Prefer `URI.create(s).toURL()` in
/// production code.
///
/// @see [Hacking Lambda Expressions in
// Java](https://dzone.com/articles/hacking-lambda-expressions-in-java)
@SuppressWarnings("deprecation") // URL(String) constructor used intentionally for the demo
public final class SilentInvokerDemo implements CallUnchecked {

  private static final String URL_LOCALHOST = "http://localhost/";
  private static final String URL_DZONE = "https://dzone.com";
  private static final String URL_INVALID = "xxxxx://dzone.com";

  public static void main() {
    var demo = new SilentInvokerDemo();

    System.out.println("Valid URLs:");
    System.out.println(demo.getList());

    System.out.println("\nWith invalid URL (will throw RuntimeException):");
    try {
      System.out.println(demo.getListWithException());
    } catch (RuntimeException e) {
      System.err.println("Caught: " + e.getMessage());
      if (e.getCause() instanceof Exception cause)
        System.err.println(
            "Caused by: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
    }

    System.out.println("\nModern approach using URI:");
    System.out.println(demo.getListModern());
  }

  private List<URL> getList() {
    return List.of(URL_LOCALHOST, URL_DZONE).stream()
        .map(url -> callUnchecked(() -> new URL(url)))
        .toList();
  }

  private List<URL> getListWithException() {
    return List.of(URL_LOCALHOST, URL_INVALID).stream()
        .map(url -> callUnchecked(() -> new URL(url)))
        .toList();
  }

  private List<URL> getListModern() {
    return List.of(URL_LOCALHOST, URL_DZONE).stream()
        .map(uri -> callUnchecked(() -> URI.create(uri).toURL()))
        .toList();
  }
}
