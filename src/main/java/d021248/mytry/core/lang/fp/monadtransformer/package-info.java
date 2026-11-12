/// Monad transformers — stacking two monads into one.
///
/// A **monad transformer** wraps one monad around another so that their
/// effects are available simultaneously. Instead of choosing between
/// "has state" **or** "may fail", you get both at once in a single pipeline.
///
/// ## How it works
///
/// Given a base monad `M` (here `Optional`) and an effect layer (Reader or
/// State), the transformer produces a new type whose `flatMap` sequences
/// both effects at once:
///
/// | Transformer | Type | Effect combo |
/// |-------------|------|--------------|
/// | `ReaderT<E,A>` | `E → Optional<A>` | Read-only env + may fail |
/// | `StateT<S,A>` | `S → Optional<(A,S)>` | Mutable state + may fail |
///
/// ## Key types
///
///   - {@link d021248.mytry.core.lang.fp.monadtransformer.ReaderT} —
///       Reader over Optional: access shared config with the ability to fail
///   - {@link d021248.mytry.core.lang.fp.monadtransformer.StateT} —
///       State over Optional: thread state with the ability to fail
///
/// @since 1.0
package d021248.mytry.core.lang.fp.monadtransformer;
