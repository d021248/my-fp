# `core.lang.fp.lazy` — Null-Safe Memoising `Lazy<T>`

A thread-safe, lazily-evaluated value holder. The supplier is called at most
once, even under concurrent access, and the result is memoised for all
subsequent calls.

---

## The problem

`Supplier<T>` is eager: every `get()` re-computes. A naive `if (value == null)`
double-checked lock breaks when the supplier legitimately returns `null`.
`Lazy<T>` solves both with an `UNSET` sentinel object so that `null` is a valid
cached value.

---

## API

```java
Lazy<T> lazy = Lazy.of(() -> expensiveComputation());
lazy.get();           // computed on first call, memoised thereafter
lazy.isEvaluated();   // true after first get()
```

```java
Lazy<T> eager = Lazy.eager(knownValue);  // already evaluated, no supplier
```

```java
Lazy<U> mapped = lazy.map(fn);  // new Lazy that applies fn when evaluated
```

---

## Thread safety

`value` is declared `volatile`. A double-checked locking pattern guards the
supplier call:

```
first get()  → value is UNSET → synchronize → call supplier → store → return
later get()  → value is T     → return immediately (no lock)
```

The `UNSET` sentinel ensures that `null` returned by the supplier is correctly
memoised (stored and returned on all subsequent calls without re-invoking the
supplier).

---

## Comparison with alternatives

| | `Lazy<T>` | `Optional<T>` | `Supplier<T>` |
|---|---|---|---|
| Null-safe memoisation | yes | N/A | no |
| Thread-safe | yes | N/A | no (by default) |
| Composition with `map` | yes | yes | yes |
| Signals evaluated state | `isEvaluated()` | `isPresent()` | no |

---

## Demo

```bash
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.lazy.LazyDemo
```
