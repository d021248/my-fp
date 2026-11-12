package d021248.mytry.core.lang.fp.typelevel;

/// Peano natural numbers encoded in the type system.
///
/// {@code Zero} represents 0, {@code Succ<N>} represents N + 1.
/// These types are never used at runtime for arithmetic — they exist
/// purely so the compiler can count and verify bounds at compile time.
public sealed interface Nat permits Nat.Zero, Nat.Succ {

  /// Represents the number 0.
  record Zero() implements Nat {}

  /// Represents the successor of a number (N + 1).
  record Succ<N extends Nat>(N pred) implements Nat {}
}
