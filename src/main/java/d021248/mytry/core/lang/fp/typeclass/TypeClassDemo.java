package d021248.mytry.core.lang.fp.typeclass;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/// Demonstrates {@link Show} and {@link Eq} as type-class instances.
///
/// Three properties make this different from the subclassing approach in
/// {@link d021248.mytry.core.lang.fp.typeclass.broken.SubclassPolymorphismDemo}:
///
/// - **Multiple instances** — `Money` has three different `Show` instances;
///   no wrapper types needed
/// - **External types** — `LocalDate` (a JDK final class) gets a custom
///   `Show` without any modification
/// - **Composition** — `Show<List<Money>>` is derived from `Show<Money>` via
///   `listShow`; no new class is required
public final class TypeClassDemo {

  // ── Domain records ────────────────────────────────────────────────────────

  record Money(double amount, String currency) {}

  record Temperature(double celsius) {
    double toFahrenheit() {
      return celsius * 9.0 / 5.0 + 32;
    }
  }

  // ── Show instances for Money ──────────────────────────────────────────────

  static final Show<Money> moneyShort =
      m -> String.format(Locale.ROOT, "%.2f %s", m.amount(), m.currency());
  static final Show<Money> moneySymbol =
      m ->
          switch (m.currency()) {
            case "EUR" -> String.format(Locale.ROOT, "€%.2f", m.amount());
            case "USD" -> String.format(Locale.ROOT, "$%.2f", m.amount());
            case "GBP" -> String.format(Locale.ROOT, "£%.2f", m.amount());
            default -> moneyShort.show(m);
          };
  static final Show<Money> moneyDebug =
      m -> String.format(Locale.ROOT, "Money{amount=%.2f, currency=%s}", m.amount(), m.currency());

  // ── Show instances for external types (no subclassing possible) ───────────

  static final Show<Temperature> tempCelsius =
      t -> String.format(Locale.ROOT, "%.1f°C", t.celsius());
  static final Show<Temperature> tempFahrenheit =
      t -> String.format(Locale.ROOT, "%.1f°F", t.toFahrenheit());

  // LocalDate is final — can't be subclassed, but type classes work fine
  static final Show<LocalDate> isoDate = LocalDate::toString;
  static final Show<LocalDate> humanDate =
      d -> "%s %d, %d".formatted(d.getMonth().name(), d.getDayOfMonth(), d.getYear());

  // ── Derived instances via combinators ─────────────────────────────────────

  // Show<List<Money>> is derived from Show<Money> — no new class
  static final Show<List<Money>> moneyList = Show.listShow(moneyShort);

  // Show<Optional<Money>>
  static final Show<Optional<Money>> moneyOpt = Show.optionalShow(moneyShort);

  // Show<Temperature> in Fahrenheit derived via contramap from Show<Double>
  static final Show<Double> twoDecimal = d -> String.format(Locale.ROOT, "%.2f", d);
  static final Show<Temperature> derivedFahrenheit =
      twoDecimal.contramap(Temperature::toFahrenheit);

  // ── Eq instances ──────────────────────────────────────────────────────────

  static final Eq<String> caseEq = Eq.caseInsensitive();
  static final Eq<Double> approx = Eq.approximate(0.001);
  // Eq<Money> by amount only, ignoring currency
  static final Eq<Money> byAmount = Eq.<Double>natural().contramap(Money::amount);

  // ── Main ──────────────────────────────────────────────────────────────────

  public static void main() {
    System.out.println("=== Type-Class Instances ===\n");

    var price = new Money(9.99, "EUR");
    var temp = new Temperature(36.6);
    var date = LocalDate.of(2026, 10, 7);

    System.out.println("--- Show<Money> ---");
    System.out.println("short:  " + moneyShort.show(price));
    System.out.println("symbol: " + moneySymbol.show(price));
    System.out.println("debug:  " + moneyDebug.show(price));

    System.out.println("\n--- Show<Temperature> ---");
    System.out.println("celsius:    " + tempCelsius.show(temp));
    System.out.println("fahrenheit: " + tempFahrenheit.show(temp));
    System.out.println("derived °F: " + derivedFahrenheit.show(temp));

    System.out.println("\n--- Show<LocalDate> (external final class) ---");
    System.out.println("iso:   " + isoDate.show(date));
    System.out.println("human: " + humanDate.show(date));

    System.out.println("\n--- Derived: Show<List<Money>> ---");
    System.out.println(moneyList.show(List.of(price, new Money(4.50, "USD"))));

    System.out.println("\n--- Derived: Show<Optional<Money>> ---");
    System.out.println(moneyOpt.show(Optional.of(price)));
    System.out.println(moneyOpt.show(Optional.empty()));

    System.out.println("\n--- Eq instances ---");
    System.out.println("'hello' eq 'HELLO' (case-insensitive): " + caseEq.eq("hello", "HELLO"));
    System.out.println("0.1+0.2 approx eq 0.3: " + approx.eq(0.1 + 0.2, 0.3));
    System.out.println(
        "Money(9.99,EUR) byAmount Money(9.99,USD): " + byAmount.eq(price, new Money(9.99, "USD")));
  }
}
