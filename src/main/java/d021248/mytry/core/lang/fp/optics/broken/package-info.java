/// ANTI-PATTERN: manual `new` chains for updating deeply-nested records.
///
/// Every update requires reconstructing the entire path from root to the
/// changed leaf, repeating every sibling field by hand.
///
/// The correct approach is in {@link d021248.mytry.core.lang.fp.optics}.
///
/// @since 1.0
package d021248.mytry.core.lang.fp.optics.broken;
