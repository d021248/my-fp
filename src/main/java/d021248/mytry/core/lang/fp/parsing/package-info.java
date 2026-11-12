/// Parser combinators derived from FP first principles (Functor → Applicative → Monad).
///
/// ## Purpose
///
/// This package shows **how** a parser combinator library is built by stacking
/// three FP abstractions.  The focus is on the theory; the return type is
/// `Optional` — failures carry no message.  For a production-style implementation
/// with `Result`-based error messages see {@link d021248.mytry.core.lang.parser}.
///
/// ## The `Parser<A>` abstraction
///
/// A `Parser<A>` is a function `String → Optional<ParseResult<A>>`.  A failed
/// parse returns `Optional.empty()`; a successful parse returns the typed value
/// and the unconsumed input tail wrapped in `ParseResult<A>`.
///
/// ## Functor / Applicative / Monad
///
/// `Parser<A>` supports all three layers of abstraction:
///
/// | Layer | Method | Use |
/// |---|---|---|
/// | Functor | `map` | transform the parsed value |
/// | Applicative | `ap` | combine independent parsers |
/// | Monad | `flatMap` | chain where the second parser depends on the first |
///
/// ## Relationship to other packages
///
/// - **`applicative`** — general Functor / Applicative / Monad hierarchy that
///   `Parser` follows informally through its `map` / `ap` / `flatMap` API
/// - **`kleisli`** — a Kleisli arrow `A → Optional<B>` is structurally identical
///   to a parser that reads nothing; `Parser` is a richer Kleisli arrow over the
///   remaining-string state
/// - **`core.lang.parser`** — the pragmatic counterpart: same combinators, richer
///   error messages via `Result`, Java-idiomatic naming
///
/// @since 1.0
package d021248.mytry.core.lang.fp.parsing;
