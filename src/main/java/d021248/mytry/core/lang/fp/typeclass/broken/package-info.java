/// ANTI-PATTERN: using subclassing to add behaviour to types.
///
/// Subclassing for ad-hoc polymorphism breaks when you need two different
/// behaviours for the same type, or when the type comes from a library.
///
/// The correct approach is in {@link d021248.mytry.core.lang.fp.typeclass}.
///
/// @since 1.0
package d021248.mytry.core.lang.fp.typeclass.broken;
