package d021248.mytry.core.lang.fp.persistent;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/// An immutable, persistently shared vector backed by a 32-way trie.
///
/// All mutating operations (`append`, `set`, `update`) return a **new** vector
/// that shares almost all internal tree nodes with the original — no copying.
/// The original vector is unchanged.
///
/// ## Structural sharing
///
/// The trie has branching factor 32 (`BITS = 5`, `BRANCH = 32`).
/// A vector of N elements has depth `⌈log₃₂ N⌉`.
/// `append` and `set` copy only the one path from root to the affected leaf —
/// O(log₃₂ N) node copies, effectively O(1) amortized.
///
/// ## Complexity
///
/// | Operation | Complexity |
/// |---|---|
/// | `get(i)` | O(log₃₂ N) ≈ O(1) |
/// | `append(e)` | O(log₃₂ N) ≈ O(1) |
/// | `set(i, e)` | O(log₃₂ N) ≈ O(1) |
/// | `size()` | O(1) |
///
/// ## Limitations
///
/// This implementation does not include a tail buffer optimisation (Clojure
/// does add one).  Appending sequentially therefore copies one path per
/// element.  For bulk construction use `PersistentVector.of(...)`.
///
/// @param <E> the element type
public final class PersistentVector<E> implements Iterable<E> {

  private static final int BITS = 5;
  private static final int BRANCH = 1 << BITS; // 32
  private static final int MASK = BRANCH - 1; // 0x1F

  private static final Object[] EMPTY_NODE = new Object[0];

  private final int size;
  private final int depth; // shift amount for root, e.g. 5, 10, 15 …
  private final Object root; // Object[] (internal) or E[] (leaf)

  // ── Private constructor ───────────────────────────────────────────────────

  private PersistentVector(int size, int depth, Object root) {
    this.size = size;
    this.depth = depth;
    this.root = root;
  }

  // ── Factory ───────────────────────────────────────────────────────────────

  private static <E> PersistentVector<E> empty() {
    return new PersistentVector<>(0, 0, EMPTY_NODE);
  }

  /// Returns an empty `PersistentVector`.
  public static <E> PersistentVector<E> of() {
    return empty();
  }

  /// Returns a `PersistentVector` containing the given elements in order.
  @SafeVarargs
  public static <E> PersistentVector<E> of(E... elements) {
    var v = PersistentVector.<E>empty();
    for (var e : elements) v = v.append(e);
    return v;
  }

  // ── Accessors ─────────────────────────────────────────────────────────────

  /// Returns the number of elements.
  public int size() {
    return size;
  }

  /// Returns `true` if this vector contains no elements.
  public boolean isEmpty() {
    return size == 0;
  }

  /// Returns the element at index `i`.
  @SuppressWarnings("unchecked")
  public E get(int i) {
    Objects.checkIndex(i, size);
    return (E) findLeaf(root, depth, i)[i & MASK];
  }

  // ── Structural operations ─────────────────────────────────────────────────

  /// Returns a new vector with `element` appended at the end.
  public PersistentVector<E> append(E element) {
    var newRoot = insertPath(root, depth, size, element);
    var newDepth = depth;

    // Root overflowed — add a new trie level
    if (newRoot == null) {
      newRoot = new Object[] {root, newPath(depth, element)};
      newDepth = depth + BITS;
    }
    return new PersistentVector<>(size + 1, newDepth, newRoot);
  }

  /// Returns a new vector with position `i` replaced by `element`.
  public PersistentVector<E> set(int i, E element) {
    Objects.checkIndex(i, size);
    return new PersistentVector<>(size, depth, updatePath(root, depth, i, element));
  }

  /// Returns a new vector with `f` applied to the element at index `i`.
  public PersistentVector<E> update(int i, UnaryOperator<E> f) {
    return set(i, f.apply(get(i)));
  }

