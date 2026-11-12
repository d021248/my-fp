# Trampolining — TCO without a JVM

## The concept

Functional languages with **tail-call optimisation** (TCO) — Haskell, Scala (for
direct self-recursion), Erlang, F#, Kotlin (`tailrec`) — guarantee that a
tail-recursive function uses O(1) stack space regardless of input size:

```haskell
factorial :: Integer -> Integer -> Integer
factorial 0 acc = acc
factorial n acc = factorial (n-1) (n * acc)   -- no stack frame kept
```

The compiler rewrites the tail call into a loop. The function can recurse to
any depth.

## The Java problem

The JVM does **not** perform TCO. Every call pushes a frame. A tail-recursive
`factorial(100_000, 1)` throws `StackOverflowError` at ~10,000 frames (JVM
default: 512 KB stack).

See [`broken/RecursiveDemo.java`](broken/RecursiveDemo.java) — `factorial` and
`fibonacci` that crash on large inputs.

## The fix: Bounce<T>

A **trampoline** replaces direct recursion with a _description_ of the next
step. The driver loop executes steps in a `while` loop — always the same stack
frame:

```java
sealed interface Bounce<T> permits Done, More {
    record Done<T>(T value)              implements Bounce<T> {}
    record More<T>(Supplier<Bounce<T>> next) implements Bounce<T> {}

    static <T> T run(Bounce<T> bounce) {
        while (bounce instanceof More<T> m) bounce = m.next().get();
        return ((Done<T>) bounce).value();
    }
}
```

A trampolined function never calls itself — it returns `Bounce.more(...)`:

```java
Bounce<Long> factStep(int n, long acc) {
    if (n <= 1) return Bounce.done(acc);
    return Bounce.more(() -> factStep(n - 1, (long) n * acc));  // no call!
}

long result = Bounce.run(factStep(100_000, 1L));   // O(1) stack
```

See [`Bounce.java`](Bounce.java) and
[`TrampolineDemo.java`](TrampolineDemo.java).

## Mutual recursion

Without trampolining, mutual recursion (`isEven` ↔ `isOdd`) also overflows.
With `Bounce<T>` each step returns a thunk, so both functions share the same
trampoline loop:

```java
Bounce<Boolean> isEvenStep(int n) {
    if (n == 0) return Bounce.done(true);
    return Bounce.more(() -> isOddStep(n - 1));
}
Bounce<Boolean> isOddStep(int n) {
    if (n == 0) return Bounce.done(false);
    return Bounce.more(() -> isEvenStep(n - 1));
}
boolean isEven(100_000)   // works fine
```

## How the trampoline runs

```
Bounce.run(factStep(5, 1)):
  step 1: More(() -> factStep(4, 5))
  step 2: More(() -> factStep(3, 20))
  step 3: More(() -> factStep(2, 60))
  step 4: More(() -> factStep(1, 120))
  step 5: Done(120)  → return 120
All on the same stack frame.
```

## Cost

Each step allocates one `More` record (one lambda).  That is much cheaper than
a JVM stack frame (~hundreds of bytes), and the previous `More` is immediately
GC-eligible.

## Comparison

| Approach                | Stack depth | Max input    | Allocations/step |
|-------------------------|:-----------:|:------------:|:----------------:|
| Direct recursion        | O(n)        | ~10 000      | 1 frame          |
| Trampoline (`Bounce<T>`)| O(1)        | unlimited    | 1 record         |
| Kotlin `tailrec`        | O(1)        | unlimited    | 0                |
| Haskell / Erlang        | O(1)        | unlimited    | 0                |

## Files

| File | Purpose |
|------|---------|
| [`broken/RecursiveDemo.java`](broken/RecursiveDemo.java) | Naive recursion that overflows |
| [`Bounce.java`](Bounce.java) | Sealed `Done` / `More` + `run()` loop |
| [`TrampolineDemo.java`](TrampolineDemo.java) | Factorial, mutual recursion, sum |
| [`TrampolineDemo2.java`](TrampolineDemo2.java) | Same algorithms using `BigInteger` — computes 1 000 000! |
