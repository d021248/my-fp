/// Tagless-final encoding — interpreting a DSL without intermediate data structures.
///
/// ## What is tagless-final?
///
/// Tagless-final (also called *object algebra* in OOP literature) encodes a DSL
/// as an interface parameterised by a **carrier type** `F`.  Every constructor
/// of the language becomes a method; every interpreter is an implementation of
/// the interface that supplies a concrete `F`.
///
/// ```
/// interface Expr<F> {
///     F lit(int n);
///     F add(F left, F right);
/// }
/// ```
///
/// - `EvalInterpreter implements Expr<Integer>` evaluates expressions to integers.
/// - `PrettyPrinter implements Expr<String>` renders them as strings.
///
/// The same "program" (sequence of algebra calls) works with any interpreter
/// without modification — this is the **expression problem** solved at the call site.
///
/// ## Contrast with the Free monad
///
/// | Approach | DSL representation | Interpretation |
/// |---|---|---|
/// | Free monad | data structure (tree of constructors) | fold over the tree |
/// | Tagless-final | direct function calls | supply different implementation |
///
/// Free monads trade flexibility (inspect / transform the program) for overhead
/// (allocating ADT nodes). Tagless-final is lighter but the program cannot be
/// inspected at runtime.
///
/// ## Key types
///
/// - {@link d021248.mytry.core.lang.fp.tagless.Expr} — the algebra interface
/// - {@link d021248.mytry.core.lang.fp.tagless.EvalInterpreter} — `Expr<Integer>`
/// - {@link d021248.mytry.core.lang.fp.tagless.PrettyPrinter} — `Expr<String>`
///
/// @since 1.0
package d021248.mytry.core.lang.fp.tagless;
