# CLAUDE.md — my-fp

## Project Overview

Functional programming patterns in Java 25 (`d021248.mytry.core.lang.fp`, `d021248.mytry.core.lang.adt`).
Java 25, Maven build. Extracted from [my-try](https://github.com/d021248/my-try).

> **Architectural excellence is not a means but a goal.**
> No ad-hoc solutions. No classes with more than one responsibility. No hidden side effects.
> No premature abstractions. Every design decision is explicit and justified — the code is the learning material.

---

## Build & Test

```bash
mvn compile                  # compile only
mvn test                     # all tests
mvn test -Dtest=ClassName    # single test class
mvn verify                   # tests + JaCoCo coverage check + Spotless check
```

Java version: **25** (defined in `pom.xml` via `<maven.compiler.source>`).

---

## Key Conventions

See [my-try/CLAUDE.md](https://github.com/d021248/my-try/blob/main/CLAUDE.md) for the full project
style guide. This repo follows the same Java 25, Javadoc (`///`), test, and naming conventions.

---

## Package Map

```
src/main/java/d021248/mytry/
│
├── core/lang/adt/           algebraic data types — Either<L,R>, sealed hierarchies
│   └── quantum/             QuantumOp sum type (sealed + records demo)
│
├── core/lang/fp/            functional programming patterns
│   ├── applicative/         Validated<E,A> — Applicative with error accumulation
│   ├── arrows/              Arrow typeclass — arr, first, >>> composition
│   ├── bifunctor/           Bifunctor — map over both type parameters
│   ├── church/              Church encoding — booleans, naturals, lists, Y combinator
│   ├── cofree/              Cofree comonad — lazy infinite streams
│   ├── comonad/             Env, Store, Traced comonads
│   ├── cps/                 continuation-passing style
│   ├── currying/            currying and partial application
│   ├── free/                Free monad — interpreter pattern, effect separation (StoreOp functor)
│   ├── kleisli/             Kleisli arrows — composable A → Optional<B>
│   ├── functions/           function utilities
│   │   ├── silentinvoker/   invoke Callable inside lambdas without checked exceptions
│   │   └── decorators/      higher-order decorators: memoize, logging, timing, retry
│   ├── lazy/                Lazy<T> — memoising deferred evaluation
│   ├── monadtransformer/    ReaderT, StateT — stacking effects
│   ├── monads/              Reader, Writer, State monads
│   ├── optics/              Iso, Lens, Prism, AffineTraversal, Traversal
│   ├── parsing/             parser combinators from FP first principles (Optional-based)
│   ├── persistent/          PersistentVector<E> — 32-way trie with structural sharing
│   ├── profunctor/          ProfunctorFn, StrongFn, ChoiceFn (uses adt/Either)
│   ├── recursionschemes/    cata, ana, hylo, para, apo
│   ├── selective/           SelectiveOpt — selective applicative (uses adt/Either)
│   ├── tagless/             Tagless Final — abstract algebras over effect type F
│   ├── trampoline/          stack-safe tail recursion
│   ├── typeclass/           Eq, Show, Semigroup, Monoid
│   ├── typelevel/           HList, type witnesses, type-level programming
│   └── zipper/              ListZipper — focus + context, comonad operations
```

### Canonical Types

- **`adt/Either<L,R>`** — the canonical Either type. Used by `fp/selective/` and `fp/profunctor/`.

---

## Test Coverage

560 tests across 38 test classes, including jqwik property-based tests for:

- `fp/kleisli/` — **KleisliPropertyTest** (monad laws)
- `fp/optics/` — **IsoPropertyTest**, **PrismPropertyTest**
- `fp/persistent/` — **PersistentVectorPropertyTest** (append/set/persistence invariants)
