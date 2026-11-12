/// Functional programming patterns and techniques implemented in Java 25.
///
/// Each sub-package is self-contained: a pattern, its motivation (`broken/`), and a demo.
///
/// | Package | Pattern |
/// |---|---|
/// | `trampoline` | Trampoline — stack-safe tail recursion |
/// | `currying` | Currying and partial application |
/// | `cps` | Continuation-Passing Style |
/// | `lazy` | `Lazy<T>` — memoising deferred evaluation |
/// | `optics` | Optics — `Iso`, `Lens`, `Prism`, `AffineTraversal`, `Traversal` in one hierarchy |
/// | `typeclass` | Type classes — `Eq`, `Show` (Haskell-inspired) |
/// | `typelevel` | Type-level programming — `Nat`, `HList`, `Get` |
/// | `lambda` | Lambda tooling — exception bridging + function decorators |
/// | `church` | Church encodings — booleans, numerals, pairs, Y combinator |
/// | `free` | Free monad — programs as data, multiple interpreters |
/// | `persistent` | `PersistentVector<E>` — 32-way trie with structural sharing |
/// | `state` | Reader, Writer, and State monads — functional effect handling |
/// | `recursionschemes` | Catamorphism, anamorphism, hylomorphism — structural recursion |
/// | `comonad` | Store and Env comonads — the categorical dual of monads |
/// | `applicative` | `Validated<E,A>` — Applicative functor with error accumulation |
/// | `cofree` | `CofreeStream<A>` — Cofree comonad as a lazy infinite stream |
/// | `zipper` | `ListZipper<A>` — Functional zipper with focus and comonad operations |
/// | `profunctor` | `ProfunctorFn`, `StrongFn` — Profunctors and the optics connection |
/// | `kleisli` | `Kleisli<A,B>` — Kleisli arrows; monad laws as category laws |
/// | `monadtransformer` | `ReaderT<E,A>`, `StateT<S,A>` — stack two effects without losing either |
/// | `selective` | `SelectiveOpt<A>` — selective applicative bridges Applicative and Monad |
/// | `bifunctor` | `Bifunctor<A,B>` — functor over both type parameters (`Pair`, `Either`) |
/// | `parsing` | `Parser<A>`, `Parsers` — applicative parser combinators |
/// | `tagless` | `Expr<F>` — tagless-final DSL; swap interpreters by changing the carrier type |
/// | `arrows` | `Arrow<A,B>` — generalised functions with sequential composition and fanout |
///
/// ## Moved packages
///
/// - `coroutines` → `core/lang/concurrency/coroutines/` (Java concurrency, not FP)
/// - `ghostcontext` → `core/lang/flexibleconstructor/ghostcontext/` (JEP 492 demo, not FP)
///
/// @since 1.0
package d021248.mytry.core.lang.fp;
