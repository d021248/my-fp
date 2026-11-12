# `typelevel` — Type-Level Programming

Encode invariants as types so the compiler catches violations at compile time,
not at runtime.

## The core idea

Some constraints — "index is within bounds", "list has at least N elements" —
are traditionally checked at runtime with `IndexOutOfBoundsException`. Type-level
programming encodes these constraints in the type signature so a wrong usage
simply does not compile.

## Peano naturals — `Nat`

```
Zero       ≡ 0
Succ<Zero> ≡ 1
Succ<Succ<Zero>> ≡ 2
…
```

`Zero` and `Succ<N>` are sealed types that exist only at the type level. No
arithmetic happens at runtime; the values are never instantiated.

## Heterogeneous list — `HList`

An `HList` is a tuple encoded as a linked list whose type captures the type of
every element:

```java
// HCons<String, HCons<Integer, HCons<Boolean, HNil>>>
var list = new HCons<>("hello", new HCons<>(42, new HCons<>(true, new HNil())));
```

Unlike `Object[]`, an `HList` preserves the exact type of each element without
casts.

## Compile-time-bounded access — `Get<L, N, A>`

`Get<L, N, A>` is a typed accessor: "in list `L`, at index `N`, you find an
`A`". The compiler verifies the index at compile time:

```java
// index 0 — always valid; type is the head type
Get<HCons<String, HCons<Integer, HNil>>, Nat.Zero, String> g0 = Get.zeroIdx();

// index 1 — compiler verifies the list has at least 2 elements
Get<HCons<String, HCons<Integer, HNil>>, Nat.Succ<Nat.Zero>, Integer> g1 =
    Get.succIdx(Get.zeroIdx());

// compile error — index 2 does not exist in a 2-element list
// Get.succIdx(Get.succIdx(Get.succIdx(Get.zeroIdx()))) // won't compile
```

## Why this matters

Bounds-checked access via `get(i)` throws at runtime. `Get` fails at compile
time, eliminating an entire class of bugs. The technique also demonstrates
*phantom types* — types used only for compiler checking, not carried at runtime.

## Relationship to other packages

- `lens` — a `Lens<S, A>` is conceptually similar: a compile-time-checked
  focused accessor, but over record fields rather than list indices

## Running the demo

```bash
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.typelevel.TypeLevelDemo
```

## Test coverage

`TypeLevelTest` — verifies `HList` construction, `Get.zeroIdx`, and chained
`Get.succIdx` for indices 0, 1, and 2.
