package d021248.mytry.core.lang.fp.comonad;

import java.util.Locale;

/// Demonstrates the Store and Env comonads.
///
/// Store models a focusable lookup — like a function from positions to values
/// with a current position. Env models a value that carries its configuration
/// context alongside it.
public final class ComonadDemo {

  // ── Store ─────────────────────────────────────────────────────────────────

  static void storeDemo() {
    System.out.println("=== Store Comonad — Squares Table ===");

    // Lookup table: position = index, value = index²
    var squares = Store.<Integer, Integer>of(n -> n * n, 3);

    System.out.println("extract() at 3       : " + squares.extract()); // 9
    System.out.println("peek(5)              : " + squares.peek(5)); // 25
    System.out.println("seek(7).extract()    : " + squares.seek(7).extract()); // 49

    // Extend: each position becomes the sum of its neighbours' squares
    // pos 3: 2² + 3² + 4² = 4+9+16 = 29
    var ns = squares.extend(s -> s.peek(s.pos() - 1) + s.extract() + s.peek(s.pos() + 1));
    System.out.println("neighbourSum at 3    : " + ns.extract()); // 29
    System.out.println("neighbourSum at 5    : " + ns.seek(5).extract()); // 77 (4²+5²+6²)

    System.out.println("\n=== Store Comonad — 1-D Blur ===");
    double[] signal = {0, 0, 10, 0, 0, 10, 0};
    var store = Store.<Integer, Double>of(i -> i >= 0 && i < signal.length ? signal[i] : 0.0, 0);
    var blurred =
        store.extend(s -> (s.peek(s.pos() - 1) + s.extract() + s.peek(s.pos() + 1)) / 3.0);

    System.out.print("Before: ");
    for (double v : signal) System.out.printf(Locale.ROOT, "%.1f  ", v);
    System.out.println();
    System.out.print("After : ");
    for (int i = 0; i < signal.length; i++)
      System.out.printf(Locale.ROOT, "%.1f  ", blurred.seek(i).extract());
    System.out.println();
  }

  // ── Env ───────────────────────────────────────────────────────────────────

  record Config(int precision) {}

  static void envDemo() {
    System.out.println("\n=== Env Comonad — Config-aware Formatting ===");

    var pi = Env.of(new Config(4), Math.PI);
    System.out.println("extract()          : " + pi.extract());
    System.out.println("ask().precision    : " + pi.ask().precision());

    var formatted = pi.extend(ctx -> ("%." + ctx.ask().precision() + "f").formatted(ctx.extract()));
    System.out.println("formatted (4dp)    : " + formatted.extract()); // 3.1416

    var formatted2 =
        pi.local(cfg -> new Config(2))
            .extend(ctx -> ("%." + ctx.ask().precision() + "f").formatted(ctx.extract()));
    System.out.println("formatted (2dp)    : " + formatted2.extract()); // 3.14

    var doubled = pi.map(v -> v * 2);
    System.out.printf(Locale.ROOT, "doubled pi         : %.6f%n", doubled.extract()); // 6.283...
  }

  public static void main() {
    storeDemo();
    envDemo();
  }
}
