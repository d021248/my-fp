/// Comparison: explicit structural recursion duplicated per operation.
///
/// Each function that traverses an expression tree must repeat the recursive
/// pattern itself. The parent package's recursion schemes factor that traversal
/// into `cata`, `ana`, and `hylo`.
///
/// @since 1.0
package d021248.mytry.core.lang.fp.recursionschemes.broken;
