package d021248.mytry.core.lang.fp.cofree;

import module java.base;

/// A rose tree (multiway tree) where every node carries a value — the Cofree
/// comonad over `List` (the "many children" functor).
///
/// `CofreeRoseTree<A>` demonstrates that `CofreeStream<A>` is just one
/// specialisation of the Cofree pattern: swapping `Supplier<CofreeStream<A>>`
/// (one child, lazy) for `List<CofreeRoseTree<A>>` (N children, eager) gives
/// a proper rose tree with the same comonadic structure.
///
/// ## Cofree comonad over F = List
///
/// ```
/// CofreeRoseTree<A> = (A, List<CofreeRoseTree<A>>)
///                   = (A, F(CofreeRoseTree<A>))   where F = List
/// ```
///
/// ## Comonad operations
///
/// - `extract` — returns the value at the root node
/// - `extend(f)` — replaces every node's value with `f` applied to the subtree
///   rooted at that node; the shape is preserved
/// - `map(g)` — applies `g` to every node's value
///
/// ## Laws
///
/// 1. **extract ∘ extend f == f** (evaluating the extended tree at root = f of the tree)
/// 2. **extend extract == identity** (extending with extract is a no-op)
/// 3. **extend f ∘ extend g == extend (f ∘ extend g)** (associativity)
///
/// @param <A> the value stored at each node
public record CofreeRoseTree<A>(A value, List<CofreeRoseTree<A>> children) {

  // ── Factory ───────────────────────────────────────────────────────────────

  /// Creates a leaf node (no children).
  public static <A> CofreeRoseTree<A> leaf(A value) {
    return new CofreeRoseTree<>(value, List.of());
  }

  /// Creates a node with explicit children.
  @SafeVarargs
  public static <A> CofreeRoseTree<A> node(A value, CofreeRoseTree<A>... children) {
    return new CofreeRoseTree<>(value, List.of(children));
  }

  /// Builds a tree from a seed by repeatedly applying `step` to produce
  /// `(value, list-of-child-seeds)` until the child list is empty.
  public static <A, S> CofreeRoseTree<A> unfold(S seed, Function<S, Pair<A, S>> step) {
    var pair = step.apply(seed);
    var childTrees = pair.childSeeds().stream().map(s -> unfold(s, step)).toList();
    return new CofreeRoseTree<>(pair.value(), childTrees);
  }

  /// Helper record returned by the `unfold` seed function: a node value and
  /// a list of seeds for child nodes.
  public record Pair<A, S>(A value, List<S> childSeeds) {}

  // ── Comonad operations ────────────────────────────────────────────────────

  /// Returns the value at the root.
  public A extract() {
    return value;
  }

  /// Applies `f` to every subtree rooted at each node, replacing node values.
  ///
  /// This is the comonad `extend` (cobind) operation: every node in the result
  /// carries `f` applied to the subtree rooted at that node in the original tree.
  public <B> CofreeRoseTree<B> extend(Function<CofreeRoseTree<A>, B> f) {
    return new CofreeRoseTree<>(f.apply(this), children.stream().map(c -> c.extend(f)).toList());
  }

  /// Applies `g` to every node value.
  public <B> CofreeRoseTree<B> map(Function<A, B> g) {
    return new CofreeRoseTree<>(g.apply(value), children.stream().map(c -> c.map(g)).toList());
  }

  // ── Utilities ─────────────────────────────────────────────────────────────

  /// Returns a flat list of all values in depth-first (pre-order) order.
  public List<A> toList() {
    var result = new ArrayList<A>();
    result.add(value);
    for (var child : children) result.addAll(child.toList());
    return Collections.unmodifiableList(result);
  }

  /// Returns the depth of the tree (0 = leaf).
  public int depth() {
    if (children.isEmpty()) return 0;
    return 1 + children.stream().mapToInt(CofreeRoseTree::depth).max().orElse(0);
  }

  /// Returns the total number of nodes.
  public int size() {
    return 1 + children.stream().mapToInt(CofreeRoseTree::size).sum();
  }
}
