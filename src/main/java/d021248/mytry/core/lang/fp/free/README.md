# `free` — Free Monad

Encode programs as data structures, then interpret them independently.

## The core idea

The Free monad separates *what a program does* from *how it is executed*:

- **Program layer** — write business logic using `get`, `put`, `delete`.
  Programs are plain values; they compose with `flatMap`.
- **Interpreter layer** — supply the meaning at the call site. Swap in a
  real `HashMap` store, a logging store, a dry-run store, or a pure test
  fixture without touching the program code.

## Structure

```
Free<A>
 ├── Pure<A>      — done; carries result value a
 ├── Lift<A>      — a single StoreOp step (op contains the continuation)
 └── Bind<A,B>    — sequence: run step:Free<B>, feed B to k:B→Free<A>
```

`StoreOp<K>` is the *functor* that the Free monad lifts:

```
StoreOp<K>
 ├── Get<K>       — read key; continuation receives Optional<String>
 ├── Put<K>       — write key/value; continuation receives Void
 └── Delete<K>    — delete key; continuation receives Void
```

## Usage

```java
// build a program (no I/O yet)
Free<Optional<String>> program =
    Free.put("lang", "Java")
        .andThen(Free.put("version", "25"))
        .andThen(Free.get("lang"));

// interpret with a HashMap store
HashMap<String, String> store = new HashMap<>();
Optional<String> result = Free.run(program, new HashMapInterpreter(store));
// result == Optional.of("Java")
```

## Stack safety

`Free.run` uses an explicit continuation stack rather than recursion, so
arbitrarily deep programs execute without `StackOverflowError`.

## Laws

`Free<A>` is a monad, so it satisfies the three monad laws:

1. **Left identity**: `Free.pure(a).flatMap(k) == k(a)`
2. **Right identity**: `m.flatMap(Free::pure) == m`
3. **Associativity**: `(m.flatMap(f)).flatMap(g) == m.flatMap(a -> f(a).flatMap(g))`

## Relationship to other packages

- `tagless` — an alternative approach: encode programs as type-parameterised
  interfaces rather than as data; often simpler but less introspectable
- `state` — `Reader`/`Writer`/`State` monads solve a narrower version of the
  same problem; `Free` generalises them all

## Running the demo

```bash
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.free.FreeDemo
```

## Test coverage

`FreeTest` — verifies round-trip execution, `flatMap` sequencing, and
interpreter swapping.
