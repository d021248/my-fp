package d021248.mytry.core.lang.fp.optics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class IsoTest {

  // ── Test domain ───────────────────────────────────────────────────────────

  record Celsius(double degrees) {}

  record Fahrenheit(double degrees) {}

  // ── Iso under test ────────────────────────────────────────────────────────

  static final Iso<Celsius, Fahrenheit> celsiusToF =
      Iso.of(
          c -> new Fahrenheit(c.degrees() * 9.0 / 5.0 + 32.0),
          f -> new Celsius((f.degrees() - 32.0) * 5.0 / 9.0));

  static final Iso<String, List<String>> splitOnComma =
      Iso.of(s -> List.of(s.split(",", -1)), parts -> String.join(",", parts));

  // ── Iso laws ──────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Iso laws")
  class IsoLaws {

    @Test
    @DisplayName("Law 1 (get-reverseGet): get(reverseGet(a)) == a")
    void getAfterReverseGet() {
      var f = new Fahrenheit(212.0);
      var result = celsiusToF.get(celsiusToF.reverseGet(f));
      assertThat(result.degrees()).isEqualTo(f.degrees());
    }

    @Test
    @DisplayName("Law 2 (reverseGet-get): reverseGet(get(s)) == s")
    void reverseGetAfterGet() {
      var c = new Celsius(100.0);
      var result = celsiusToF.reverseGet(celsiusToF.get(c));
      assertThat(result.degrees()).isEqualTo(c.degrees());
    }
  }

  // ── get / reverseGet ──────────────────────────────────────────────────────

  @Test
  @DisplayName("get converts Celsius to Fahrenheit correctly")
  void getConvertsCelsiusToFahrenheit() {
    assertThat(celsiusToF.get(new Celsius(0.0)).degrees()).isEqualTo(32.0);
    assertThat(celsiusToF.get(new Celsius(100.0)).degrees()).isEqualTo(212.0);
  }

  @Test
  @DisplayName("reverseGet converts Fahrenheit to Celsius correctly")
  void reverseGetConvertsFahrenheitToCelsius() {
    assertThat(celsiusToF.reverseGet(new Fahrenheit(32.0)).degrees()).isEqualTo(0.0);
    assertThat(celsiusToF.reverseGet(new Fahrenheit(212.0)).degrees()).isEqualTo(100.0);
  }

  // ── reverse ───────────────────────────────────────────────────────────────

  @Test
  @DisplayName("reverse() returns the inverse Iso<Fahrenheit, Celsius>")
  void reverseProducesInverseIso() {
    var fToC = celsiusToF.reverse();
    assertThat(fToC.get(new Fahrenheit(212.0)).degrees()).isEqualTo(100.0);
    assertThat(fToC.reverseGet(new Celsius(0.0)).degrees()).isEqualTo(32.0);
  }

  @Test
  @DisplayName("reverse().reverse() round-trips back to original behaviour")
  void doubleReverseIsOriginal() {
    var roundTripped = celsiusToF.reverse().reverse();
    var c = new Celsius(20.0);
    assertThat(roundTripped.get(c).degrees()).isEqualTo(celsiusToF.get(c).degrees());
  }

  // ── modify ────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("modify applies the function in the target type and returns updated source")
  void modifyTransformsInTargetType() {
    var freezing = new Celsius(0.0);
    // Add 18°F (which corresponds to 10°C shift)
    var result = celsiusToF.modify(freezing, f -> new Fahrenheit(f.degrees() + 18.0));
    assertThat(result.degrees()).isEqualTo(10.0);
  }

  @Test
  @DisplayName("modify with identity is a no-op")
  void modifyWithIdentityIsNoop() {
    var c = new Celsius(25.0);
    var result = celsiusToF.modify(c, f -> f);
    assertThat(result.degrees()).isEqualTo(c.degrees());
  }

  // ── andThen (composition) ─────────────────────────────────────────────────

  @Nested
  @DisplayName("andThen composes two Isos")
  class Composition {

    record Kelvin(double value) {}

    // Celsius ↔ Kelvin: K = C + 273.15
    static final Iso<Celsius, Kelvin> celsiusToKelvin =
        Iso.of(c -> new Kelvin(c.degrees() + 273.15), k -> new Celsius(k.value() - 273.15));

    // Composed: Fahrenheit ↔ Kelvin via Celsius
    static final Iso<Fahrenheit, Kelvin> fToK = celsiusToF.reverse().andThen(celsiusToKelvin);

    @Test
    @DisplayName("composed get drills through both conversions")
    void composedGetDrillsThrough() {
      var freezing = new Fahrenheit(32.0);
      var kelvin = fToK.get(freezing);
      assertThat(kelvin.value()).isEqualTo(273.15);
    }

    @Test
    @DisplayName("composed reverseGet inverts both conversions")
    void composedReverseGetInverts() {
      var k = new Kelvin(373.15); // 100°C = 212°F
      var fahrenheit = fToK.reverseGet(k);
      assertThat(fahrenheit.degrees()).isEqualTo(212.0);
    }

    @Test
    @DisplayName("composed Iso satisfies both laws")
    void composedIsoSatisfiesLaws() {
      var f = new Fahrenheit(68.0);
      var k = fToK.get(f);
      assertThat(fToK.reverseGet(k).degrees()).isEqualTo(f.degrees());
      assertThat(fToK.get(fToK.reverseGet(k)).value()).isEqualTo(k.value());
    }
  }

  // ── asLens downcast ───────────────────────────────────────────────────────

  @Nested
  @DisplayName("asLens downcast behaves as a well-formed Lens")
  class AsLens {

    @Test
    @DisplayName("asLens get returns the same value as Iso get")
    void asLensGetMatchesIsoGet() {
      var lens = celsiusToF.asLens();
      var c = new Celsius(100.0);
      assertThat(lens.get(c).degrees()).isEqualTo(celsiusToF.get(c).degrees());
    }

    @Test
    @DisplayName("asLens set replaces the value via reverseGet")
    void asLensSetUsesReverseGet() {
      var lens = celsiusToF.asLens();
      var result = lens.set(new Celsius(0.0), new Fahrenheit(212.0));
      assertThat(result.degrees()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("asLens satisfies get-set law")
    void asLensGetSetLaw() {
      Lens<Celsius, Fahrenheit> lens = celsiusToF.asLens();
      var c = new Celsius(50.0);
      assertThat(lens.get(lens.set(c, lens.get(c))).degrees()).isEqualTo(lens.get(c).degrees());
    }
  }

  // ── asPrism downcast ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("asPrism downcast behaves as a total Prism")
  class AsPrism {

    @Test
    @DisplayName("asPrism getOption always returns Optional.of the focused value")
    void asPrismAlwaysSucceeds() {
      var prism = celsiusToF.asPrism();
      var c = new Celsius(25.0);
      assertThat(prism.getOption(c)).isEqualTo(Optional.of(celsiusToF.get(c)));
    }

    @Test
    @DisplayName("asPrism review is the same as reverseGet")
    void asPrismReviewIsReverseGet() {
      Prism<Celsius, Fahrenheit> prism = celsiusToF.asPrism();
      var f = new Fahrenheit(68.0);
      assertThat(prism.review(f)).isEqualTo(celsiusToF.reverseGet(f));
    }

    @Test
    @DisplayName("asPrism satisfies the Prism review-getOption round-trip law")
    void asPrismSatisfiesLaw() {
      var prism = celsiusToF.asPrism();
      var f = new Fahrenheit(100.0);
      assertThat(prism.getOption(prism.review(f))).contains(f);
    }
  }
}
