package d021248.mytry.core.lang.adt;

import module java.base;

/// JSON modelled as a recursive algebraic data type.
///
/// Six variants cover the full JSON grammar (RFC 8259):
///
/// | Variant | JSON | Java carrier |
/// |---------|------|-------------|
/// | {@link JNull}   | `null`            | — (singleton) |
/// | {@link JBool}   | `true` / `false`  | `boolean` |
/// | {@link JNum}    | `3.14`            | `double` |
/// | {@link JStr}    | `"hello"`         | `String` |
/// | {@link JArr}    | `[1, 2, 3]`       | `List<JsonValue>` |
/// | {@link JObj}    | `{"k": "v"}`      | `Map<String, JsonValue>` |
///
/// ## Why this is a good ADT example
///
/// - **Recursive**: `JArr` and `JObj` contain `JsonValue` — the type refers to
///   itself.
/// - **Exhaustive switch**: every operation on `JsonValue` must handle all six
///   cases; the compiler rejects incomplete switches.
/// - **Record patterns**: nested destructuring extracts values at arbitrary
///   depth in a single `case`.
///
/// ## No library needed
///
/// This is not a JSON parser — it's a demonstration that Java's type system can
/// model a recursive grammar as cleanly as Haskell's `data Value = ...`.
public sealed interface JsonValue
    permits JsonValue.JNull,
        JsonValue.JBool,
        JsonValue.JNum,
        JsonValue.JStr,
        JsonValue.JArr,
        JsonValue.JObj {

  record JNull() implements JsonValue {}

  record JBool(boolean value) implements JsonValue {}

  record JNum(double value) implements JsonValue {}

  record JStr(String value) implements JsonValue {}

  record JArr(List<JsonValue> elements) implements JsonValue {}

  record JObj(Map<String, JsonValue> fields) implements JsonValue {}

  // ── smart constructors ───────────────────────────────────────────────

  JsonValue NULL = new JNull();

  static JsonValue of(boolean v) {
    return new JBool(v);
  }

  static JsonValue of(double v) {
    return new JNum(v);
  }

  static JsonValue of(String v) {
    return new JStr(v);
  }

  static JsonValue arr(JsonValue... elems) {
    return new JArr(List.of(elems));
  }

  static JsonValue obj(Map<String, JsonValue> m) {
    return new JObj(Map.copyOf(m));
  }

  static JsonValue obj(String k1, JsonValue v1) {
    return new JObj(Map.of(k1, v1));
  }

  static JsonValue obj(String k1, JsonValue v1, String k2, JsonValue v2) {
    return new JObj(Map.of(k1, v1, k2, v2));
  }

  static JsonValue obj(String k1, JsonValue v1, String k2, JsonValue v2, String k3, JsonValue v3) {
    return new JObj(Map.of(k1, v1, k2, v2, k3, v3));
  }

  // ── operations via pattern matching ──────────────────────────────────

  /// Returns a human-readable type tag for this value.
  default String typeName() {
    return switch (this) {
      case JNull() -> "null";
      case JBool(_) -> "boolean";
      case JNum(_) -> "number";
      case JStr(_) -> "string";
      case JArr(_) -> "array";
      case JObj(_) -> "object";
    };
  }

  /// Pretty-prints this value as indented JSON.
  default String prettyPrint() {
    return prettyPrint(0);
  }

  /// Recursively counts the total number of scalar values (null, bool, num, str).
  default int scalarCount() {
    return switch (this) {
      case JNull() -> 1;
      case JBool(_) -> 1;
      case JNum(_) -> 1;
      case JStr(_) -> 1;
      case JArr(var elems) -> elems.stream().mapToInt(JsonValue::scalarCount).sum();
      case JObj(var fields) -> fields.values().stream().mapToInt(JsonValue::scalarCount).sum();
    };
  }

  /// Fold: collapse this value into a single result using per-variant functions.
  ///
  /// This is the **catamorphism** — the fundamental operation on an ADT.
  /// Every other operation can be written in terms of fold.
  default <T> T fold(
      Supplier<T> onNull,
      Function<Boolean, T> onBool,
      Function<Double, T> onNum,
      Function<String, T> onStr,
      Function<List<JsonValue>, T> onArr,
      Function<Map<String, JsonValue>, T> onObj) {
    return switch (this) {
      case JNull() -> onNull.get();
      case JBool(var v) -> onBool.apply(v);
      case JNum(var v) -> onNum.apply(v);
      case JStr(var v) -> onStr.apply(v);
      case JArr(var elems) -> onArr.apply(elems);
      case JObj(var fields) -> onObj.apply(fields);
    };
  }

  // ── private helpers ──────────────────────────────────────────────────

  private String prettyPrint(int indent) {
    var pad = " ".repeat(indent);
    var pad2 = " ".repeat(indent + 2);
    return switch (this) {
      case JNull() -> "null";
      case JBool(var v) -> String.valueOf(v);
      case JNum(var v) -> v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
      case JStr(var v) -> "\"" + v + "\"";
      case JArr(var elems) when elems.isEmpty() -> "[]";
      case JArr(var elems) -> {
        var sb = new StringBuilder("[\n");
        for (int i = 0; i < elems.size(); i++) {
          sb.append(pad2).append(elems.get(i).prettyPrint(indent + 2));
          if (i < elems.size() - 1) sb.append(",");
          sb.append("\n");
        }
        yield sb.append(pad).append("]").toString();
      }
      case JObj(var fields) when fields.isEmpty() -> "{}";
      case JObj(var fields) -> {
        var sb = new StringBuilder("{\n");
        var entries = new ArrayList<>(fields.entrySet());
        for (int i = 0; i < entries.size(); i++) {
          var e = entries.get(i);
          sb.append(pad2)
              .append("\"")
              .append(e.getKey())
              .append("\": ")
              .append(e.getValue().prettyPrint(indent + 2));
          if (i < entries.size() - 1) sb.append(",");
          sb.append("\n");
        }
        yield sb.append(pad).append("}").toString();
      }
    };
  }

  /// Creates a JObj from any number of key-value pairs.
  @SafeVarargs
  static JsonValue obj(Map.Entry<String, JsonValue>... entries) {
    var map = new LinkedHashMap<String, JsonValue>();
    for (var e : entries) {
      map.put(e.getKey(), e.getValue());
    }
    return new JObj(map);
  }

  /// Helper to create key-value pairs readably (alias for Map.entry).
  static Map.Entry<String, JsonValue> prop(String key, JsonValue value) {
    return Map.entry(key, value);
  }
}
