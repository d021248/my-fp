# my-fp

[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)](https://openjdk.org/projects/jdk/25/)
[![Build](https://img.shields.io/badge/Build-Maven-blue?logo=apachemaven)](https://maven.apache.org/)
[![CI](https://github.com/d021248/my-fp/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/d021248/my-fp/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/d021248/my-fp/branch/main/graph/badge.svg?branch=main)](https://codecov.io/gh/d021248/my-fp)

Functional programming patterns in Java 25: algebraic data types, monads, optics, parser
combinators, and more. Extracted from [my-try](https://github.com/d021248/my-try).

Part of the [`d021248`](https://github.com/d021248) learning suite alongside
[my-try](https://github.com/d021248/my-try) · [my-demos](https://github.com/d021248/my-demos) ·
[my-lisp](https://github.com/d021248/my-lisp) · [my-downloads](https://github.com/d021248/my-downloads).

---

## Topics

| Package | What it demonstrates |
|---|---|
| `adt/` | `Either<L,R>`, sealed hierarchies, `QuantumOp` sum type |
| `fp/applicative/` | `Validated<E,A>` — Applicative with error accumulation |
| `fp/arrows/` | Arrow typeclass — `arr`, `first`, `>>>` composition |
| `fp/bifunctor/` | Bifunctor — `bimap` over both type parameters |
| `fp/church/` | Church encoding — booleans, naturals, lists, Y combinator |
| `fp/cofree/` | Cofree comonad — lazy infinite streams |
| `fp/comonad/` | Env, Store, Traced comonads |
| `fp/cps/` | Continuation-passing style |
| `fp/currying/` | Currying and partial application |
| `fp/free/` | Free monad — interpreter pattern, `StoreOp` functor |
| `fp/functions/` | Higher-order function decorators: memoize, logging, timing, retry |
| `fp/kleisli/` | Kleisli arrows — composable `A → Optional<B>` |
| `fp/lazy/` | `Lazy<T>` — memoising deferred evaluation |
| `fp/monadtransformer/` | `ReaderT`, `StateT` — stacking effects |
| `fp/monads/` | Reader, Writer, State monads |
| `fp/optics/` | Iso, Lens, Prism, AffineTraversal, Traversal |
| `fp/parsing/` | Parser combinators from FP first principles |
| `fp/persistent/` | `PersistentVector<E>` — 32-way trie with structural sharing |
| `fp/profunctor/` | `ProfunctorFn`, `StrongFn`, `ChoiceFn` |
| `fp/recursionschemes/` | cata, ana, hylo, para, apo |
| `fp/selective/` | `SelectiveOpt` — selective applicative |
| `fp/tagless/` | Tagless Final — abstract algebras over effect type `F` |
| `fp/trampoline/` | Stack-safe tail recursion |
| `fp/typeclass/` | Eq, Show, Semigroup, Monoid |
| `fp/typelevel/` | HList, type witnesses, type-level programming |
| `fp/zipper/` | `ListZipper` — focus + context, comonad operations |

---

## Build

```bash
mvn test      # compile + run 560 tests
mvn verify    # tests + JaCoCo coverage check + Spotless format check
```

Requires Java 25 and Maven 3.8+. Uses `--enable-preview`.
Spotless (Google Java Format 1.30) runs automatically on `mvn compile`.
