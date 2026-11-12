package d021248.mytry.core.lang.fp.profunctor;

import java.util.function.UnaryOperator;

/// Demonstrates `ProfunctorFn` and `StrongFn`.
///
/// Shows `dimap`, `contramap`, and `rmap` on a simple `String → Integer` function,
/// then demonstrates `first` and `second` on a `StrongFn`.
public final class ProfunctorDemo {

  public static void main() {
    System.out.println("=== ProfunctorFn: generalized function with dimap ===");

    ProfunctorFn<String, Integer> len = ProfunctorFn.of(String::length);
    System.out.println("len(\"hello\") = " + len.apply("hello"));

    // dimap: pre-process input AND post-process output
    var trimmedDoubleLen = len.dimap(String::trim, n -> n * 2);
    System.out.println("dimap(trim, n*2)(\"  hi  \") = " + trimmedDoubleLen.apply("  hi  "));

    // contramap: only pre-process
    UnaryOperator<String> toLower = String::toLowerCase;
    var lowerCaseLen = len.contramap(toLower);
    System.out.println("contramap(toLowerCase)(\"HELLO\") = " + lowerCaseLen.apply("HELLO"));

    // rmap: only post-process
    var lenString = len.rmap(n -> "len=" + n);
    System.out.println("rmap(n->\"len=\"+n)(\"world\") = " + lenString.apply("world"));

    System.out.println("\n=== StrongFn: operates on one half of a pair ===");
    var upper = StrongFn.<String, String>of(String::toUpperCase);

    var upperFirst = upper.first();
    System.out.println(
        "upper.first().apply((\"hello\", 42)) = "
            + upperFirst.apply(new StrongFn.Pair<>("hello", 42)));

    var upperSecond = upper.<Integer>second();
    System.out.println(
        "upper.second().apply((42, \"world\")) = "
            + upperSecond.apply(new StrongFn.Pair<>(42, "world")));
  }
}
