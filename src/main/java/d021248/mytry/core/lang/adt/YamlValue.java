package d021248.mytry.core.lang.adt;

import module java.base;

/// A YAML data structure modelled as a recursive algebraic data type.
public sealed interface YamlValue
    permits YamlValue.YNull,
        YamlValue.YBool,
        YamlValue.YNum,
        YamlValue.YStr,
        YamlValue.YSeq,
        YamlValue.YMap {

  record YNull() implements YamlValue {}

  record YBool(boolean value) implements YamlValue {}

  record YNum(double value) implements YamlValue {}

  record YStr(String value) implements YamlValue {}

  record YSeq(List<YamlValue> elements) implements YamlValue {}

  record YMap(Map<String, YamlValue> fields) implements YamlValue {}

  // Smart Constructors
  YamlValue NULL = new YNull();

  static YamlValue of(boolean v) {
    return new YBool(v);
  }

  static YamlValue of(double v) {
    return new YNum(v);
  }

  static YamlValue of(String v) {
    return new YStr(v);
  }

  static YamlValue seq(YamlValue... elems) {
    return new YSeq(List.of(elems));
  }

  static YamlValue map(Map<String, YamlValue> m) {
    return new YMap(Map.copyOf(m));
  }

  // Generates valid block-style YAML
  default String toYamlString() {
    return toYamlString(0);
  }

  private String toYamlString(int indent) {
    var pad = " ".repeat(indent);
    return switch (this) {
      case YNull() -> "~"; // YAML notation for null
      case YBool(var v) -> String.valueOf(v);
      case YNum(var v) -> v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
      case YStr(var v) -> v.matches("^[a-zA-Z0-9_-]+$") ? v : "\"" + v + "\"";
      case YSeq(var elems) -> {
        if (elems.isEmpty()) yield "[]";
        var sb = new StringBuilder();
        for (var el : elems) {
          sb.append("\n").append(pad).append("- ").append(el.toYamlString(indent + 2));
        }
        yield sb.toString();
      }
      case YMap(var fields) -> {
        if (fields.isEmpty()) yield "{}";
        var sb = new StringBuilder();
        fields.forEach(
            (k, v) -> {
              sb.append("\n").append(pad).append(k).append(": ").append(v.toYamlString(indent + 2));
            });
        yield sb.toString();
      }
    };
  }
}
