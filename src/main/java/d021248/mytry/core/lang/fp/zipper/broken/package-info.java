/// Comparison: a mutable list editor with an integer cursor.
///
/// The mutable approach requires keeping the list and cursor in sync, is
/// prone to index errors, and has no clean equivalent of the comonad `extend`.
///
/// Compare with `ListZipper` which is immutable, navigates in O(1),
/// and provides comonad operations.
///
/// @since 1.0
package d021248.mytry.core.lang.fp.zipper.broken;
