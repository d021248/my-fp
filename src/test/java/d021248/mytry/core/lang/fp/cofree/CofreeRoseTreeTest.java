package d021248.mytry.core.lang.fp.cofree;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CofreeRoseTreeTest {

  // ── leaf and node factories ───────────────────────────────────────────────

  @Test
  @DisplayName("leaf creates a node with no children")
  void leafHasNoChildren() {
    var leaf = CofreeRoseTree.leaf(42);
    assertThat(leaf.extract()).isEqualTo(42);
    assertThat(leaf.children()).isEmpty();
  }

  @Test
  @DisplayName("node factory creates a node with the given children")
  void nodeCreatesWithChildren() {
    var tree = CofreeRoseTree.node("root", CofreeRoseTree.leaf("a"), CofreeRoseTree.leaf("b"));
    assertThat(tree.extract()).isEqualTo("root");
    assertThat(tree.children()).hasSize(2);
  }

  // ── size and depth ────────────────────────────────────────────────────────

  @Test
  @DisplayName("size of a leaf is 1")
  void leafSizeIsOne() {
    assertThat(CofreeRoseTree.leaf(0).size()).isEqualTo(1);
  }

  @Test
  @DisplayName("size counts all nodes in the tree")
  void sizeCountsAllNodes() {
    var tree =
        CofreeRoseTree.node(
            1,
            CofreeRoseTree.node(2, CofreeRoseTree.leaf(4), CofreeRoseTree.leaf(5)),
            CofreeRoseTree.leaf(3));
    assertThat(tree.size()).isEqualTo(5);
  }

  @Test
  @DisplayName("depth of a leaf is 0")
  void leafDepthIsZero() {
    assertThat(CofreeRoseTree.leaf("x").depth()).isEqualTo(0);
  }

  @Test
  @DisplayName("depth returns the maximum path length from root to leaf")
  void depthReturnsMaxPath() {
    var tree =
        CofreeRoseTree.node(
            1, CofreeRoseTree.node(2, CofreeRoseTree.leaf(4)), CofreeRoseTree.leaf(3));
    assertThat(tree.depth()).isEqualTo(2);
  }

  // ── toList ────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("toList returns values in pre-order depth-first order")
  void toListPreOrder() {
    var tree =
        CofreeRoseTree.node(
            1,
            CofreeRoseTree.node(2, CofreeRoseTree.leaf(4), CofreeRoseTree.leaf(5)),
            CofreeRoseTree.leaf(3));
    assertThat(tree.toList()).containsExactly(1, 2, 4, 5, 3);
  }

  // ── map ───────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("map applies f to every node value, preserving structure")
  void mapTransformsAllValues() {
    var tree = CofreeRoseTree.node(1, CofreeRoseTree.leaf(2), CofreeRoseTree.leaf(3));
    var doubled = tree.map(n -> n * 2);
    assertThat(doubled.toList()).containsExactly(2, 4, 6);
  }

  @Test
  @DisplayName("map with identity is a no-op")
  void mapIdentityIsNoop() {
    var tree = CofreeRoseTree.node("a", CofreeRoseTree.leaf("b"));
    assertThat(tree.map(s -> s).toList()).isEqualTo(tree.toList());
  }

  // ── extend (comonad cobind) ───────────────────────────────────────────────

  @Test
  @DisplayName("extend extract is identity (comonad right law)")
  void extendExtractIsIdentity() {
    var tree = CofreeRoseTree.node(10, CofreeRoseTree.leaf(20), CofreeRoseTree.leaf(30));
    assertThat(tree.extend(CofreeRoseTree::extract).toList()).isEqualTo(tree.toList());
  }

  @Test
  @DisplayName("extend replaces each node with f applied to the subtree rooted there")
  void extendAppliesFToEachSubtree() {
    // f = size of the subtree rooted at each node
    var tree =
        CofreeRoseTree.node(
            1,
            CofreeRoseTree.node(2, CofreeRoseTree.leaf(4), CofreeRoseTree.leaf(5)),
            CofreeRoseTree.leaf(3));
    var sizes = tree.extend(CofreeRoseTree::size);
    // root has 5 nodes, left child has 3 nodes, right child has 1 node, leaves are
    // 1
    assertThat(sizes.toList()).containsExactly(5, 3, 1, 1, 1);
  }

  // ── unfold ────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("unfold builds a binary tree from a depth-and-value seed")
  void unfoldBuildsBinaryTree() {
    // Unfold: seed = (depth, value); children = [(depth-1, value*2), (depth-1,
    // value*2+1)]
    var tree =
        CofreeRoseTree.unfold(
            new int[] {2, 1},
            arr ->
                new CofreeRoseTree.Pair<>(
                    arr[1],
                    arr[0] == 0
                        ? List.of()
                        : List.of(
                            new int[] {arr[0] - 1, arr[1] * 2},
                            new int[] {arr[0] - 1, arr[1] * 2 + 1})));
    assertThat(tree.size()).isEqualTo(7);
    assertThat(tree.depth()).isEqualTo(2);
    assertThat(tree.extract()).isEqualTo(1);
  }
}
