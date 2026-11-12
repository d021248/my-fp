package d021248.mytry.core.lang.fp.functions;

import java.sql.SQLException;

/// Demonstrates a Java type-system trick to throw checked exceptions without
/// declaring them.
///
/// **Warning:** This is a language-quirk demonstration, NOT for production use.
///
/// **How it works:** Java's type erasure allows us to fool the compiler:
///
/// - A generic method declares `throws E` where `E extends Exception`
/// - At the call site we supply `<RuntimeException>` as the type argument
/// - The compiler sees a `RuntimeException` throw — no checked-exception rule applies
/// - At runtime, type erasure discards the bound, so the actual `SQLException`
///   propagates unchecked
///
/// @see [Type Erasure](https://docs.oracle.com/javase/tutorial/java/generics/erasure.html)
public final class NoCheckedException {

  // No throws clause here — but we throw SQLException anyway!
  public static void main() {
    doThrow(new SQLException());
  }

  static void doThrow(Exception e) {
    NoCheckedException.<RuntimeException>doThrow0(e);
  }

  @SuppressWarnings("unchecked")
  static <E extends Exception> void doThrow0(Exception e) throws E {
    throw (E) e;
  }
}
