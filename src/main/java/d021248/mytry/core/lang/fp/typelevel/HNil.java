package d021248.mytry.core.lang.fp.typelevel;

/// The empty heterogeneous list — base case for {@link HList}.
final class HNil implements HList {
  static final HNil INSTANCE = new HNil();

  private HNil() {}

  @Override
  public Nat.Zero length() {
    return null; // type-level only — never called at runtime
  }

  /// Prepends an element, producing an {@link HCons}.
  public <H> HCons<H, HNil> cons(H head) {
    return new HCons<>(head, this);
  }
}
