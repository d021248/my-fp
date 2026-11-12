package d021248.mytry.core.lang.adt;

import static d021248.mytry.core.lang.adt.JsonValue.prop;

public final class ValueBridgeDemo {

  public static void main() {
    // 1. Build a complex JsonValue
    JsonValue originalJson =
        JsonValue.obj(
            prop("project", JsonValue.of("BridgeBuilder")),
            prop("active", JsonValue.of(true)),
            prop("version", JsonValue.of(1.25)),
            prop("tags", JsonValue.arr(JsonValue.of("java25"), JsonValue.of("functional"))),
            prop(
                "settings",
                JsonValue.obj(
                    prop("theme", JsonValue.of("dark")), prop("auto-save", JsonValue.of(false)))));

    System.out.println("=== 1. ORIGINAL JSON ===");
    System.out.println(originalJson.prettyPrint());

    // 2. Bridge: JSON -> YAML
    YamlValue convertedYaml = ValueBridge.jsonToYaml(originalJson);

    System.out.println("\n=== 2. GENERATED YAML ===");
    System.out.println(convertedYaml.toYamlString());

    // 3. Bridge back: YAML -> JSON
    JsonValue reconstructedJson = ValueBridge.yamlToJson(convertedYaml);

    System.out.println("\n=== 3. RECONSTRUCTED JSON ===");
    System.out.println(reconstructedJson.prettyPrint());

    // Validation
    boolean identical = originalJson.equals(reconstructedJson);
    System.out.println("\nOriginal and reconstruction equal? " + identical);
  }
}
