package d021248.mytry.core.lang.fp.currying.broken;

import java.util.List;
import java.util.function.IntUnaryOperator;

/// ANTI-PATTERN: simulating default parameters and partial application
/// through overloading in Java.
///
/// In Haskell every function is automatically curried:
///
/// ```java
/// -- Haskell
/// clamp :: Int -> Int -> Int -> Int
/// clamp lo hi x = max lo (min hi x)
///
/// clamp0to100 = clamp 0 100   -- partial application, free
/// clamp0to100 75               -- 75
/// clamp0to100 150              -- 100
/// ```
///
/// In Scala / Kotlin named + default arguments cover the simpler case:
///
/// ```java
/// // Kotlin
/// fun repeat(text: String, times: Int = 3, sep: String = ", "): String = ...
/// repeat("hi")                   // "hi, hi, hi"
/// repeat("hi", sep = " | ")      // "hi | hi | hi"
/// ```
///
/// Java has neither. The result is an *overload explosion*: one method
/// per combination of present/absent parameters. The explosion grows
/// exponentially with the number of optional parameters.
public final class OverloadExplosionDemo {

  // ---- String repeat — 4 overloads for 2 optional parameters ----

  static String repeat(String text, int times, String sep) {
    var sb = new StringBuilder();
    for (int i = 0; i < times; i++) {
      if (i > 0) sb.append(sep);
      sb.append(text);
    }
    return sb.toString();
  }

  static String repeat(String text, int times) {
    return repeat(text, times, ", ");
  }

  static String repeat(String text, String sep) {
    return repeat(text, 3, sep);
  }

  static String repeat(String text) {
    return repeat(text, 3, ", ");
  }

  // ---- clamp — partial application requires a new wrapper ----

  static int clamp(int lo, int hi, int x) {
    return Math.max(lo, Math.min(hi, x));
  }

  // To "partially apply" clamp(0, 100, _) Java needs an explicit lambda or helper
  // — there is no syntax for it
  static IntUnaryOperator clamp0to100() {
    return x -> clamp(0, 100, x); // written by hand every time
  }

  // ---- format — 3 optional params → 8 overloads (only 4 shown) ----

  static String format(String value, int width, boolean leftAlign, char pad) {
    String s = leftAlign ? value : " ".repeat(Math.max(0, width - value.length())) + value;
    return s.length() < width ? s + String.valueOf(pad).repeat(width - s.length()) : s;
  }

  static String format(String value, int width, boolean leftAlign) {
    return format(value, width, leftAlign, ' ');
  }

  static String format(String value, int width) {
    return format(value, width, false, ' ');
  }

  static String format(String value) {
    return format(value, 10, false, ' ');
  }

  // ... and so on for every other combination

  public static void main() {
    System.out.println(repeat("hi"));
    System.out.println(repeat("hi", 4));
    System.out.println(repeat("hi", " | "));

    var clip = clamp0to100();
    List.of(-5, 50, 150).stream().map(clip::applyAsInt).forEach(System.out::println);

    System.out.println("\nProblems: N optional params → 2^N overloads.");
    System.out.println("No named arguments, no partial application syntax.");
  }
}
