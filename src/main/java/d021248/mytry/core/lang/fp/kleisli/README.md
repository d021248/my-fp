# Kleisli Arrows

A **Kleisli arrow** is a function `A → M<B>` — a function that returns a
monadic value rather than a plain value.  In this package `M` is `Optional`,
modelling safe partial functions that may fail silently instead of throwing.

---

## What is a Kleisli arrow?

Ordinary function composition wires `A → B` and `B → C` into `A → C`.
But if your functions return `Optional<B>` and `Optional<C>`, normal
composition breaks down — you cannot feed an `Optional<B>` directly into a
function expecting a plain `B`.

A Kleisli arrow solves this by lifting the composition step into the monad:

```
(A → Optional<B>) >=> (B → Optional<C>)  =  A → Optional<C>
```

The `>=>` operator (pronounced "fish" in Haskell) is called **Kleisli
composition**.  `andThen` in this package implements exactly that.

---

## The `andThen` / `>=>` operator

`Kleisli<A,B>.andThen(Kleisli<B,C>)` returns a `Kleisli<A,C>`:

```java
default <C> Kleisli<A, C> andThen(Kleisli<B, C> other) {
    return a -> run(a).flatMap(other::run);
}
```

That single line is all the wiring you need.  If `run(a)` returns empty,
`flatMap` short-circuits and `other` is never called.  If it succeeds, the
value is piped into `other::run` automatically.

---

## Why monad laws = category laws

Kleisli arrows under `>=>` form a **category**:

| Law | Kleisli form |
|---|---|
| Left identity | `identity() >=> f` = `f` |
| Right identity | `f >=> identity()` = `f` |
| Associativity | `(f >=> g) >=> h` = `f >=> (g >=> h)` |

These three laws are **exactly** the monad laws written in Kleisli form.
The identity arrow corresponds to `Optional.of` (the monadic `return`), and
`andThen` corresponds to `flatMap` (the monadic `bind`).

This is the essence of the category-theory insight: **a monad is precisely a
monoid in the category of Kleisli arrows**.  The "multiplication" of the monoid
is `>=>`, and the identity element is `a → M(a)`.

---

## `andThen` is `Optional.flatMap`

For the `Optional` monad, `andThen` is equivalent to chaining `flatMap`:

```java
// With Kleisli andThen:
var pipeline = safeSqrt.andThen(safeLog).andThen(safeReciprocal);
pipeline.run(x);

// Equivalent flatMap chain:
safeSqrt.run(x)
    .flatMap(safeLog::run)
    .flatMap(safeReciprocal::run);
```

The Kleisli version wins when you need a **reusable, named pipeline** that can
be passed around, stored as a field, or composed further.  The `flatMap` chain
is anonymous and cannot be composed after the fact.

More generally, for any monad `M`, `andThen` is `M.flatMap`.  Switching from
`Optional` to `CompletableFuture`, `List`, or a custom monad only requires
changing the concrete implementation of `Kleisli<A,B>` — the composition
operator stays the same.

---

## The safe-math example

Three partial functions that each may fail:

| Arrow | Fails when |
|---|---|
| `safeSqrt` | input < 0 |
| `safeReciprocal` | input == 0 |
| `safeLog` | input <= 0 |

Composed with `andThen`:

```java
var pipeline = safeSqrt.andThen(safeLog).andThen(safeReciprocal);

pipeline.run(Math.E * Math.E);  // Optional[1.0]  (sqrt→e, log→1, 1/1→1)
pipeline.run(1.0);              // Optional.empty  (sqrt→1, log→0, 1/0→∅)
pipeline.run(-1.0);             // Optional.empty  (sqrt fails immediately)
```

The failure short-circuits at the first empty result — no exception, no
null, no explicit check.

---

## Comparison: Kleisli vs manual flatMap chaining

### With Kleisli arrows

```java
var pipeline = safeSqrt.andThen(safeLog).andThen(safeReciprocal);
var result = pipeline.run(x);
```

The pipeline is a **first-class value**: stored, passed, extended.

### Without Kleisli arrows (see `broken/ManualNullChecks`)

```java
static Optional<Double> sqrtThenReciprocal(double x) {
    var r1 = safeSqrt(x);
    if (r1.isEmpty()) return Optional.empty();
    return safeReciprocal(r1.get());
}
```

Every combination requires a new method with repetitive `isEmpty` guards.
Adding a third step means adding another method.  There is no reusable
abstraction for the composition pattern itself.

---

## Files

| File | Description |
|---|---|
| `Kleisli.java` | `@FunctionalInterface` — the Kleisli arrow with `andThen`, `compose`, `map`, `lift`, `pure` |
| `KleisliDemo.java` | Runnable demo: safe-math pipeline and monad-law checks |
| `broken/ManualNullChecks.java` | Anti-pattern: ad-hoc composition with explicit `isEmpty` guards |
| `broken/package-info.java` | Package Javadoc for the `broken` sub-package |
| `package-info.java` | Package Javadoc for this package |

---

## How to run the demo

```bash
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.kleisli.KleisliDemo
```

Expected output:

```
=== Kleisli: composable partial functions ===

sqrt >=> reciprocal:
  4.0  -> Optional[0.5]
  0.0  -> Optional.empty
  -1.0 -> Optional.empty

sqrt >=> log >=> reciprocal:
  Math.E^2 -> Optional[1.0]
  1.0      -> Optional.empty

=== Kleisli category laws ===
Left identity same result: true
Right identity same result: true
Associativity same result: true
```