  /// Returns a new vector with `f` applied to every element.
  public PersistentVector<E> map(Function<E, E> f) {
    var result = PersistentVector.<E>empty();
    for (var e : this) result = result.append(f.apply(e));
    return result;
  }

  /// Returns a new vector containing only elements that match `pred`.
  public PersistentVector<E> filter(Predicate<E> pred) {
    var result = PersistentVector.<E>empty();
    for (var e : this) if (pred.test(e)) result = result.append(e);
    return result;
  }

  /// Returns a `Stream` of this vector's elements.
  public Stream<E> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  // ── Iterator ──────────────────────────────────────────────────────────────

  @Override
  public Iterator<E> iterator() {
    return new Iterator<>() {
      private int idx = 0;

      public boolean hasNext() {
        return idx < size;
      }

      public E next() {
        if (!hasNext()) throw new NoSuchElementException();
        return get(idx++);
      }
    };
  }

  // ── Object ────────────────────────────────────────────────────────────────

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof PersistentVector<?> other)) return false;
    if (size != other.size) return false;
    var it1 = iterator();
    var it2 = other.iterator();
    while (it1.hasNext()) if (!Objects.equals(it1.next(), it2.next())) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int h = 1;
    for (var e : this) h = 31 * h + Objects.hashCode(e);
    return h;
  }

  @Override
  public String toString() {
    var sb = new StringBuilder("[");
    var sep = "";
    for (var e : this) {
      sb.append(sep).append(e);
      sep = ", ";
    }
    return sb.append("]").toString();
  }

  // ── Internal trie operations ──────────────────────────────────────────────

  private static Object[] findLeaf(Object node, int depth, int i) {
    var current = node;
    for (var level = depth; level > 0; level -= BITS) {
      var arr = (Object[]) current;
      current = arr[(i >>> level) & MASK];
    }
    return (Object[]) current;
  }

  // Returns updated root, or null if the current level is completely full (all
  // BRANCH slots used).
  private static Object insertPath(Object node, int depth, int i, Object element) {
    if (depth == 0) {
      // leaf
      var leaf = (Object[]) node;
      if (leaf.length == BRANCH) return null; // full — signal overflow to parent
      var newLeaf = Arrays.copyOf(leaf, leaf.length + 1);
      newLeaf[leaf.length] = element;
      return newLeaf;
    }
    // internal node
    var arr = (Object[]) node;
    var childIdx = (i >>> depth) & MASK;
    if (childIdx >= arr.length) {
      // slot doesn't exist yet — append a fresh path
      if (arr.length == BRANCH) return null;
      var newArr = Arrays.copyOf(arr, arr.length + 1);
      newArr[arr.length] = newPath(depth - BITS, element);
      return newArr;
    }
    var updatedChild = insertPath(arr[childIdx], depth - BITS, i, element);
    if (updatedChild == null) {
      // child is full — add a sibling slot at this level
      if (arr.length == BRANCH) return null;
      var newArr = Arrays.copyOf(arr, arr.length + 1);
      newArr[arr.length] = newPath(depth - BITS, element);
      return newArr;
    }
    var newArr = Arrays.copyOf(arr, arr.length);
    newArr[childIdx] = updatedChild;
    return newArr;
  }

  private static Object newPath(int depth, Object element) {
    if (depth == 0) return new Object[] {element};
    return new Object[] {newPath(depth - BITS, element)};
  }

  private static Object updatePath(Object node, int depth, int i, Object element) {
    if (depth == 0) {
      var leaf = (Object[]) node;
      var newLeaf = Arrays.copyOf(leaf, leaf.length);
      newLeaf[i & MASK] = element;
      return newLeaf;
    }
    var arr = (Object[]) node;
    var childIdx = (i >>> depth) & MASK;
    var newArr = Arrays.copyOf(arr, arr.length);
    newArr[childIdx] = updatePath(arr[childIdx], depth - BITS, i, element);
    return newArr;
  }
}
