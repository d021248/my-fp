# `core/lang/fp/state` — Reader, Writer, and State Monads

Three monads that model common computational effects in a purely functional style.

## The Problem

Consider passing a database config through a pipeline:

```java
// Every helper must declare config as a parameter — even if it doesn't use it
String buildUrl(Config cfg) { return "http://" + cfg.host() + ":" + cfg.port(); }

User fetchUser(Config cfg, int id) {
    var url = buildUrl(cfg);           // must pass cfg everywhere
    return http.get(url + "/users/" + id);
}
```

The monads replace explicit parameter threading with a `flatMap` pipeline.

---

## Reader Monad

`Reader<E, A>` = `E → A` — a computation that reads from environment `E`.

```java
record Config(String host, int port) {}

Reader<Config, String> baseUrl =
    Reader.<Config, String>asks(Config::host)
          .flatMap(host -> Reader.<Config, Integer>asks(Config::port)
          .map(port -> "http://%s:%d".formatted(host, port)));

System.out.println(baseUrl.run(new Config("localhost", 8080)));
// http://localhost:8080
```

### Key operations

| Operation    | Description |
|--------------|-------------|
| `pure(a)`    | Ignore the environment, return `a` |
| `ask()`      | Return the entire environment |
| `asks(f)`    | Project a field: `Reader.asks(Config::host)` |
| `local(f)`   | Run with a modified environment |
| `map(f)`     | Transform the result |
| `flatMap(f)` | Sequence computations sharing the same env |

---

## Writer Monad

`Writer<W, A>` = `(W, A)` — a value paired with an accumulated log.

```java
Writer<String, Integer> result =
    Writer.of("start; ", 10)
          .flatMap(n -> Writer.of("doubled; ", n * 2), String::concat)
          .flatMap(n -> Writer.of("done; ",   n + 1), String::concat);

System.out.println(result.log());    // start; doubled; done;
System.out.println(result.value());  // 21
```

### Key operations

| Operation              | Description |
|------------------------|-------------|
| `of(log, value)`       | Pair a value with a log entry |
| `pure(value, empty)`   | Start with an empty log |
| `tell(log)`            | Emit a log entry, result is null |
| `map(f)`               | Transform value; log unchanged |
| `flatMap(f, combine)`  | Sequence, joining logs with `combine` |

---

## State Monad

`State<S, A>` = `S → (A, S)` — reads and updates state, returning a value.

```java
State<Integer, String> tick =
    State.<Integer>get()
         .flatMap(n -> State.<Integer>modify(s -> s + 1)
         .flatMap(_ -> State.<Integer, String>pure("tick #" + (n + 1))));

System.out.println(tick.eval(0));   // tick #1
System.out.println(tick.exec(0));   // 1  (state after one tick)
```

### Key operations

| Operation    | Description |
|--------------|-------------|
| `pure(a)`    | Return `a` without touching state |
| `get()`      | Expose current state as result |
| `put(s)`     | Replace state |
| `modify(f)`  | Transform state with `f` |
| `eval(s)`    | Run; return only the result |
| `exec(s)`    | Run; return only the final state |

---

## How They Relate

```
Reader<E,A> = E -> A          (read-only)
Writer<W,A> = (W, A)          (write-only)
State<S,A>  = S -> (A, S)     (read + write)

State generalises both:
  State.get()    acts like Reader.ask()
  State.put(s)   acts like Writer.tell(s)
```

---

## Files

| File | Purpose |
|------|---------|
| `Reader.java` | Reader monad — dependency injection |
| `Writer.java` | Writer monad — accumulation/logging |
| `State.java` | State monad — stateful computation |
| `StateDemo.java` | Demo showing all three |
| `broken/ManualStatePassing.java` | Comparison: explicit state threading |
