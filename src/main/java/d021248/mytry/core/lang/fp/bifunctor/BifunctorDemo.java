package d021248.mytry.core.lang.fp.bifunctor;

import d021248.mytry.core.lang.fp.bifunctor.Bifunctor.Either;
import d021248.mytry.core.lang.fp.bifunctor.Bifunctor.Pair;

/// Demonstrates `Bifunctor` with `Pair` (product) and `Either` (sum) examples.
public final class BifunctorDemo {

  private BifunctorDemo() {}

  public static void main() {
    // ── Pair: bimap transforms both components ────────────────────────────
    var pair = new Pair<>("hello", 3);
    var mappedPair = pair.bimap(String::toUpperCase, n -> n * 2);
    System.out.println("Pair original: %s".formatted(pair));
    System.out.println("Pair after bimap: %s".formatted(mappedPair));

    // ── Pair: mapFirst and mapSecond ──────────────────────────────────────
    var firstOnly = pair.mapFirst(String::length);
    System.out.println("Pair mapFirst (String::length): %s".formatted(firstOnly));

    var secondOnly = pair.mapSecond(n -> n + 10);
    System.out.println("Pair mapSecond (+10): %s".formatted(secondOnly));

    // ── Either: Left case — bimap applies f to the left value ─────────────
    Either<String, Integer> left = Either.left("error: not found");
    var mappedLeft = left.bimap(String::toUpperCase, n -> n * 2);
    System.out.println("Either.Left original: %s".formatted(left));
    System.out.println("Either.Left after bimap: %s".formatted(mappedLeft));

    // ── Either: Right case — bimap applies g to the right value ───────────
    Either<String, Integer> right = Either.right(42);
    var mappedRight = right.bimap(String::toUpperCase, n -> n * 2);
    System.out.println("Either.Right original: %s".formatted(right));
    System.out.println("Either.Right after bimap: %s".formatted(mappedRight));

    // ── Either: mapSecond as a Functor-style map over the happy path ───────
    Either<String, Integer> success = Either.right(7);
    var doubled = success.mapSecond(n -> n * 6);
    System.out.println("Either.Right mapSecond (*6): %s".formatted(doubled));

    Either<String, Integer> failure = Either.left("oops");
    var notMapped = failure.mapSecond(n -> n * 6);
    System.out.println("Either.Left mapSecond (no-op): %s".formatted(notMapped));

    // ── Pattern matching over Either ──────────────────────────────────────
    Either<String, Integer> result = Either.right(100);
    var description =
        switch (result) {
          case Either.Left<String, Integer> l -> "Failed with: " + l.value();
          case Either.Right<String, Integer> r -> "Succeeded with: " + r.value();
        };
    System.out.println("Switch on Either: " + description);
  }
}
