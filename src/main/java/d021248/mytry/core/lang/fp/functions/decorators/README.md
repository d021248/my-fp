# `functional` — Function Decorators

Composable wrappers over `Function<A, B>` that add cross-cutting behaviour
without modifying the underlying function.

## The pattern

A *decorator* takes a `Function<A, B>` and returns a new `Function<A, B>`:

```java
Function<String, Integer> base = Integer::parseInt;
Function<String, Integer> fast = FunctionalDecorators.memoize(base);
Function<String, Integer> loud = FunctionalDecorators.withLogging("parse", fast);
```

The original function is untouched. Decorators stack freely via nesting or
`andThen`.

## Available decorators

| Decorator | What it adds |
|---|---|
| `memoize(f)` | Caches results by input; calls `f` at most once per distinct value |
| `withLogging(name, f)` | Prints start/done around each call |
| `withTiming(name, f)` | Prints elapsed time in µs after each call |
| `withRetry(n, f)` | Retries up to `n` times on `RuntimeException` |
| `withFallback(default, f)` | Returns `default` when `f` throws |

## Why this matters

Java's standard library has no decorator protocol for `Function`. Each
cross-cutting concern (logging, caching, retrying) typically requires a
subclass or a wrapper object. Here, each concern is just a higher-order
function — the composition algebra is the `Function` interface itself.

## Relationship to other packages

- `lazy` — `Lazy<T>` is `memoize` applied to `Supplier<T>`, a zero-argument
  function
- `currying` — partially-applied functions compose with `memoize`/`withLogging`
  via standard `andThen`

## Running the demo

```bash
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.lambda.decorators.FunctionalDecoratorsDemo
```

## Test coverage

`FunctionalDecoratorsTest` — verifies memoize, retry, fallback, logging, and
timing decorators, including composition of multiple decorators.
