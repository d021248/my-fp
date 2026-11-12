# Profunctor

A **profunctor** is a bifunctor that is contravariant in its first type parameter
and covariant in its second.  In plain terms: it wraps a function `A → B` and lets
you transform both the input and the output independently.

---

## What is a profunctor?

Every `Function<A, B>` in Java is, at heart, a profunctor.  You can:

- **Post-process the output** (`map` / `rmap`): given `A → B` and `B → D`, produce `A → D`.
- **Pre-process the input** (`contramap`): given `C → A` and `A → B`, produce `C → B`.
- **Do both at once** (`dimap`): given `C → A`, `A → B`, and `B → D`, produce `C → D`.

The `dimap` operation captures the essence of a profunctor:

```
dimap :: (C → A) → (B → D) → p A B → p C D
```

It lets you adapt a computation at both ends without touching its core logic.

---

## The `dimap` operation

`dimap` generalises both `map` (covariant) and `contramap` (contravariant):

```
contramap f = dimap f identity
rmap     g = dimap identity g
```

This unification is the key insight: profunctors capture the idea that a
computation can be adapted on both sides simultaneously.

---

## Strong profunctors

A **Strong** profunctor adds two operations that let you "focus" a function
on one component of a pair while leaving the other unchanged:

```
first  :: p A B → p (A, C) (B, C)
second :: p A B → p (C, A) (C, B)
```

In `StrongFn`:

```java
var upper = StrongFn.<String, String>of(String::toUpperCase);
upper.first().apply(new StrongFn.Pair<>("hello", 42));
// → Pair("HELLO", 42)
```

The second component (`42`) passes through unchanged.

---

## Why optics ARE profunctor transformers

The profunctor encoding of optics is the insight that every optic is simply
a **function on profunctors**.

### Lens as a Strong profunctor transformer

A `Lens<S, A>` focuses on a field of type `A` inside a structure `S`.
In the profunctor encoding:

```
type Lens S A = forall p. Strong p => p A A → p S S
```

Given any Strong profunctor `p`, a lens takes `p A A` (a computation on `A`)
and produces `p S S` (the "same" computation, but operating inside `S`).

In practice, the lens:

1. Uses `first` to lift `p A A` to work on pairs `(A, context)`.
2. Uses `dimap` to split `S` into `(A, context)` and reassemble.

### Prism as a Choice profunctor transformer

A `Prism<S, A>` focuses on one branch of a sum type.
It requires a **Choice** profunctor (adds `left`/`right` analogous to `first`/`second` for coproducts):

```
type Prism S A = forall p. Choice p => p A A → p S S
```

### Traversal

A `Traversal<S, A>` focuses on multiple `A` values inside `S`.
It requires both `Strong` and `Choice` (plus applicative structure).

---

## Optic composition = function composition

The elegant payoff of the profunctor encoding is that **composing optics is
just composing functions**:

```java
Function<p<A,A>, p<S,S>> lens1 = ...;
Function<p<B,B>, p<A,A>> lens2 = ...;

// Composed lens: p<B,B> → p<S,S>
var composed = lens2.andThen(lens1);
```

No special `andThen` on `Lens`, no manual getter/setter threading — just `Function.andThen`.
Compare this with the manual approach in `broken/VerboseLensComposition.java` where every
composed path requires a hand-written getter and setter pair.

---

## Files

| File | Description |
|------|-------------|
| `ProfunctorFn.java` | `@FunctionalInterface` for `A → B` with `dimap`, `contramap`, `rmap` |
| `StrongFn.java` | Extends `ProfunctorFn` with `first`/`second` for Lens encoding |
| `ProfunctorDemo.java` | Runnable demo of `ProfunctorFn` and `StrongFn` |
| `package-info.java` | Package Javadoc |
| `broken/VerboseLensComposition.java` | Manual getter/setter composition — the pre-profunctor way |
| `broken/package-info.java` | Sub-package Javadoc |

---

## Running the demo

```bash
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.profunctor.ProfunctorDemo
```

Expected output:

```
=== ProfunctorFn: generalized function with dimap ===
len("hello") = 5
dimap(trim, n*2)("  hi  ") = 4
contramap(toLowerCase)("HELLO") = 5
rmap(n->"len="+n)("world") = len=5

=== StrongFn: operates on one half of a pair ===
upper.first().apply(("hello", 42)) = Pair[first=HELLO, second=42]
upper.second().apply((42, "world")) = Pair[first=42, second=WORLD]
```

---

## Further reading

- Milewski, *Category Theory for Programmers*, ch. 22 — Profunctors
- van Laarhoven, *CPS based functional references* — the origin of profunctor optics
- Pickering, Gibbons, Wu, *Profunctor Optics: Modular Data Accessors* (2017)
