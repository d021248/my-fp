# Cofree Comonad — `CofreeStream`

## What is `Cofree`?

The **Cofree comonad** is the categorical dual of the **Free monad**.
Where the Free monad builds a tree of *requests* to be interpreted,
the Cofree comonad builds a tree of *values*, each annotating a node
in a lazily unfolded structure.

Formally:

```
Cofree F A  =  (A,  F (Cofree F A))
```

A `Cofree F A` value is a pair of:
- an annotation `A` at the current node, and
- an `F`-shaped structure of lazy sub-trees, each of which is itself a `Cofree F A`.

The functor `F` determines the *branching shape* of the tree.

---

## Duality Table: Free vs Cofree

| Concept          | Free monad                         | Cofree comonad                       |
|------------------|------------------------------------|--------------------------------------|
| Category         | initial algebra of `F`             | terminal co-algebra of `F`           |
| Structure        | tree of *requests*                 | tree of *values*                     |
| Injection        | `pure` — lift a value              | `extract` — get the current value    |
| Sequencing       | `flatMap` — sequence effects       | `extend` — enrich with context       |
| Folding          | `fold` / interpreter               | `unfold` / co-iterator               |
| Use cases        | programs as data, effect DSLs      | streams, cellular automata, zippers  |

---

## `CofreeStream` = `Cofree Identity`

When `F` is the identity functor (i.e., `Supplier<_>`), each node has exactly
**one lazy successor**, giving:

```
CofreeStream A  =  (A,  Supplier<CofreeStream A>)
```

This is a lazy, potentially **infinite** stream.  Evaluation is deferred: the
tail is not computed until it is requested.

Contrast this with `java.util.stream.Stream`, which is also lazy but is
single-pass and cannot be rewound.  A `CofreeStream` is a *persistent* lazy
structure — you can traverse the same prefix many times.

---

## The `extend` Operation

`extend` is the heart of the comonad.  Its type is:

```java
<B> CofreeStream<B> extend(Function<CofreeStream<A>, B> f)
```

`f` receives the **entire stream starting at each position**, not just the
element at that position.  This lets `f` look ahead (or behind, if context is
threaded) to compute a context-sensitive result.

### Cellular Automaton Analogy

Conway's Game of Life uses exactly this pattern on a 2D grid.  For a 1D
automaton:

```java
// Rule: new value = average of left neighbour + self + right neighbour
var smoothed = signal.extend(ctx ->
    ctx.tail().get().head()   // next element
    + ctx.head()              // current element
);
```

The `extend` replaces every cell simultaneously, producing a new stream where
each element depends on its local neighbourhood in the original stream.

---

## Comonad Laws

Every comonad must satisfy three laws (verified by the tests in `CofreeStreamTest`):

1. **Identity**: `w.extend(ctx -> ctx.extract()) == w`
   — extending with the trivial "just extract" function is a no-op.

2. **Extract after extend**: `w.extend(f).extract() == f(w)`
   — reading the head of the extended stream gives the same result as
   applying `f` directly to the original stream.

3. **Composition** (not directly tested, follows from the above):
   `w.extend(f).extend(g) == w.extend(w -> g(w.extend(f)))`

---

## How Cofree Generalises

By swapping the functor `F`, you get completely different structures:

| Functor `F`         | `Cofree F A` is…                          |
|---------------------|-------------------------------------------|
| `Supplier<_>` (id)  | `CofreeStream` — a lazy infinite stream   |
| `List<_>`           | a *rose tree* — each node has many children |
| `Optional<_>`       | a *non-empty list* — at least one element |
| `Map<K, _>`         | a *trie* — keys at every branching level  |

This package demonstrates only the identity-functor case (`CofreeStream`).

---

## Files

| File | Description |
|------|-------------|
| `CofreeStream.java` | `Cofree` over `Supplier` — lazy infinite stream with `iterate`, `unfold`, `map`, `extend`, `take`, `drop` |
| `CofreeDemo.java` | Runnable demo: naturals, Fibonacci, running pair-sum via `extend` |
| `broken/EagerStream.java` | Eager `ArrayList`-based stream — shows what `CofreeStream` avoids |
| `broken/package-info.java` | Package documentation for the comparison sub-package |
| `package-info.java` | This package's Javadoc |

---

## How to Run the Demo

```bash
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.cofree.CofreeDemo
```

Expected output:

```
=== CofreeStream: Cofree over the identity functor ===

First 10 naturals: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
First 10 fibs:     [0, 1, 1, 2, 3, 5, 8, 13, 21, 34]

Running pair-sums (n + n+1) for first 5:
[1, 3, 5, 7, 9]

First 5 naturals doubled: [0, 2, 4, 6, 8]
After dropping 5, next head: 5
```

---

## See Also

- `core/lang/fp/free/` — the Free monad (dual of this package)
- `core/lang/fp/comonad/` — Store and Env comonads (non-Cofree comonad instances)
- JEP 473 — Stream Gatherers (another lazy-stream mechanism in Java 25)
