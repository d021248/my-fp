package d021248.mytry.core.lang.fp.kleisli;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.function.Function;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

/// Property-based tests for {@link Kleisli} verifying the three category laws
/// (= monad laws) for *any* input.
///
/// ## Kleisli category laws
///
/// | Law | Statement |
/// |---|---|
/// | Left identity | `identity().andThen(f)` is equivalent to `f` |
/// | Right identity | `f.andThen(identity())` is equivalent to `f` |
/// | Associativity | `(f >=> g) >=> h` is equivalent to `f >=> (g >=> h)` |
///
/// These are exactly the monad laws for `Optional` expressed as Kleisli arrows,
/// showing that **a monad is a monoid in the category of endofunctors**.
///
/// ## Arrows under test
///
/// ```
/// f : Integer → String    (always succeeds via lift)
/// g : String  → Integer   (always succeeds via lift)
/// h : Integer → Integer   (always succeeds via lift)
/// ```
///
/// Using total lifted functions keeps the laws clean.  Short-circuiting behaviour
/// (when an arrow returns empty) is tested separately.
///
/// The existing {@link KleisliTest} verifies the laws for one specific input.
/// This class verifies they hold for *any* randomly generated integer.
@PropertyDefaults(tries = 300)
class KleisliPropertyTest {

  // Total arrows (always return present Optional)
  private static final Kleisli<Integer, String> f = Kleisli.lift(i -> "n=" + i);
  private static final Kleisli<String, Integer> g = Kleisli.lift(String::length);
  private static final Kleisli<Integer, Integer> h = Kleisli.lift(i -> i * 2);

  // Always-failing arrow — used to verify short-circuit behaviour
  private static final Kleisli<Integer, Integer> failing = Kleisli.of(_ -> Optional.empty());

  // ── Left identity ─────────────────────────────────────────────────────────

  @Property
  @Label("left identity: identity().andThen(f).run(a) == f.run(a) for any a")
  void leftIdentity(@ForAll int a) {
    assertThat(Kleisli.<Integer>identity().andThen(f).run(a)).isEqualTo(f.run(a));
  }

  // ── Right identity ────────────────────────────────────────────────────────

  @Property
  @Label("right identity: f.andThen(identity()).run(a) == f.run(a) for any a")
  void rightIdentity(@ForAll int a) {
    assertThat(f.andThen(Kleisli.identity()).run(a)).isEqualTo(f.run(a));
  }

  // ── Associativity ─────────────────────────────────────────────────────────

  @Property
  @Label("associativity: (f >=> g) >=> h == f >=> (g >=> h) for any a")
  void associativity(@ForAll int a) {
    var lhs = f.andThen(g).andThen(h);
    var rhs = f.andThen(g.andThen(h));
    assertThat(lhs.run(a)).isEqualTo(rhs.run(a));
  }

  // ── lift ──────────────────────────────────────────────────────────────────

  @Property
  @Label("lift always produces a present Optional for any input")
  void liftAlwaysSucceeds(@ForAll int a) {
    var toStr = Kleisli.<Integer, String>lift(String::valueOf);
    assertThat(toStr.run(a)).isPresent();
  }

  // ── Short-circuit ─────────────────────────────────────────────────────────

  @Property
  @Label("failing arrow short-circuits: failing.andThen(h).run(a) is always empty")
  void failingShortCircuitsComposition(@ForAll int a) {
    assertThat(failing.andThen(h).run(a)).isEmpty();
  }

  @Property
  @Label("h.andThen(failing).run(a) is always empty regardless of h's output")
  void composingWithFailingProducesEmpty(@ForAll int a) {
    assertThat(h.andThen(failing).run(a)).isEmpty();
  }

  // ── map (functor law) ─────────────────────────────────────────────────────

  @Property
  @Label("map identity: f.map(identity).run(a) == f.run(a)")
  void mapIdentityLaw(@ForAll int a) {
    assertThat(f.map(s -> s).run(a)).isEqualTo(f.run(a));
  }

  @Property
  @Label("map composition: f.map(fn1).map(fn2).run(a) == f.map(fn1.andThen(fn2)).run(a)")
  void mapCompositionLaw(@ForAll int a) {
    Function<String, Integer> fn1 = s -> s.length();
    Function<Integer, String> fn2 = n -> "[" + n + "]";
    assertThat(f.map(fn1).map(fn2).run(a)).isEqualTo(f.map(fn1.andThen(fn2)).run(a));
  }
}
