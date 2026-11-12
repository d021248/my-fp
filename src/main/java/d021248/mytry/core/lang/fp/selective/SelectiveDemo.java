package d021248.mytry.core.lang.fp.selective;

import module java.base;

import d021248.mytry.core.lang.adt.Either;

/// Demonstrates the selective applicative functor.
///
/// Shows the key distinction between `Applicative` and `Selective`: the
/// function argument to `select` is only evaluated when the `Either` is
/// `Left`. When the `Either` is `Right`, the function is skipped entirely.
public final class SelectiveDemo {

  private SelectiveDemo() {}

  public static void main() {

    // ── Left: function is required and applied ────────────────────────────

    var leftCase =
        SelectiveOpt.select(
            SelectiveOpt.pure(Either.<Integer, Integer>left(5)), SelectiveOpt.pure(n -> n * 2));
    System.out.println("Left(5), fn=n*2        → " + leftCase); // SelectiveOpt[10]

    // ── Right: function is skipped (even when the function SelectiveOpt is empty)
    // ─

    SelectiveOpt<Either<Void, String>> rightEither = SelectiveOpt.pure(Either.right("hello"));
    SelectiveOpt<Function<Void, String>> noFn = SelectiveOpt.empty();
    var rightCase = SelectiveOpt.select(rightEither, noFn);
    System.out.println("Right(hello), fn=empty → " + rightCase); // SelectiveOpt[hello]

    // ── Empty Either: propagates empty ────────────────────────────────────

    SelectiveOpt<Either<Integer, Integer>> emptyEither = SelectiveOpt.empty();
    var emptyCase = SelectiveOpt.select(emptyEither, SelectiveOpt.pure(n -> n + 100));
    System.out.println("Empty either, fn=n+100 → " + emptyCase); // SelectiveOpt[empty]

    // ── Left with empty function: propagates empty ────────────────────────

    SelectiveOpt<Function<Integer, Integer>> emptyFn = SelectiveOpt.empty();
    var leftEmptyFn =
        SelectiveOpt.select(SelectiveOpt.pure(Either.<Integer, Integer>left(7)), emptyFn);
    System.out.println("Left(7), fn=empty      → " + leftEmptyFn); // SelectiveOpt[empty]

    // ── Applicative ap: both must be present ──────────────────────────────

    var doubled = SelectiveOpt.ap(SelectiveOpt.pure((Integer n) -> n * 2), SelectiveOpt.pure(21));
    System.out.println("ap(n*2, 21)            → " + doubled); // SelectiveOpt[42]

    SelectiveOpt<Function<Integer, Integer>> emptyAp = SelectiveOpt.empty();
    var failedAp = SelectiveOpt.ap(emptyAp, SelectiveOpt.pure(21));
    System.out.println("ap(empty, 21)          → " + failedAp); // SelectiveOpt[empty]
  }
}
