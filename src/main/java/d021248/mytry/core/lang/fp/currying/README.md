# Currying and partial application in Java

## The concept

In **Haskell** every function is automatically curried — `f a b c` is sugar for
`((f a) b) c`. Partial application costs nothing:

```haskell
clamp :: Int -> Int -> Int -> Int
clamp lo hi x = max lo (min hi x)

clip = clamp 0 100        -- partially applied, free
clip 150  -- → 100
```

In **Scala** named and default parameters cover the simpler case. In
**Python / JavaScript / Kotlin** libraries like `functools.partial` or
`Function.partial()` are idiomatic.

## The Java problem

Java has none of that. The default pattern for optional parameters is to add
one overload per combination:

```java
String repeat(String text, int times, String sep) { ... }
String repeat(String text, int times)             { return repeat(text, times, ""); }
String repeat(String text)                        { return repeat(text, 1, ""); }
// 3 parameters → up to 7 overloads. 4 parameters → up to 15.
```

The business logic is duplicated (or hidden) across all overloads. Adding a
fourth parameter requires touching every call site.  See
[`broken/OverloadExplosionDemo.java`](broken/OverloadExplosionDemo.java).

## The fix: Fn2 / Fn3 with partial()

Represent the function as a value; derive partial views on demand:

```java
Fn3<Integer, Integer, Integer, Integer> clamp =
    fn3((lo, hi, x) -> Math.max(lo, Math.min(hi, x)));

Function<Integer, Integer> clip = clamp.partial(0, 100);   // fix lo=0, hi=100
clip.apply(-5)   // → 0
clip.apply(50)   // → 50
clip.apply(150)  // → 100
```

The logic is written **once**. Any combination of fixed / free arguments is
expressed by calling `.partial(...)` or `.curried()`.

See [`Curry.java`](Curry.java) and
[`CurryingDemo.java`](CurryingDemo.java).

## API surface

### `Fn2<A, B, R>`

| Method | Signature | Description |
|--------|-----------|-------------|
| `apply` | `(A, B) → R` | Full application |
| `partial` | `A → (B → R)` | Fix first argument |
| `curried` | `() → A → B → R` | Curried form |
| `flip` | `() → Fn2<B,A,R>` | Swap argument order |

### `Fn3<A, B, C, R>`

| Method | Signature | Description |
|--------|-----------|-------------|
| `apply` | `(A, B, C) → R` | Full application |
| `partial(a)` | `A → Fn2<B,C,R>` | Fix first argument |
| `partial(a, b)` | `(A,B) → C → R` | Fix first two arguments |
| `curried` | `() → A → B → C → R` | Fully curried form |

## Comparison

| Approach              | Logic duplication | Partial application | Verbosity |
|-----------------------|:-----------------:|:-------------------:|:---------:|
| Overload explosion    | High              | N/A                 | High      |
| `Fn2` / `Fn3`         | None              | Built-in            | Medium    |
| Haskell / F#          | None              | Free                | Low       |
| Python `partial`      | None              | Library             | Low       |

## Files

| File | Purpose |
|------|---------|
| [`broken/OverloadExplosionDemo.java`](broken/OverloadExplosionDemo.java) | 4 overloads per function, clamp via lambda |
| [`Curry.java`](Curry.java) | `Fn2` / `Fn3` with `partial`, `curried`, `flip` |
| [`CurryingDemo.java`](CurryingDemo.java) | clamp, repeat, table formatting, pipeline |
