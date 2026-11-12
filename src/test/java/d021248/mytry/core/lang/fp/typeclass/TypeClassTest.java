package d021248.mytry.core.lang.fp.typeclass;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TypeClassTest {

  // ── Test domain ───────────────────────────────────────────────────────────

  record Money(double amount, String currency) {}

  // ── Show ──────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Show<A>")
  class ShowTests {

    @Test
    @DisplayName("Show.natural() delegates to Object.toString")
    void naturalUsesToString() {
      Show<Integer> intShow = Show.natural();
      assertThat(intShow.show(42)).isEqualTo("42");
    }

    @Test
    @DisplayName("lambda instance formats value as specified")
    void lambdaInstanceFormatsValue() {
      Show<Integer> hexShow = i -> "0x%X".formatted(i);
      assertThat(hexShow.show(255)).isEqualTo("0xFF");
    }

    @Test
    @DisplayName("contramap lifts Show<B> to Show<A> by mapping A→B first")
    void contramapLiftsShow() {
      Show<Integer> decShow = Object::toString;
      Show<Money> amountShow = decShow.contramap(m -> (int) Math.round(m.amount()));
      assertThat(amountShow.show(new Money(9.99, "EUR"))).isEqualTo("10");
    }

    @Test
    @DisplayName("listShow produces bracketed comma-separated representation")
    void listShowFormatsAsBracketedList() {
      Show<String> strShow = s -> "'" + s + "'";
      Show<List<String>> listShow = Show.listShow(strShow);
      assertThat(listShow.show(List.of("a", "b", "c"))).isEqualTo("['a', 'b', 'c']");
    }

    @Test
    @DisplayName("listShow with empty list produces empty brackets")
    void listShowWithEmptyList() {
      assertThat(Show.listShow(Show.<Integer>natural()).show(List.of())).isEqualTo("[]");
    }

    @Test
    @DisplayName("optionalShow wraps present value in Some(...)")
    void optionalShowPresentValue() {
      Show<Optional<String>> optShow = Show.optionalShow(s -> "«" + s + "»");
      assertThat(optShow.show(Optional.of("hello"))).isEqualTo("Some(«hello»)");
    }

    @Test
    @DisplayName("optionalShow returns None for empty Optional")
    void optionalShowEmptyReturnsNone() {
      Show<Optional<String>> optShow = Show.optionalShow(Show.natural());
      assertThat(optShow.show(Optional.empty())).isEqualTo("None");
    }

    @Test
    @DisplayName("contramap composes: show(f(b)) equals show applied to f's output")
    void contramapComposesCorrectly() {
      Show<String> upper = String::toUpperCase;
      Show<Integer> digitCount = upper.contramap(n -> String.valueOf(n).replace("-", ""));
      assertThat(digitCount.show(12345)).isEqualTo("12345");
      assertThat(digitCount.show(-99)).isEqualTo("99");
    }
  }

  // ── Eq ────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Eq<A>")
  class EqTests {

    @Test
    @DisplayName("Eq.natural() uses Objects.equals")
    void naturalUsesObjectsEquals() {
      Eq<String> eq = Eq.natural();
      assertThat(eq.eq("hello", "hello")).isTrue();
      assertThat(eq.eq("hello", "world")).isFalse();
      assertThat(eq.eq(null, null)).isTrue();
      assertThat(eq.eq("x", null)).isFalse();
    }

    @Test
    @DisplayName("Eq.caseInsensitive() ignores case")
    void caseInsensitiveIgnoresCase() {
      Eq<String> eq = Eq.caseInsensitive();
      assertThat(eq.eq("Hello", "HELLO")).isTrue();
      assertThat(eq.eq("Hello", "World")).isFalse();
    }

    @Test
    @DisplayName("Eq.approximate() accepts values within epsilon")
    void approximateAcceptsValuesWithinEpsilon() {
      Eq<Double> eq = Eq.approximate(0.01);
      assertThat(eq.eq(0.1 + 0.2, 0.3)).isTrue();
      assertThat(eq.eq(1.0, 1.02)).isFalse();
    }

    @Test
    @DisplayName("neq is the negation of eq")
    void neqIsNegation() {
      Eq<Integer> eq = Eq.natural();
      assertThat(eq.neq(1, 2)).isTrue();
      assertThat(eq.neq(1, 1)).isFalse();
    }

    @Test
    @DisplayName("contramap lifts Eq<B> to Eq<A> by mapping A→B first")
    void contramapLiftsEq() {
      Eq<String> strEq = Eq.caseInsensitive();
      Eq<Money> byCurrency = strEq.contramap(Money::currency);
      assertThat(byCurrency.eq(new Money(1.0, "EUR"), new Money(9.0, "eur"))).isTrue();
      assertThat(byCurrency.eq(new Money(1.0, "EUR"), new Money(1.0, "USD"))).isFalse();
    }

    @Test
    @DisplayName("Eq.fromComparator returns true when comparator returns 0")
    void fromComparatorDelegatesToComparator() {
      Eq<String> byLength = Eq.fromComparator(Comparator.comparingInt(String::length));
      assertThat(byLength.eq("abc", "xyz")).isTrue();
      assertThat(byLength.eq("ab", "xyz")).isFalse();
    }
  }

  // ── Ord ───────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Ord<A>")
  class OrdTests {

    @Test
    @DisplayName("Ord.natural() orders strings lexicographically")
    void naturalOrdersStrings() {
      Ord<String> ord = Ord.natural();
      assertThat(ord.compare("apple", "banana")).isNegative();
      assertThat(ord.compare("z", "a")).isPositive();
      assertThat(ord.compare("same", "same")).isZero();
    }

    @Test
    @DisplayName("Ord.natural() satisfies Eq consistency: compare==0 iff eq")
    void naturalIsConsistentWithEq() {
      Ord<Integer> ord = Ord.natural();
      assertThat(ord.eq(5, 5)).isTrue();
      assertThat(ord.eq(5, 6)).isFalse();
    }

    @Test
    @DisplayName("min returns the smaller of two values")
    void minReturnsSmaller() {
      Ord<Integer> ord = Ord.natural();
      assertThat(ord.min(3, 7)).isEqualTo(3);
      assertThat(ord.min(7, 3)).isEqualTo(3);
    }

    @Test
    @DisplayName("max returns the larger of two values")
    void maxReturnsLarger() {
      Ord<Integer> ord = Ord.natural();
      assertThat(ord.max(3, 7)).isEqualTo(7);
    }

    @Test
    @DisplayName("reversed() inverts the comparison")
    void reversedInvertsComparison() {
      Ord<Integer> desc = Ord.<Integer>natural().reversed();
      assertThat(desc.compare(3, 7)).isPositive();
    }

    @Test
    @DisplayName("contramap derives Ord<Person> from Ord<String> by name")
    void contramapDerivesOrd() {
      record Person(String name) {}
      Ord<Person> byName = Ord.<String>natural().contramap(Person::name);
      assertThat(byName.compare(new Person("Alice"), new Person("Bob"))).isNegative();
    }
  }

  // ── Semigroup ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Semigroup<A>")
  class SemigroupTests {

    @Test
    @DisplayName("string semigroup concatenates two strings")
    void stringCombine() {
      Semigroup<String> sg = Semigroup.of(String::concat);
      assertThat(sg.combine("hello", " world")).isEqualTo("hello world");
    }

    @Test
    @DisplayName("sconcat folds a non-empty list")
    void sconcatFoldsList() {
      Semigroup<String> sg = Semigroup.of(String::concat);
      assertThat(sg.sconcat(List.of("a", "b", "c"))).isEqualTo("abc");
    }

    @Test
    @DisplayName("intercalate inserts delimiter between each pair")
    void intercalateInserts() {
      Semigroup<String> sg = Semigroup.of(String::concat);
      assertThat(sg.intercalate(", ").sconcat(List.of("x", "y", "z"))).isEqualTo("x, y, z");
    }
  }

  // ── Monoid ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Monoid<A>")
  class MonoidTests {

    @Test
    @DisplayName("Monoid.strings(): left identity combine(empty, a) == a")
    void stringsLeftIdentity() {
      var m = Monoid.strings();
      assertThat(m.combine(m.empty(), "hello")).isEqualTo("hello");
    }

    @Test
    @DisplayName("Monoid.strings(): right identity combine(a, empty) == a")
    void stringsRightIdentity() {
      var m = Monoid.strings();
      assertThat(m.combine("hello", m.empty())).isEqualTo("hello");
    }

    @Test
    @DisplayName("Monoid.intSum(): identity is 0")
    void intSumIdentity() {
      var m = Monoid.intSum();
      assertThat(m.combine(m.empty(), 42)).isEqualTo(42);
    }

    @Test
    @DisplayName("Monoid.intProduct(): identity is 1")
    void intProductIdentity() {
      var m = Monoid.intProduct();
      assertThat(m.combine(m.empty(), 7)).isEqualTo(7);
    }

    @Test
    @DisplayName("mconcat reduces an empty list to the identity")
    void mconcatEmptyList() {
      assertThat(Monoid.strings().mconcat(List.of())).isEqualTo("");
    }

    @Test
    @DisplayName("mconcat reduces a list of strings")
    void mconcatStringList() {
      assertThat(Monoid.strings().mconcat(List.of("a", "b", "c"))).isEqualTo("abc");
    }

    @Test
    @DisplayName("Monoid.lists(): concatenates lists of arbitrary element type")
    void listsMonoidConcatenates() {
      Monoid<List<Integer>> m = Monoid.lists();
      assertThat(m.mconcat(List.of(List.of(1, 2), List.of(3), List.of(4, 5))))
          .containsExactly(1, 2, 3, 4, 5);
    }
  }
}
