# `core/lang/fp` — Functional Programming Patterns

Functional programming techniques implemented in Java 25. Each sub-package is
self-contained: the core abstraction, an optional `broken/` sub-package
showing the problem it solves, and a `*Demo` class to run.

## Packages

| Package | Pattern | Key class | Demo |
|---|---|---|---|
| `trampoline` | Stack-safe tail recursion | `Bounce` | `TrampolineDemo` |
| `currying` | Currying & partial application | `Curry` | `CurryingDemo` |
| `cps` | Continuation-Passing Style | `Cont` | `CpsDemo` |
| `lazy` | Memoising deferred evaluation | `Lazy<T>` | `LazyDemo` |
| `optics` | Iso/Lens/Prism/AffineTraversal/Traversal hierarchy | `Iso`, `Lens`, `Prism` | `OpticsDemo` |
| `typeclass` | Type classes (`Eq`, `Show`) | `Eq`, `Show` | `TypeClassDemo` |
| `typelevel` | Type-level Peano naturals & HList | `Nat`, `HList`, `Get` | `TypeLevelDemo` |
| `lambda` | Lambda tooling — unchecked-exception bridging + function decorators | `CallUnchecked`, `FunctionalDecorators` | `SilentInvokerDemo` |
| `church` | Church encodings & Y combinator | `Church` | `ChurchDemo` |
| `free` | Free monad — programs as data | `Free` | `FreeDemo` |
| `persistent` | Persistent vector (32-way trie) | `PersistentVector` | `PersistentVectorDemo` |
| `state` | Reader, Writer & State monads | `Reader`, `Writer`, `State` | `StateDemo` |
| `recursionschemes` | Cata-, ana-, hylomorphism | `RecursionSchemes` | `RecursionSchemesDemo` |
| `comonad` | Store & Env comonads | `Store`, `Env` | `ComonadDemo` |
| `applicative` | Applicative functor; error accumulation | `Validated` | `ValidatedDemo` |
| `cofree` | Cofree comonad as lazy stream | `CofreeStream` | `CofreeDemo` |
| `zipper` | Functional zipper with focus | `ListZipper` | `ZipperDemo` |
| `profunctor` | Profunctors & optics connection | `ProfunctorFn`, `StrongFn` | `ProfunctorDemo` |
| `kleisli` | Kleisli arrows; monad laws as category | `Kleisli` | `KleisliDemo` |
| `monadtransformer` | Monad transformer stack | `ReaderT`, `StateT` | `MonadTransformerDemo` |
| `selective` | Selective applicative | `SelectiveOpt` | `SelectiveDemo` |
| `bifunctor` | Functor over two type parameters | `Bifunctor`, `Pair`, `Either` | `BifunctorDemo` |
| `parsing` | Applicative parser combinators | `Parser`, `Parsers` | `ParserDemo` |
| `tagless` | Tagless-final DSL | `Expr`, `EvalInterpreter`, `PrettyPrinter` | `TaglessDemo` |
| `arrows` | Generalised function composition | `Arrow` | `ArrowDemo` |

`lambda` has two sub-packages: `silentinvoker` (unchecked-exception bridging) and
`decorators` (memoize / logging wrappers over `Function<A,B>`).

## Pattern relationships

```
lambda/decorators   decorators over Function<A,B> — memoize, logging
  └─ lazy           Lazy<T> is a special case: memoize applied to Supplier<T>

trampoline          replaces recursion with heap-allocated continuations
  └─ cps            CPS makes continuations explicit; trampoline executes them
    └─ church       Y combinator makes anonymous recursion possible without trampoline

currying            transforms f(a,b,c) → f(a)(b)(c) for partial application
  └─ lambda         partially-applied functions compose with the decorators' memoize/logging

optics              Iso → Lens → Prism → AffineTraversal → Traversal — one hierarchy
  └─ typeclass      Eq/Show are the simplest type classes; a Lens is a more advanced one
  └─ profunctor     every optic is a profunctor transformer: Lens = Strong, Prism = Choice
  └─ zipper         ListZipper is isomorphic to Store<Int,A> and its focus is a Lens

free                programs as data; swap interpreters without changing the program
  └─ persistent     PersistentVector stores immutable state used by the store interpreter

applicative         Applicative fills the gap between Functor (map) and Monad (flatMap)
  └─ state          Reader/Writer/State are applicative (and monadic)

state               Reader/Writer/State monads — three faces of the same monad pattern
  └─ kleisli        Kleisli arrows compose monadic functions; monad laws = category laws
  └─ comonad        comonads are the categorical dual of monads (extract ↔ pure, extend ↔ flatMap)
    └─ cofree       Cofree comonad = value at every node; CofreeStream = Cofree over Identity

recursionschemes    cata/ana/hylo separate traversal logic from per-node algebra
  └─ trampoline     both address stack safety — trampoline at runtime, cata at design time

monadtransformer    stacks Reader/State effects so both can be used in the same computation
  └─ state          ReaderT<E,A> = E → Optional<A>; StateT<S,A> = S → Optional<Pair<A,S>>

parsing             Parser<A> is an applicative functor; combinators assemble grammars
  └─ applicative    `or`/`many` are applicative sequencing; `flatMap` makes it context-sensitive

tagless             parametrize over the carrier type F to defer interpretation
  └─ typeclass      Expr<F> is a type class instance; swap F to change behaviour
  └─ free           both defer interpretation — tagless uses functions, free uses data

bifunctor           bimap(f, g) maps both type parameters of a container independently
  └─ lambda         Pair.mapFirst is contramap on fst; Either.mapRight is plain fmap

arrows              Arrow<A,B> generalises Function<A,B> with fanout and parallel composition
  └─ kleisli        Kleisli arr a b = a → M b; Arrow is strictly more general

selective           select(Either<A,B>, f:A→B) short-circuits when the result is already known
  └─ applicative    every Monad has a Selective instance; Applicative alone does not
```

## Running a demo

```bash
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.lambda.decorators.FunctionalDecoratorsDemo
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.trampoline.TrampolineDemo
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.lazy.LazyDemo
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.optics.OpticsDemo
# etc.
```

## Test coverage

All packages with a reusable library class have a corresponding `*Test` in
`src/test/…/fp/<package>/`.

Packages with test coverage: `lambda/decorators`, `trampoline`, `currying`, `cps`, `lazy`,
`optics`, `typeclass`, `typelevel`, `lambda/silentinvoker`, `church`, `free`, `persistent`,
`state`, `recursionschemes`, `comonad`, `applicative`, `cofree`, `zipper`, `profunctor`,
`kleisli`, `monadtransformer`, `selective`, `bifunctor`, `parsing`, `tagless`, `arrows`.
