/// Functional zipper — a data structure with a focused element.
///
/// A `ListZipper<A>` = `(reversed-left, focus, right)`.  Navigation and
/// update at the focus are O(1); the full list is reconstructed in O(n).
///
/// ## Connection to the Store comonad
///
/// `ListZipper<A>` is isomorphic to `Store<Integer, A>`:
///
/// - `extract()` = `store.extract()` (the focused value)
/// - `extend(f)` = `store.extend(f)` (context-sensitive transform everywhere)
/// - `moveLeft/Right` = `store.seek(pos ± 1)`
///
/// ## Connection to Lens
///
/// The focus is a Lens into the zipper: `get(z)` = `z.focus()`,
/// `set(z, v)` = `z.replace(v)`.
///
/// ## Key types
///
/// - `ListZipper` — immutable list with focus
///
/// ## Sub-packages
///
/// - `d021248.mytry.core.lang.fp.zipper.broken` — mutable editor with integer cursor
///
/// @since 1.0
package d021248.mytry.core.lang.fp.zipper;
