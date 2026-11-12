/// Type-class simulation — separating data from behaviour via capability interfaces.
///
/// A *type class* (Haskell, Scala, Rust traits) lets you attach new behaviour
/// to any type — including third-party types you cannot modify — without
/// inheritance.  Java does not have type classes natively, but the pattern
/// can be approximated with interfaces and explicit instances.
///
/// ## Sub-packages
///
///   - {@link d021248.mytry.core.lang.fp.typeclass.broken} — anti-pattern:
///       using subclassing to add behaviour; breaks for external types and
///       produces awkward multi-inheritance hierarchies
///   - {@link d021248.mytry.core.lang.fp.typeclass.Show} and
///       {@link d021248.mytry.core.lang.fp.typeclass.Eq} — capability interfaces
///       whose *instances* are ordinary values, composable with `contramap`
///       and derived combinators
///
/// @since 1.0
package d021248.mytry.core.lang.fp.typeclass;
