package d021248.mytry.core.lang.fp.optics;

import java.util.List;

/// Demonstrates `Iso` with temperature conversion, string splitting, and composition.
///
/// This class shows how bidirectional, lossless transformations are expressed as
/// composable `Iso` values.
public final class IsoDemo {

  private IsoDemo() {}

  // ── Temperature domain ────────────────────────────────────────────────────

  record Celsius(double degrees) {}

  record Fahrenheit(double degrees) {}

  // ── Isos ──────────────────────────────────────────────────────────────────

  /// Celsius ↔ Fahrenheit: `F = C × 9/5 + 32`
  static final Iso<Celsius, Fahrenheit> celsiusToFahrenheit =
      Iso.of(
          c -> new Fahrenheit(c.degrees() * 9.0 / 5.0 + 32.0),
          f -> new Celsius((f.degrees() - 32.0) * 5.0 / 9.0));

  /// Splits a comma-separated `String` into a `List<String>` and joins it back.
  static final Iso<String, List<String>> splitOnComma =
      Iso.of(s -> List.of(s.split(",", -1)), parts -> String.join(",", parts));

  // ── Demo ──────────────────────────────────────────────────────────────────

  public static void main() {
    // ── Temperature: get, reverse, modify ─────────────────────────────────
    var boiling = new Celsius(100.0);
    var fahrenheit = celsiusToFahrenheit.get(boiling);
    System.out.println("Boiling in Fahrenheit: %s".formatted(fahrenheit));

    var backToCelsius = celsiusToFahrenheit.reverseGet(fahrenheit);
    System.out.println("Back to Celsius: %s".formatted(backToCelsius));

    var warm = new Celsius(20.0);
    var cooled = celsiusToFahrenheit.modify(warm, f -> new Fahrenheit(f.degrees() - 5.0));
    System.out.println("20°C cooled by 5°F then back: %s".formatted(cooled));

    // ── reverse() produces the inverse Iso ────────────────────────────────
    var fahrenheitToCelsius = celsiusToFahrenheit.reverse();
    System.out.println(
        "212°F in Celsius: %s".formatted(fahrenheitToCelsius.get(new Fahrenheit(212.0))));

    // ── String ↔ List<String> ─────────────────────────────────────────────
    var csv = "one,two,three";
    var parts = splitOnComma.get(csv);
    System.out.println("Split '%s' → %s".formatted(csv, parts));
    System.out.println("Joined back: '%s'".formatted(splitOnComma.reverseGet(parts)));

    // ── asLens() downcast ─────────────────────────────────────────────────
    var lens = celsiusToFahrenheit.asLens();
    System.out.println("Via lens, get boiling: %s".formatted(lens.get(boiling)));
    var updatedC = lens.set(boiling, new Fahrenheit(32.0));
    System.out.println("Set boiling to 32°F (0°C): %s".formatted(updatedC));

    // ── asPrism() downcast ────────────────────────────────────────────────
    var prism = celsiusToFahrenheit.asPrism();
    System.out.println("Via prism, getOption boiling: %s".formatted(prism.getOption(boiling)));
  }
}
