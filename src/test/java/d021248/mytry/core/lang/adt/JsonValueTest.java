package d021248.mytry.core.lang.adt;

import static org.assertj.core.api.Assertions.assertThat;

import d021248.mytry.core.lang.adt.JsonValue.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JsonValueTest {

  // ── variant construction ─────────────────────────────────────────────

  @Nested
  @DisplayName("Smart constructors")
  class Constructors {
    @Test
    void nullSingleton() {
      assertThat(JsonValue.NULL).isInstanceOf(JNull.class);
    }

    @Test
    void boolTrue() {
      assertThat(JsonValue.of(true)).isEqualTo(new JBool(true));
    }

    @Test
    void boolFalse() {
      assertThat(JsonValue.of(false)).isEqualTo(new JBool(false));
    }

    @Test
    void number() {
      assertThat(JsonValue.of(42)).isEqualTo(new JNum(42));
    }

    @Test
    void string() {
      assertThat(JsonValue.of("hi")).isEqualTo(new JStr("hi"));
    }

    @Test
    void emptyArray() {
      assertThat(JsonValue.arr()).isEqualTo(new JArr(List.of()));
    }

    @Test
    void arrayWithElements() {
      assertThat(JsonValue.arr(JsonValue.of(1), JsonValue.of(2)))
          .isEqualTo(new JArr(List.of(new JNum(1), new JNum(2))));
    }
  }

  // ── typeName ─────────────────────────────────────────────────────────

  @Nested
  @DisplayName("typeName() exhaustive switch")
  class TypeName {
    @Test
    void nullType() {
      assertThat(JsonValue.NULL.typeName()).isEqualTo("null");
    }

    @Test
    void boolType() {
      assertThat(JsonValue.of(true).typeName()).isEqualTo("boolean");
    }

    @Test
    void numType() {
      assertThat(JsonValue.of(1).typeName()).isEqualTo("number");
    }

    @Test
    void strType() {
      assertThat(JsonValue.of("x").typeName()).isEqualTo("string");
    }

    @Test
    void arrType() {
      assertThat(JsonValue.arr().typeName()).isEqualTo("array");
    }

    @Test
    void objType() {
      assertThat(JsonValue.obj("k", JsonValue.NULL).typeName()).isEqualTo("object");
    }
  }

  // ── scalarCount ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("scalarCount() recursive counting")
  class ScalarCount {
    @Test
    void singleScalar() {
      assertThat(JsonValue.of("x").scalarCount()).isEqualTo(1);
    }

    @Test
    void emptyArray() {
      assertThat(JsonValue.arr().scalarCount()).isEqualTo(0);
    }

    @Test
    void flatArray() {
      assertThat(JsonValue.arr(JsonValue.of(1), JsonValue.of(2)).scalarCount()).isEqualTo(2);
    }

    @Test
    void nestedStructure() {
      var v =
          JsonValue.obj(
              "name",
              JsonValue.of("Alice"),
              "tags",
              JsonValue.arr(JsonValue.of("a"), JsonValue.of("b")));
      assertThat(v.scalarCount()).isEqualTo(3); // "Alice", "a", "b"
    }
  }

  // ── prettyPrint ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("prettyPrint()")
  class PrettyPrint {
    @Test
    void nullPrints() {
      assertThat(JsonValue.NULL.prettyPrint()).isEqualTo("null");
    }

    @Test
    void boolPrints() {
      assertThat(JsonValue.of(true).prettyPrint()).isEqualTo("true");
    }

    @Test
    void intPrints() {
      assertThat(JsonValue.of(42).prettyPrint()).isEqualTo("42");
    }

    @Test
    void doublePrints() {
      assertThat(JsonValue.of(3.14).prettyPrint()).isEqualTo("3.14");
    }

    @Test
    void stringPrints() {
      assertThat(JsonValue.of("hi").prettyPrint()).isEqualTo("\"hi\"");
    }

    @Test
    void emptyArrPrints() {
      assertThat(JsonValue.arr().prettyPrint()).isEqualTo("[]");
    }

    @Test
    void emptyObjPrints() {
      assertThat(new JObj(Map.of()).prettyPrint()).isEqualTo("{}");
    }
  }

  // ── fold ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("fold dispatches to the correct function")
  void foldDispatches() {
    assertThat(JsonValue.NULL.fold(() -> "N", b -> "B", n -> "D", s -> "S", a -> "A", o -> "O"))
        .isEqualTo("N");

    assertThat(JsonValue.of(true).fold(() -> "N", b -> "B", n -> "D", s -> "S", a -> "A", o -> "O"))
        .isEqualTo("B");

    assertThat(JsonValue.of(3.14).fold(() -> "N", b -> "B", n -> "D", s -> "S", a -> "A", o -> "O"))
        .isEqualTo("D");
  }

  // ── record equality ──────────────────────────────────────────────────

  @Test
  @DisplayName("Record equality: structurally identical values are equal")
  void recordEquality() {
    var a = JsonValue.obj("x", JsonValue.of(1));
    var b = JsonValue.obj("x", JsonValue.of(1));
    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }

  // ── pattern matching ─────────────────────────────────────────────────

  @Test
  @DisplayName("Pattern matching with record deconstruction")
  void patternMatching() {
    JsonValue v = JsonValue.of("hello");
    var result =
        switch (v) {
          case JStr(var s) -> s.toUpperCase();
          default -> "not a string";
        };
    assertThat(result).isEqualTo("HELLO");
  }
}
