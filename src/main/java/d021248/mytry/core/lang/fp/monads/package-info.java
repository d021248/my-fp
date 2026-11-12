/// Reader, Writer, and State monads for purely functional effect handling.
///
/// These three monads each model a different computational effect without
/// mutation, hidden state, or implicit globals:
///
/// | Monad | Effect | Core operation |
/// |-------|--------|----------------|
/// | `Reader<E,A>` | Read-only dependency injection | `run(E env)` |
/// | `Writer<W,A>` | Log/accumulation | `(W log, A value)` record |
/// | `State<S,A>` | Stateful sequential computation | `run(S state)` |
///
/// ## Why these belong together
///
/// All three are monads — they share the `pure`/`flatMap` structure.
/// `State<S,A>` generalises both `Reader` (read-only `get`) and a simplified
/// `Writer` (write-only `put`). Seeing them side by side makes the monad
/// pattern easier to recognise.
///
/// ## Sub-packages
///
/// - {@link d021248.mytry.core.lang.fp.monads.broken} — comparison: manual
///   state threading without the monad abstraction
///
/// @since 1.0
package d021248.mytry.core.lang.fp.monads;
