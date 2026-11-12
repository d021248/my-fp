package d021248.mytry.core.lang.fp.comonad.broken;

/// Demonstrates context-sensitive transformation using mutable state instead of a comonad.
///
/// Without the Store comonad, computing a context-sensitive value (e.g., a
/// neighbourhood sum) requires either mutating the array in-place or writing an
/// explicit index-based loop — the context-threading logic is mixed with the
/// transformation logic.
///
/// Compare with {@link d021248.mytry.core.lang.fp.comonad.Store} which expresses
/// the same pattern with `extend`.
public final class MutableContext {

  static int[] neighbourSum(int[] cells) {
    var result = new int[cells.length];
    for (int i = 0; i < cells.length; i++) {
      int left = i > 0 ? cells[i - 1] : 0;
      int center = cells[i];
      int right = i < cells.length - 1 ? cells[i + 1] : 0;
      result[i] = left + center + right; // context-sensitive — but inline and manual
    }
    return result;
  }

  public static void main() {
    int[] input = {1, 2, 3, 4, 5};
    int[] output = neighbourSum(input);

    System.out.print("Input : ");
    for (int v : input) System.out.printf("%3d", v);
    System.out.println();
    System.out.print("Output: ");
    for (int v : output) System.out.printf("%3d", v);
    System.out.println();
    System.out.println();
    System.out.println("Manual loops couple traversal and transformation.");
    System.out.println("Store.extend() decouples them — the neighbourhood pattern");
    System.out.println("becomes a reusable higher-order function.");
  }
}
