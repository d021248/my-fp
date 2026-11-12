package d021248.mytry.core.lang.fp.arrows;

import module java.base;

/// Demonstrates Hughes-style arrows compared with plain `Function` composition.
public final class ArrowDemo {

  private ArrowDemo() {}

  public static void main() {
    // ── Sequential composition ─────────────────────────────────────────────
    var addOne = Arrow.arr((Integer n) -> n + 1);
    var doubled = Arrow.arr((Integer n) -> n * 2);
    var addThenDouble = addOne.andThen(doubled);

    System.out.println("addThenDouble(3) = %d".formatted(addThenDouble.apply(3))); // 8

    // ── Fan-out ────────────────────────────────────────────────────────────
    Arrow<String, Integer> length = Arrow.arr(String::length);
    Arrow<String, String> upper = Arrow.arr(String::toUpperCase);
    var bothArrow = length.fanout(upper);

    var result = bothArrow.apply("hello");
    System.out.println("length=%d, upper=%s".formatted(result.first(), result.second()));

    // ── first / second ─────────────────────────────────────────────────────
    Arrow<Integer, Integer> times3 = Arrow.arr(n -> n * 3);
    Arrow<Arrow.Pair<Integer, String>, Arrow.Pair<Integer, String>> onFirst = times3.first();

    var pairResult = onFirst.apply(new Arrow.Pair<>(4, "unchanged"));
    System.out.println("first times3: (%d, %s)".formatted(pairResult.first(), pairResult.second()));

    // ── Function.andThen vs Arrow.andThen ──────────────────────────────────
    Function<Integer, Integer> funcAdd = n -> n + 1;
    Function<Integer, Integer> funcMul = n -> n * 2;
    var funcComposed = funcAdd.andThen(funcMul);

    System.out.println("Function.andThen(3) = %d".formatted(funcComposed.apply(3)));
    System.out.println("Arrow.andThen(3)    = %d".formatted(addThenDouble.apply(3)));
  }
}
