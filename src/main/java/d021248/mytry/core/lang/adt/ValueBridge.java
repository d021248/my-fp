package d021248.mytry.core.lang.adt;

import module java.base;

public final class ValueBridge {

  private ValueBridge() {}

  /// Converts a JsonValue to an equivalent YamlValue.
  public static YamlValue jsonToYaml(JsonValue json) {
    return switch (json) {
      case JsonValue.JNull() -> YamlValue.NULL;
      case JsonValue.JBool(var b) -> YamlValue.of(b);
      case JsonValue.JNum(var n) -> YamlValue.of(n);
      case JsonValue.JStr(var s) -> YamlValue.of(s);
      case JsonValue.JArr(var elems) ->
          new YamlValue.YSeq(elems.stream().map(ValueBridge::jsonToYaml).toList());
      case JsonValue.JObj(var fields) -> {
        var yamlMap = new LinkedHashMap<String, YamlValue>();
        fields.forEach((k, v) -> yamlMap.put(k, jsonToYaml(v)));
        yield new YamlValue.YMap(yamlMap);
      }
    };
  }

  /// Converts a YamlValue back to a JsonValue.
  public static JsonValue yamlToJson(YamlValue yaml) {
    return switch (yaml) {
      case YamlValue.YNull() -> JsonValue.NULL;
      case YamlValue.YBool(var b) -> JsonValue.of(b);
      case YamlValue.YNum(var n) -> JsonValue.of(n);
      case YamlValue.YStr(var s) -> JsonValue.of(s);
      case YamlValue.YSeq(var elems) ->
          new JsonValue.JArr(elems.stream().map(ValueBridge::yamlToJson).toList());
      case YamlValue.YMap(var fields) -> {
        var jsonMap = new LinkedHashMap<String, JsonValue>();
        fields.forEach((k, v) -> jsonMap.put(k, yamlToJson(v)));
        yield new JsonValue.JObj(jsonMap);
      }
    };
  }
}
