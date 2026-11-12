package d021248.mytry.core.lang.fp.typelevel;

/// A non-empty heterogeneous list cell: a head element and a tail list.
///
/// The type parameters encode both the head type and the full structure
/// of the remaining list.
record HCons<Head, Tail extends HList>(Head head, Tail tail) implements HList {

  @Override
  public Nat.Succ<? extends Nat> length() {
    return null; // type-level only — never called at runtime
  }

  /// Prepends an element, producing a longer {@link HCons}.
  public <H> HCons<H, HCons<Head, Tail>> cons(H head) {
    return new HCons<>(head, this);
  }
}
