package d021248.mytry.core.lang.fp.cofree;

/// Demonstrates [CofreeStream]: natural numbers, Fibonacci, and
/// a running-average transform via `extend`.
public final class CofreeDemo {

  public static void main() {
    System.out.println("=== CofreeStream: Cofree over the identity functor ===");

    // Infinite stream of natural numbers
    var nats = CofreeStream.iterate(0, n -> n + 1);
    System.out.println("\nFirst 10 naturals: " + nats.take(10));

    // Fibonacci via unfold
    record FibState(long a, long b) {}
    var fibs =
        CofreeStream.unfold(
            new FibState(0L, 1L),
            s -> new CofreeStream.Pair<>(s.a(), new FibState(s.b(), s.a() + s.b())));
    System.out.println("First 10 fibs:     " + fibs.take(10));

    // Running sum: extend to compute head + next at each position
    var runningSum = nats.extend(ctx -> ctx.head() + ctx.tail().get().head());
    System.out.println("\nRunning pair-sums (n + n+1) for first 5:");
    System.out.println(runningSum.take(5));

    // Map: double every element
    var doubled = nats.take(5);
    System.out.println("\nFirst 5 naturals doubled: " + nats.map(n -> n * 2).take(5));

    // Drop
    System.out.println("After dropping 5, next head: " + nats.drop(5).head());
  }
}
