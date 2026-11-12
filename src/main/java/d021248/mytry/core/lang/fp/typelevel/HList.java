package d021248.mytry.core.lang.fp.typelevel;

/// A heterogeneous list where each element carries its own type.
///
/// Unlike {@code List<Object>}, the compiler tracks every element type
/// individually. The list {@code ["hello", 42, true]} has the precise type
/// {@code HCons<String, HCons<Integer, HCons<Boolean, HNil>>>}.
public sealed interface HList permits HNil, HCons {

  /// Returns the length as a Peano-number type.
  Nat length();

  /// Creates an empty list.
  static HNil nil() {
    return HNil.INSTANCE;
  }
}
