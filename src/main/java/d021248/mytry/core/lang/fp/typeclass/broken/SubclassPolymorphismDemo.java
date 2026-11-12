package d021248.mytry.core.lang.fp.typeclass.broken;

import java.util.Locale;

/// ANTI-PATTERN: subclassing to add behaviour.
///
/// Attaching behaviour via inheritance looks natural until you hit three walls:
///
///  1. **Two behaviours for the same type** — you cannot extend the same class
///     twice; you need a separate subclass per behaviour.
///  2. **External types** — you cannot add `extends Displayable` to a class
///     from a library you do not own.
///  3. **Multiple behaviours** — deep or diamond hierarchies become
///     unmanageable when a type needs more than one capability.
///
/// See {@link d021248.mytry.core.lang.fp.typeclass.TypeClassDemo}
/// for the correct approach.
public final class SubclassPolymorphismDemo {

  // ── Base class forces inheritance ─────────────────────────────────────────

  abstract static class Displayable {
    abstract String display();
  }

  // ── Each type must extend Displayable (only one display style possible) ───

  static final class Money extends Displayable {
    final double amount;
    final String currency;

    Money(double amount, String currency) {
      this.amount = amount;
      this.currency = currency;
    }

    @Override
    String display() {
      // Only one format possible without another subclass
      return String.format(Locale.ROOT, "%.2f %s", amount, currency);
    }
  }

  static final class Temperature extends Displayable {
    final double celsius;

    Temperature(double celsius) {
      this.celsius = celsius;
    }

    @Override
    String display() {
      return String.format(Locale.ROOT, "%.1f°C", celsius);
    }
  }

  // ── Problem 1: want a second format for Money ─────────────────────────────

  // Must create a separate subclass — cannot reuse Money
  static final class MoneyDebug extends Displayable {
    final Money money;

    MoneyDebug(Money money) {
      this.money = money;
    }

    @Override
    String display() {
      return String.format(
          Locale.ROOT, "Money{amount=%.2f, currency=%s}", money.amount, money.currency);
    }
  }

  // ── Problem 2: java.time.LocalDate cannot extend Displayable ─────────────

  // java.time.LocalDate is final — cannot extend it.
  // The only option is a wrapper, which adds ceremony at every call site.

  // ── Problem 3: need Comparable AND Displayable? ───────────────────────────

  // Java supports single inheritance; Comparable is an interface so that
  // works, but adding a third capability via another abstract class is
  // impossible. Every new capability requires a new hierarchy.

  // ── Main ──────────────────────────────────────────────────────────────────

  public static void main() {
    var price = new Money(9.99, "EUR");
    var temp = new Temperature(36.6);

    System.out.println(price.display()); // 9.99 EUR
    System.out.println(temp.display()); // 36.6°C

    // Second format requires a wrapper
    System.out.println(new MoneyDebug(price).display());

    System.out.println("\nSee typeclass/TypeClassDemo for the correct approach.");
  }
}
