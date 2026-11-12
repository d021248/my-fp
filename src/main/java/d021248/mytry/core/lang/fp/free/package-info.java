/// Free monad — programs as pure data, interpreters as plug-ins.
///
/// ## Core idea
///
/// A `Free<A>` value is a **description** of a key-value store program.
/// No I/O happens until you call `Free.run(program, interpreter)`.
/// Swapping the interpreter changes the execution semantics without
/// touching the program:
///
/// | Interpreter | What it does |
/// |---|---|
/// | `Free.mapInterpreter(map)` | real mutations on a `HashMap` |
/// | `Free.loggingInterpreter(delegate, log)` | records ops, then delegates |
/// | `Free.dryRunInterpreter(readStore)` | reads only; writes are no-ops |
///
/// ## Structure
///
/// - {@link d021248.mytry.core.lang.fp.free.StoreOp} — the DSL functor
///   (`Get`, `Put`, `Delete`), each carrying its continuation
/// - {@link d021248.mytry.core.lang.fp.free.Free} — the Free monad
///   (`Pure`, `Lift`, `Bind`), smart constructors, `run`, and interpreter factories
/// - {@link d021248.mytry.core.lang.fp.free.FreeDemo} — three-interpreter demo
///
/// ## Why Java needs raw types in the runner
///
/// Java lacks higher-kinded types: `Free<F, A>` for a generic functor `F`
/// cannot be expressed.  Instead, `Free` is tied to `StoreOp` directly and the
/// interpreter loop uses raw types internally (suppressed warnings).
/// The public API remains fully type-safe.
///
/// @since 1.0
package d021248.mytry.core.lang.fp.free;
