# `persistent` — Persistent Vector

An immutable, persistently shared vector backed by a 32-way trie.

## The core idea

All "mutating" operations (`append`, `set`, `update`) return a **new** vector
that shares almost all internal tree nodes with the original — no full copy.
This is *structural sharing*.

```java
var v1 = PersistentVector.of(1, 2, 3);
var v2 = v1.append(4);       // new root path copied; v1 unchanged
var v3 = v2.set(1, 99);      // one root-to-leaf path copied; v2 unchanged
```

## Data structure

The trie has branching factor 32 (`BRANCH = 32`, `BITS = 5`).
An N-element vector has depth `⌈log₃₂ N⌉` — at most 7 levels for 2³⁵
elements. Operations follow one path from root to leaf.

```
depth=1  root → [e₀ … e₃₁]
depth=2  root → [node₀ … node₃₁] → [e₀ … e₃₁]
```

## Complexity

| Operation | Complexity |
|---|---|
| `get(i)` | O(log₃₂ N) ≈ O(1) |
| `append(e)` | O(log₃₂ N) ≈ O(1) |
| `set(i, e)` | O(log₃₂ N) ≈ O(1) |
| `size()` | O(1) |
| `stream()` | O(N) |

*O(log₃₂ N) is at most 7 for any feasible collection — effectively constant.*

## Limitation

This implementation omits the *tail buffer* optimisation that Clojure adds.
Sequential `append` therefore copies one full root-to-leaf path per element
rather than amortising the cost. Use `PersistentVector.of(...)` for bulk
construction.

## Why not just copy the array?

For an N-element vector, a copy is O(N). Structural sharing is O(log₃₂ N) —
about 7 node copies for any feasible collection. The old and new versions
coexist cheaply, making event-sourcing, undo history, and concurrent reads
trivial to implement.

## Relationship to other packages

- `free` — `PersistentVector` is used as the backing store in the `Free`
  package's `PersistentStoreInterpreter` demo

## Running the demo

```bash
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.persistent.PersistentVectorDemo
```

## Test coverage

`PersistentVectorTest` — verifies structural sharing, get/set/append, bounds
checking, and stream iteration.
