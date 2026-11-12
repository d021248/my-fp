package d021248.mytry.core.lang.fp.cofree.broken;

import module java.base;

/// Eager finite stream built into an {@code ArrayList}.
///
/// Problems compared to {@link d021248.mytry.core.lang.fp.cofree.CofreeStream}:
///
/// - Must know the final size upfront — cannot be truly infinite.
/// - Memory is allocated eagerly even if only the first element is needed.
/// - No `extend`: applying a context-sensitive function requires manual loops
///   with index arithmetic.
public final class EagerStream {

  private EagerStream() {}

  /// Builds the first `n` natural numbers eagerly.
  public static List<Integer> naturals(int n) {
    var result = new ArrayList<Integer>(n);
    for (int i = 0; i < n; i++) result.add(i);
    return List.copyOf(result);
  }

  /// "Running sum" of adjacent pairs — requires manual index arithmetic.
  public static List<Integer> runningPairSums(List<Integer> list) {
    var result = new ArrayList<Integer>(list.size() - 1);
    for (int i = 0; i < list.size() - 1; i++) {
      result.add(list.get(i) + list.get(i + 1));
    }
    return List.copyOf(result);
  }

  public static void main() {
    System.out.println("=== Eager stream (broken) ===");
    var nums = naturals(10);
    System.out.println("First 10 naturals: " + nums);
    System.out.println("Pair sums: " + runningPairSums(nums));
    System.out.println("Problem: we had to decide on size=10 upfront.");
  }
}
