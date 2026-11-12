# `core/lang/fp/comonad` — Comonads (Store and Env)

Comonads are the categorical dual of monads. Where a monad *injects* a value into a
context (`pure`) and sequences context-threaded computations (`flatMap`), a comonad
*extracts* a value from a context (`extract`) and applies a context-sensitive function
at every position (`extend`).

## Monad vs Comonad at a glance

| | Monad `M<A>` | Comonad `W<A>` |
|---|---|---|
| Create | `pure :: A → M<A>` | — |
| Extract | — | `extract :: W<A> → A` |
| Sequence | `flatMap :: M<A> → (A → M<B>) → M<B>` | — |
| Contextual | — | `extend :: W<A> → (W<A> → B) → W<B>` |

---

## Store Comonad

`Store<S, A>` = `(S → A, S)` — a getter function with a focused position.

```java
var squares = Store.<Integer, Integer>of(n -> n * n, 3);  // focus at index 3

squares.extract()            // 9   (3²)
squares.peek(5)              // 25  (5²) — look at any position
squares.seek(7).extract()    // 49  (7²) — move the focus

// extend: compute a neighbourhood sum at every position
var ns = squares.extend(s ->
    s.peek(s.pos() - 1) + s.extract() + s.peek(s.pos() + 1)
);
ns.extract()                 // 4+9+16 = 29  (neighbours of position 3)
ns.seek(5).extract()         // 16+25+36 = 77
```

### Applications

- Cellular automata (each cell depends on its neighbourhood)
- Image processing (convolution kernels, blur, edge detection)
- Spreadsheet evaluation (cells reference other cells)
- Conway's Game of Life rule

---

## Env Comonad

`Env<E, A>` = `(E, A)` — a value paired with a read-only environment.

`Env` is the dual of the `Reader` monad:

| `Reader<E, A>` | `Env<E, A>` |
|---|---|
| `E → A` (environment injected in) | `(E, A)` (environment carried out) |
| `run(env)` evaluates the function | `extract()` returns the value |
| `ask()` gets env inside computation | `ask()` gets env from the pair |

```java
record Config(int precision) {}

var pi = Env.of(new Config(4), Math.PI);

pi.extract()          // 3.141592653589793
pi.ask().precision()  // 4

// extend: format the value using the config
pi.extend(ctx ->
    ("%." + ctx.ask().precision() + "f").formatted(ctx.extract())
).extract()           // "3.1416"
```

---

## Comonad Laws

```
1. w.extend(w -> w.extract()) == w                             — identity
2. w.extend(f).extract()      == f(w)                          — extract after extend
3. w.extend(f).extend(g)      == w.extend(w -> g(w.extend(f))) — composition
```

---

## Files

| File | Purpose |
|------|---------|
| `Store.java` | Store comonad — focusable lookup |
| `Env.java` | Env comonad — value with environment |
| `ComonadDemo.java` | Demo: squares table, 1-D blur, config-aware formatting |
| `broken/MutableContext.java` | Comparison: mutable arrays and explicit loops |
