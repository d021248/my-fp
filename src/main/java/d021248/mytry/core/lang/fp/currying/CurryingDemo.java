package d021248.mytry.core.lang.fp.currying;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/// Demonstrates {@link Curry.Fn2} and {@link Curry.Fn3} for partial application
/// and currying without overload explosions.
public final class CurryingDemo {

  public static void main() {

    // ---- clamp: (lo, hi, x) → x clamped to [lo, hi] ----
    Curry.Fn3<Integer, Integer, Integer, Integer> clamp =
        Curry.fn3((lo, hi, x) -> Math.max(lo, Math.min(hi, x)));

    // Partial application: fix lo=0 and hi=100 once
    Function<Integer, Integer> clip = clamp.partial(0, 100);

    System.out.println("=== clamp ===");
    List.of(-5, 50, 150).forEach(x -> System.out.println(x + " → " + clip.apply(x)));

    // ---- repeat: (text, times, sep) → repeated string ----
    Curry.Fn3<String, Integer, String, String> repeat =
        Curry.fn3((text, times, sep) -> String.join(sep, Collections.nCopies(times, text)));

    // Different partial views of the same function
    Function<Integer, Function<String, String>> repeatHi = repeat.curried().apply("hi");
    Function<String, String> repeatHi3 = repeatHi.apply(3);

    System.out.println("\n=== repeat (curried) ===");
    System.out.println(repeatHi3.apply(", ")); // hi, hi, hi
    System.out.println(repeatHi3.apply(" | ")); // hi | hi | hi
    System.out.println(repeatHi.apply(5).apply("-")); // hi-hi-hi-hi-hi

    // ---- format: right-pad a string ----
    Curry.Fn2<String, Integer, String> rightPad =
        Curry.fn2((s, width) -> ("%-" + width + "s").formatted(s));

    Function<Integer, Function<String, String>> column = rightPad.flip().curried();
    Function<String, String> col10 = column.apply(10);
    Function<String, String> col20 = column.apply(20);

    System.out.println("\n=== table (partial application of format) ===");
    List.of("Alice", "Bob", "Charlotte")
        .forEach(name -> System.out.println(col10.apply(name) + "|" + col20.apply("Engineer")));

    // ---- Pipeline: compose partially-applied functions ----
    System.out.println("\n=== pipeline ===");
    Function<Integer, Integer> normalise =
        clip // clamp 0..100
            .andThen(x -> x * 255 / 100); // scale to 0..255

    List.of(-10, 0, 50, 100, 200).forEach(x -> System.out.println(x + " → " + normalise.apply(x)));
  }
}
