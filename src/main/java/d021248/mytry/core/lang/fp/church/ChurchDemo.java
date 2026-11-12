package d021248.mytry.core.lang.fp.church;

import static d021248.mytry.core.lang.fp.church.Church.*;

import module java.base;

/// Demonstrates Church encodings of booleans, natural numbers, pairs,
/// and anonymous recursion via the Y combinator.
public final class ChurchDemo {

  public static void main() {
    demonstrateBooleans();
    demonstrateNumerals();
    demonstratePairs();
    demonstrateYCombinator();
  }

  private static void demonstrateBooleans() {
    System.out.println("=== Church Booleans ===");
    System.out.println("TRUE  = " + toBoolean(TRUE));
    System.out.println("FALSE = " + toBoolean(FALSE));
    System.out.println("and(TRUE, FALSE)  = " + toBoolean(and(TRUE, FALSE)));
    System.out.println("or(FALSE, TRUE)   = " + toBoolean(or(FALSE, TRUE)));
    System.out.println("not(TRUE)         = " + toBoolean(not(TRUE)));
    System.out.println("not(not(FALSE))   = " + toBoolean(not(not(FALSE))));
    System.out.println("select: %s".formatted(TRUE.select("heads", "tails")));
  }

  private static void demonstrateNumerals() {
    System.out.println("\n=== Church Numerals ===");
    var zero = ZERO;
    var one = succ(ZERO);
    var two = succ(one);
    var three = fromInt(3);
    var five = add(two, three);
    var six = mul(two, three);
    System.out.println("0            = " + toInt(zero));
    System.out.println("1            = " + toInt(one));
    System.out.println("2 + 3        = " + toInt(five));
    System.out.println("2 * 3        = " + toInt(six));
    System.out.println("isZero(ZERO) = " + isZero(ZERO));
    System.out.println("isZero(ONE)  = " + isZero(one));
  }

  private static void demonstratePairs() {
    System.out.println("\n=== Church Pairs ===");
    var p = pair("hello", 42);
    System.out.println("fst = " + fst(p));
    System.out.println("snd = " + snd(p));
  }

  private static void demonstrateYCombinator() {
    System.out.println("\n=== Y Combinator (anonymous recursion) ===");
    Function<Integer, Integer> factorial = Y(f -> n -> n <= 1 ? 1 : n * f.apply(n - 1));
    Function<Integer, Integer> fibonacci =
        Y(f -> n -> n <= 1 ? n : f.apply(n - 1) + f.apply(n - 2));
    System.out.println("5!       = " + factorial.apply(5));
    System.out.println("10!      = " + factorial.apply(10));
    System.out.println("fib(10)  = " + fibonacci.apply(10));
    System.out.println("fib(15)  = " + fibonacci.apply(15));
  }
}
