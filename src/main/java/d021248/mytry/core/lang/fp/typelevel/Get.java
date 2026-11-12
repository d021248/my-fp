package d021248.mytry.core.lang.fp.typelevel;

/// Type-safe index access into an {@link HList} via induction proof.
///
/// The compiler verifies at compile time that the index is within bounds.
/// There are exactly two "proof rules":
///
/// 1. **Base case** ({@link #zeroIdx}): index 0 into a non-empty list
///    returns the head — always valid.
/// 2. **Inductive step** ({@link #succIdx}): index N+1 into a non-empty
///    list is valid if index N into the tail is valid.
///
/// If you try to construct a {@code Get} for an out-of-bounds index, the
/// compiler cannot unify the types and rejects the code.
public interface Get<L extends HList, Index extends Nat, Result> {

  /// Extracts the element at the encoded index.
  Result extract(L list);

  /// Base case: index 0 always extracts the head of a non-empty list.
  static <H, T extends HList> Get<HCons<H, T>, Nat.Zero, H> zeroIdx() {
    return HCons::head;
  }

  /// Inductive step: index N+1 is valid iff index N is valid in the tail.
  static <H, T extends HList, N extends Nat, R> Get<HCons<H, T>, Nat.Succ<N>, R> succIdx(
      Get<T, N, R> getTail) {
    return list -> getTail.extract(list.tail());
  }
}
