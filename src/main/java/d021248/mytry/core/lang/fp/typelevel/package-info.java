/// Type-level programming in Java — computations the compiler executes at
/// compile time, not the JVM at runtime.
///
/// This package encodes natural numbers and heterogeneous lists in the type
/// system, then uses **type-level induction** to prove at compile time that
/// an index access is within bounds.
///
/// ## Key types
///
/// - {@link d021248.mytry.core.lang.fp.typelevel.Nat} — Peano natural
///   numbers (`Zero`, `Succ<N>`)
/// - {@link d021248.mytry.core.lang.fp.typelevel.HList} /
///   {@link d021248.mytry.core.lang.fp.typelevel.HCons} /
///   {@link d021248.mytry.core.lang.fp.typelevel.HNil} — heterogeneous list
///   where each element has its own type
/// - {@link d021248.mytry.core.lang.fp.typelevel.Get} — type-safe
///   index access via induction proof: the compiler rejects out-of-bounds
///   indices
///
/// ## Relation to other packages
///
/// - {@code fp/typeclass/} — type classes add *behaviour* to types;
///   this package encodes *data structures* in types
/// - {@code phantomtypes/} — phantom type parameters encode state;
///   this package encodes natural numbers (Peano) and list structure
///
/// @since 1.0
/// @see d021248.mytry.core.lang.fp.typeclass
/// @see d021248.mytry.core.lang.phantomtypes
package d021248.mytry.core.lang.fp.typelevel;
