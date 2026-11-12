package d021248.mytry.core.lang.fp.optics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.UnaryOperator;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

/// Property-based tests for {@link Iso} verifying the two round-trip laws
/// and the compositional properties for *any* input.
///
/// ## Iso laws
///
/// A well-formed `Iso<S, A>` must satisfy:
///
/// | Law | Statement |
/// |---|---|
/// | get-reverseGet | `get(reverseGet(a)) == a` |
/// | reverseGet-get | `reverseGet(get(s)) == s` |
///
/// ## Isos under test
///
/// - **`negate`** — `Iso<Integer, Integer>` that negates both ways; self-inverse.
/// - **`reverse`** — `Iso<String, String>` that reverses both ways; self-inverse.
///
/// Using self-inverse Isos keeps the test setup minimal while still exercising
/// every code path in {@link Iso} (get, reverseGet, reverse, andThen, modify).
///
/// The existing {@link IsoTest} covers specific domain values (Celsius ↔ Fahrenheit).
/// This class verifies the same laws hold for *any* randomly generated value.
@PropertyDefaults(tries = 300)
class IsoPropertyTest {

  // Self-inverse Isos allow any generated input to serve as both S and A
  private static final Iso<Integer, Integer> negate = Iso.of(i -> -i, i -> -i);
  private static final Iso<String, String> reverse =
      Iso.of(
          s -> new StringBuilder(s).reverse().toString(),
          s -> new StringBuilder(s).reverse().toString());

  // ── Iso laws: negate ──────────────────────────────────────────────────────

  @Property
  @Label("negate: get(reverseGet(a)) == a (get-reverseGet law)")
  void negateGetReverseGet(@ForAll int a) {
    assertThat(negate.get(negate.reverseGet(a))).isEqualTo(a);
  }

  @Property
  @Label("negate: reverseGet(get(s)) == s (reverseGet-get law)")
  void negateReverseGetGet(@ForAll int s) {
    assertThat(negate.reverseGet(negate.get(s))).isEqualTo(s);
  }

  // ── Iso laws: reverse ─────────────────────────────────────────────────────

  @Property
  @Label("reverse: get(reverseGet(a)) == a (get-reverseGet law)")
  void reverseGetReverseGet(@ForAll String a) {
    assertThat(reverse.get(reverse.reverseGet(a))).isEqualTo(a);
  }

  @Property
  @Label("reverse: reverseGet(get(s)) == s (reverseGet-get law)")
  void reverseReverseGetGet(@ForAll String s) {
    assertThat(reverse.reverseGet(reverse.get(s))).isEqualTo(s);
  }

  // ── reverse() ────────────────────────────────────────────────────────────

  @Property
  @Label("iso.reverse() swaps get and reverseGet: reverse().get == iso.reverseGet")
  void reverseSwapsGetAndReverseGet(@ForAll int n) {
    assertThat(negate.reverse().get(n)).isEqualTo(negate.reverseGet(n));
  }

  @Property
  @Label("iso.reverse().reverse() behaves identically to iso: get equals original get")
  void doubleReverseIsOriginal(@ForAll int n) {
    assertThat(negate.reverse().reverse().get(n)).isEqualTo(negate.get(n));
  }

  // ── andThen (composition) ─────────────────────────────────────────────────

  @Property
  @Label("iso.andThen(iso.reverse()) is the identity: get(s) == s")
  void andThenInverseIsIdentityGet(@ForAll int n) {
    var identity = negate.andThen(negate.reverse());
    assertThat(identity.get(n)).isEqualTo(n);
  }

  @Property
  @Label("iso.andThen(iso.reverse()) is the identity: reverseGet(a) == a")
  void andThenInverseIsIdentityReverseGet(@ForAll int n) {
    var identity = negate.andThen(negate.reverse());
    assertThat(identity.reverseGet(n)).isEqualTo(n);
  }

  @Property
  @Label("composed Iso satisfies get-reverseGet: negate.andThen(negate) law holds")
  void composedIsoSatisfiesLaws(@ForAll int n) {
    var doubled = negate.andThen(negate); // negate twice = identity
    assertThat(doubled.get(doubled.reverseGet(n))).isEqualTo(n);
    assertThat(doubled.reverseGet(doubled.get(n))).isEqualTo(n);
  }

  // ── modify ────────────────────────────────────────────────────────────────

  @Property
  @Label("modify with identity is a no-op: modify(s, id) == s")
  void modifyWithIdentityIsNoop(@ForAll int n) {
    assertThat(negate.modify(n, UnaryOperator.identity())).isEqualTo(n);
  }

  @Property
  @Label("modify(s, f) == reverseGet(f(get(s))): modify matches its definition")
  void modifyMatchesDefinition(@ForAll int n) {
    UnaryOperator<Integer> f = x -> x + 100;
    assertThat(negate.modify(n, f)).isEqualTo(negate.reverseGet(f.apply(negate.get(n))));
  }
}
