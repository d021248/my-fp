# `core/lang/fp/zipper` — Functional Zipper

A **zipper** (Huet 1997) is a data structure that provides a "cursor" into another data
structure, making it possible to navigate and locally update an immutable value in O(1).
Formally, a zipper is the *derivative* of its host data type — the same sense in which
taking the derivative of a polynomial gives you a related polynomial with one term removed.

---

## What is a zipper?

Gérard Huet introduced zippers in 1997 as a way to represent the "current position"
inside an immutable structure.  The key insight is to decompose the structure into:

1. the **focus** — the element currently selected, and
2. the **context** — everything else, arranged so that navigation costs O(1).

For a list this context is simply the elements to the left (stored in reverse order so
that adding/removing the nearest element is a head operation) and the elements to the
right (in normal order).

---

## The `(left, focus, right)` structure

```
Full list  : [A, B, F, C, D]   (F is the focused element)

ListZipper : left  = [B, A]    ← nearest-first (reversed)
             focus = F
             right = [C, D]    ← normal order
```

**Nearest-first invariant for `left`**: `left.get(0)` is always the element immediately
to the left of the focus.  This is what makes O(1) navigation possible — every move is
just a `head` read and a `cons`.

---

## Navigation operations

| Method | What it does | Cost |
|---|---|---|
| `moveLeft()` | Pop `left.head` → new focus; push old focus onto `right` | O(1) |
| `moveRight()` | Pop `right.head` → new focus; push old focus onto `left` | O(1) |
| `replace(v)` | Swap the focus for `v` | O(1) |
| `modify(f)` | Apply `f` to the focus | O(1) |
| `insertRight(v)` | Push `v` onto `right` | O(1) |
| `delete()` | Remove focus; move right (or left if at end) | O(1) |
| `toList()` | Reverse `left`, concatenate `[focus]`, append `right` | O(n) |

---

## Connection to the Store comonad

`ListZipper<A>` is isomorphic to `Store<Integer, A>` — the Store comonad with an integer
position:

| `ListZipper<A>` | `Store<Integer, A>` |
|---|---|
| `extract()` | `store.extract()` — focused value |
| `extend(f)` | `store.extend(f)` — apply `f` at every position |
| `moveLeft()` | `store.seek(pos - 1)` |
| `moveRight()` | `store.seek(pos + 1)` |

The `extend` method visits every position in the list, applies `f` to the zipper focused
at that position (giving `f` access to both neighbours), and assembles a new zipper with
the results.  This is the same computation as Conway's Game of Life or a 1-D blur kernel
expressed through a Store comonad.

---

## Connection to Lens

The focus is a lawful `Lens<ListZipper<A>, A>`:

```java
Lens<ListZipper<A>, A> focusLens = Lens.of(
    ListZipper::focus,           // get
    (z, v) -> z.replace(v)       // set
);
```

This means the zipper is itself a composable optic: you can zoom into the focus of a
zipper that lives inside a larger record using ordinary lens composition.

---

## Comonad laws

`ListZipper` satisfies all three comonad laws:

```
1. z.extend(ListZipper::extract) == z           — extend with extract is identity
2. z.extend(f).extract()         == f(z)         — extract after extend applies f
3. z.extend(f).extend(g) ==
       z.extend(z -> g(z.extend(f)))             — associativity of extend
```

---

## Files

| File | Purpose |
|---|---|
| `ListZipper.java` | Immutable list zipper with comonad and functor operations |
| `ZipperDemo.java` | Navigation, modification, insertion, deletion, and `extend` demo |
| `broken/MutableListEditor.java` | Comparison: mutable list with integer cursor |
| `broken/package-info.java` | Package description for the broken sub-package |
| `package-info.java` | Package description and cross-references |
| `README.md` | This file |

---

## Running the demo

```bash
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.zipper.ZipperDemo
```

Expected output:

```
=== ListZipper: functional list editing ===
Initial focus      : one
After 2 right moves: three
After uppercase    : [one, two, THREE, four, five]
After insertRight  : [one, two, three, inserted, four, five]
After delete       : [one, two, four, five]
Extended (indexed) : [pos=0:one, pos=1:two, pos=2:three, pos=3:four, pos=4:five]
```

---

## Further reading

- Huet, G. (1997). *The Zipper.* JFP 7(5):549–554.
- McBride, C. (2001). *The Derivative of a Regular Type is its Type of One-Hole Contexts.*
- Kmett, E. [lens](https://hackage.haskell.org/package/lens) — Haskell lens library
  treating zippers as representable Store comonads.
