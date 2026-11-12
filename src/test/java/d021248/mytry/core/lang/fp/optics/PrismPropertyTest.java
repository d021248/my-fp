package d021248.mytry.core.lang.fp.optics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

/// Property-based tests for {@link Prism} verifying the two round-trip laws
/// for *any* randomly generated input.
///
/// ## Prism laws
///
/// | Law | Statement |
/// |---|---|
/// | Review–getOption | `getOption(review(a)) == Optional.of(a)` |
/// | getOption–review | if `getOption(s) == Optional.of(a)` then `review(a) == s` |
///
/// ## Sum type under test
///
/// ```
/// sealed Expr permits Num, Add
///   Num(int value)
///   Add(int left, int right)
/// ```
///
/// The `numPrism` targets the `Num` variant.  `@ForAll int` drives law 1
/// directly (any int can be wrapped in a `Num`), and law 2 follows immediately
/// because `review` always produces a `Num`.  Tests for the non-matching case
/// use `Add` values to prove `getOption` returns empty for the wrong variant.
///
/// The existing {@link OpticsTest} covers specific domain scenarios.
/// This class verifies the same laws hold for *any* randomly generated value.
@PropertyDefaults(tries = 300)
class PrismPropertyTest {

  private sealed interface Expr permits Expr.Num, Expr.Add {
    record Num(int value) implements Expr {}

    record Add(int left, int right) implements Expr {}
  }

  private static final Prism<Expr, Integer> numPrism =
      Prism.of(
          s -> s instanceof Expr.Num n ? Optional.of(n.value()) : Optional.empty(),
          n -> new Expr.Num(n));

  // ── Law 1: review–getOption round-trip ────────────────────────────────────

  @Property
  @Label("getOption(review(a)) == Optional.of(a) for any int (law 1)")
  void reviewGetOptionRoundTrip(@ForAll int value) {
    assertThat(numPrism.getOption(numPrism.review(value))).isEqualTo(Optional.of(value));
  }

  // ── Law 2: getOption–review round-trip ───────────────────────────────────

  @Property
  @Label("review(getOption(s).get()) == s when s is the targeted variant (law 2)")
  void getOptionReviewRoundTrip(@ForAll int value) {
    var num = numPrism.review(value);
    assertThat(numPrism.getOption(num).map(numPrism::review)).isEqualTo(Optional.of(num));
  }

  // ── Non-matching variant ──────────────────────────────────────────────────

  @Property
  @Label("getOption returns empty for non-matching variant (Add)")
  void getOptionEmptyForNonTarget(@ForAll int left, @ForAll int right) {
    assertThat(numPrism.getOption(new Expr.Add(left, right))).isEmpty();
  }

  // ── matches ───────────────────────────────────────────────────────────────

  @Property
  @Label("matches returns true for the targeted variant")
  void matchesTrueForTargeted(@ForAll int value) {
    assertThat(numPrism.matches(new Expr.Num(value))).isTrue();
  }

  @Property
  @Label("matches returns false for non-matching variant")
  void matchesFalseForNonTarget(@ForAll int left, @ForAll int right) {
    assertThat(numPrism.matches(new Expr.Add(left, right))).isFalse();
  }

  // ── modify ────────────────────────────────────────────────────────────────

  @Property
  @Label("modify applies the function when the variant matches")
  void modifyWhenPresent(@ForAll int value) {
    var num = new Expr.Num(value);
    var result = numPrism.modify(num, n -> n * 2);
    assertThat(result).isEqualTo(new Expr.Num(value * 2));
  }

  @Property
  @Label("modify is identity when the variant does not match")
  void modifyWhenAbsent(@ForAll int left, @ForAll int right) {
    var add = new Expr.Add(left, right);
    assertThat(numPrism.modify(add, n -> n * 2)).isEqualTo(add);
  }

  // ── andThen (composition) ─────────────────────────────────────────────────

  @Property
  @Label("composed prism satisfies law 1: getOption(review(a)) == Optional.of(a)")
  void composedPrismLaw1(@ForAll int value) {
    // inner prism: total bijection Integer <-> String (works for all ints including
    // MIN_VALUE)
    var innerPrism =
        Prism.<Integer, String>of(n -> Optional.of(String.valueOf(n)), Integer::parseInt);
    var composed = numPrism.andThen(innerPrism);
    var a = String.valueOf(value);
    assertThat(composed.getOption(composed.review(a))).isEqualTo(Optional.of(a));
  }
}
