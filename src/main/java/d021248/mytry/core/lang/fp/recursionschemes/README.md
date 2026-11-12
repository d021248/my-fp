# `core/lang/fp/recursionschemes` — Recursion Schemes

Recursion schemes capture canonical patterns of structural recursion as
reusable higher-order functions. The per-step logic (the algebra or coalgebra)
is decoupled from the traversal.

## The Problem

Every operation on a recursive tree requires its own traversal:

```java
int eval(Expr e)         { return switch(e) { case Add a -> eval(a.left()) + eval(a.right()); ... }; }
String print(Expr e)     { return switch(e) { case Add a -> print(a.left()) + "+" + print(a.right()); ... }; }
int depth(Expr e)        { return switch(e) { case Add a -> 1 + max(depth(a.left()), depth(a.right())); ... }; }
```

The traversal structure (visit children, combine) is identical across all three
— only the per-node logic differs. Recursion schemes capture the traversal once.

---

## Key Concepts

### Base Functor

Replace recursive self-references with a type parameter `A`:

```
Expr        = Num(int) | Add(Expr, Expr)  | Mul(Expr, Expr)   ← recursive
ExprF<A>    = Num(int) | Add(A, A)        | Mul(A, A)          ← non-recursive
```

`A` marks "the recursive child positions". Provide `fmap :: (A → B) → ExprF<A> → ExprF<B>`
to make `ExprF` a functor.

### Fixed Point

`Fix(out: ExprF<Fix>)` ties the knot: `Fix ≈ ExprF<ExprF<ExprF<…>>>`.
All recursive trees are `Fix` values.

### Catamorphism (fold)

`cata(algebra, tree)` — bottom-up fold. The algebra handles one layer with
already-evaluated children; `cata` handles the traversal.

```java
// Evaluate: algebra gets Integer children — no explicit recursion inside
int result = cata(e -> switch (e) {
    case ExprF.Num<Integer> n -> n.value();
    case ExprF.Add<Integer> a -> a.left() + a.right();   // children already evaluated
    case ExprF.Mul<Integer> m -> m.left() * m.right();
}, expr);
```

### Anamorphism (unfold)

`ana(coalgebra, seed)` — top-down unfold. The coalgebra produces one layer
from a seed; `ana` unfolds recursively until it reaches a leaf.

```java
// Unfold 4 into the sum tree  4 + (3 + (2 + 1))
Fix sum4 = ana(n -> n <= 1 ? new ExprF.Num<>(1) : new ExprF.Add<>(n, n - 1), 4);
```

### Hylomorphism (unfold then fold)

`hylo(algebra, coalgebra, seed)` — equivalent to `cata(algebra, ana(coalgebra, seed))`
but without materialising the intermediate `Fix` tree.

```java
// Factorial 5! via hylo
int fact = hylo(evalAlgebra, n -> n <= 1 ? new Num<>(1) : new Mul<>(n, n - 1), 5); // 120
```

---

## Pattern: How a catamorphism works

```
Fix(Add(Fix(Mul(Fix(Num 2), Fix(Num 3))), Fix(Num 4)))

Step 1 — fmap recurses to the leaves:
  Fix(Num 2) -> cata(alg, Fix(Num 2)) -> alg(Num 2) -> 2
  Fix(Num 3) -> cata(alg, Fix(Num 3)) -> alg(Num 3) -> 3
  Fix(Num 4) -> cata(alg, Fix(Num 4)) -> alg(Num 4) -> 4

Step 2 — fmap replaces Fix children with their values:
  ExprF.Add<Fix> becomes ExprF.Add<Integer>(2*3, 4) = Add(6, 4)

Step 3 — algebra handles the layer:
  alg(Add(6, 4)) -> 10
```

---

## Files

| File | Purpose |
|------|---------|
| `RecursionSchemes.java` | `ExprF<A>`, `Fix`, `fmap`, `cata`, `ana`, `hylo` |
| `RecursionSchemesDemo.java` | Demo: eval, pretty-print, depth, factorial via hylo |
| `broken/ExplicitRecursion.java` | Comparison: explicit recursion repeated per operation |

## Further Reading

- Meijer, Fokkinga, Paterson: *Functional Programming with Bananas, Lenses, Envelopes and Barbed Wire* (1991)
- The `Bounce` (trampoline) and `Cont` (CPS) packages show complementary
  approaches to structuring recursion
