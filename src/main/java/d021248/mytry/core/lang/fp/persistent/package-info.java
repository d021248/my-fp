/// Persistent (structurally shared) immutable data structures.
///
/// An operation that "modifies" a persistent data structure returns a **new**
/// version sharing almost all internal structure with the original.  Both
/// versions remain valid simultaneously — there are no copies, and no locks.
///
/// ## Contents
///
/// - {@link d021248.mytry.core.lang.fp.persistent.PersistentVector} — an
///     immutable vector backed by a 32-way trie (`get`, `append`, `set`,
///     `update` all in O(log₃₂ N) ≈ O(1))
///
/// ## Relation to other FP packages
///
/// Persistent structures are a natural complement to the existing
/// {@link d021248.mytry.core.lang.fp.optics} package: lenses let you focus
/// into a record; persistent structures let that focus produce a new version
/// without destroying the old one.
///
/// @since 1.0
package d021248.mytry.core.lang.fp.persistent;
