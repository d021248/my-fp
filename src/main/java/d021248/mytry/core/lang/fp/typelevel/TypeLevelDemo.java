package d021248.mytry.core.lang.fp.typelevel;

/// Demonstrates type-level programming: Peano numbers, heterogeneous lists,
/// and compile-time bounds checking via induction proofs.
///
/// ## What happens here
///
/// We build the list {@code ["Hallo", 42, true]} with the precise type
/// {@code HCons<String, HCons<Integer, HCons<Boolean, HNil>>>}.
/// Then we access elements by index — the compiler knows the return type
/// at each position without any cast.
///
/// ## The compiler-as-proof-checker
///
/// Uncomment the invalid access at the bottom to see the compiler reject
/// an out-of-bounds index. The type system acts as a proof checker:
/// you cannot construct a {@link Get} witness for an invalid index.
public final class TypeLevelDemo {

  public static void main() {

    System.out.println("=== Type-level heterogeneous list ===");

    // Build the list: ["Hello", 42, true]
    // The type is: HCons<String, HCons<Integer, HCons<Boolean, HNil>>>
    HCons<String, HCons<Integer, HCons<Boolean, HNil>>> list =
        HList.nil().cons(true).cons(42).cons("Hello");

    System.out.println("  List built: [\"Hello\", 42, true]");
    System.out.println();

    // ── Type-safe access ─────────────────────────────────────────────────

    System.out.println("=== Type-safe index access (no casts!) ===");

    // Index 0 → String (compiler knows this)
    Get<HCons<String, HCons<Integer, HCons<Boolean, HNil>>>, Nat.Zero, String> idx0 = Get.zeroIdx();

    // Index 1 → Integer (compiler knows this)
    Get<HCons<String, HCons<Integer, HCons<Boolean, HNil>>>, Nat.Succ<Nat.Zero>, Integer> idx1 =
        Get.succIdx(Get.zeroIdx());

    // Index 2 → Boolean (compiler knows this)
    Get<HCons<String, HCons<Integer, HCons<Boolean, HNil>>>, Nat.Succ<Nat.Succ<Nat.Zero>>, Boolean>
        idx2 = Get.succIdx(Get.succIdx(Get.zeroIdx()));

    // Extract — return types are String, Integer, Boolean. No cast needed.
    String s = idx0.extract(list);
    Integer i = idx1.extract(list);
    Boolean b = idx2.extract(list);

    System.out.println("  idx0 → " + s + "  (type: String)");
    System.out.println("  idx1 → " + i + "  (type: Integer)");
    System.out.println("  idx2 → " + b + "  (type: Boolean)");

    // ── The compiler-as-proof-checker ────────────────────────────────────

    System.out.println();
    System.out.println("=== Compile-time bounds check ===");
    System.out.println("  Accessing index 3 on a 3-element list:");
    System.out.println("  Uncomment the code below → compiler rejects it.");
    System.out.println();
    System.out.println("  // Get<..., Succ<Succ<Succ<Zero>>>, ?> idx3 =");
    System.out.println("  //     Get.succIdx(Get.succIdx(Get.succIdx(Get.zeroIdx())));");
    System.out.println("  // ❌ COMPILER ERROR — cannot unify types.");
    System.out.println("  //    The type system proves the index is out of bounds.");

    // Uncomment to see the compile error:
    // Get<HCons<String, HCons<Integer, HCons<Boolean, HNil>>>,
    // Nat.Succ<Nat.Succ<Nat.Succ<Nat.Zero>>>, Object> idx3 =
    // Get.succIdx(Get.succIdx(Get.succIdx(Get.zeroIdx())));
  }
}
