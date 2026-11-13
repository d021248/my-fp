# `fp/turingmachine/` — Turing Machine

A minimal, immutable Turing Machine built from five Java records.  
Everything is pure: no mutation, no side effects, no exceptions — just data and functions.

---

## Components

| Class | Role |
|---|---|
| `Symbol` | Tape alphabet — sealed: `Blank` / `Mark` |
| `Direction` | Head movement — `L` / `R` / `N` (no-op) |
| `TmState` | Control state — sealed: `Run(name)` / `Halt` |
| `Tape` | Conceptually infinite symbol sequence with a read/write head |
| `Configuration` | Runtime snapshot: `(state, tape)` |
| `TuringMachine` | Immutable transition table — drives `Configuration` step by step |

---

## The Builder

A `TuringMachine` is nothing but a transition table: a map from
`(current state, symbol under head)` to `(next state, symbol to write, direction to move)`.

The builder makes this table readable as plain text:

```java
var tm = TuringMachine.builder()
    .on(scan, MARK,  scan,   MARK, R)   // in SCAN, reading 1 → stay in SCAN, write 1, go right
    .on(scan, BLANK, halt(), MARK, N)   // in SCAN, reading _ → halt, write 1, stay
    .build();
```

Each `.on(...)` call adds exactly one row to the table.

### Signature

```
builder.on(state, read, nextState, write, direction)
```

| Parameter | Type | Meaning |
|---|---|---|
| `state` | `TmState.Run` | The current control state (must be `Run`, not `Halt`) |
| `read` | `Symbol` | The symbol currently under the head |
| `nextState` | `TmState` | The state to enter after this transition |
| `write` | `Symbol` | The symbol to write before moving |
| `direction` | `Direction` | Head movement: `L`, `R`, or `N` |

### Why `Run` not `TmState` for `state`?

A `Halt` state has no transitions by definition — the machine stops.  
Requiring `TmState.Run` as the parameter type makes this a compile-time guarantee:
you cannot accidentally add a rule for a halted machine.

### Implicit halt

Missing entries are not an error.  
If the machine reaches a `(state, symbol)` pair with no rule in the table,
it halts automatically — this is the standard definition of an implicit halt.

---

## Execution

### `run` — to completion

```java
Configuration result = tm.run(Configuration.initial(startState));
```

Drives the machine until it halts and returns the final `Configuration`.  
Stack-safe for any number of steps — internally uses the `Bounce` trampoline.

### `trace` — step by step

```java
tm.trace(initial).forEach(cfg -> System.out.println(cfg.tape().display()));
```

Returns a lazy `Stream<Configuration>` from the initial configuration through
(and including) the halted terminal state.  Each element is one machine step.  
**Only feasible when the total step count is small** — collecting all configurations
into a list (e.g. with `.toList()`) requires memory proportional to step count.

### `runStats` — completion with step count

```java
TuringMachine.RunStats stats = tm.runStats(Configuration.initial(startState));
// stats.halted()  — final Configuration
// stats.steps()   — number of transitions taken
```

Same as `run`, but also returns the step count.  
Iterates `transition` directly in a `while` loop — no `Bounce` allocation per step.

### `runFast` — O(1) per step for large machines

```java
TuringMachine.FastStats stats = tm.runFast(startState);
// stats.steps()  — number of transitions
// stats.marks()  — number of marks on the final tape
```

The `Tape`/`ListZipper` design is immutable and pedagogically pure, but `moveLeft` /
`moveRight` call `List.copyOf()` — each head movement copies the neighbour list,
making each step **O(tape length)**.  For machines with millions of steps that becomes
prohibitively slow (47 M steps × ~6 000 avg-cells ≈ 280 billion element operations).

`runFast` bypasses the immutable tape entirely and uses a `HashMap<Integer, Symbol>`
keyed by cell index.  Every read, write, and move is **O(1) amortised**.  
The trade-off: no `Configuration` snapshots, no `trace`, no replay — just final stats.

---

## Example: Unary increment

Input tape `[1] 1 1` (the number 3), output tape `1 1 1 [1]` (the number 4):

```
 Step │ State    │ Tape
──────┼──────────┼────────────────────────────────
    0 │ SCAN     │ [1] 1 1
    1 │ SCAN     │ 1 [1] 1
    2 │ SCAN     │ 1 1 [1]
    3 │ SCAN     │ 1 1 1 [_]
    4 │ HALT     │ 1 1 1 [1]
```

## Example: 3-state Busy Beaver

Starts on a blank tape, writes the maximum possible 6 marks in 13 steps:

```
 Step │ State    │ Tape
──────┼──────────┼────────────────────────────────
    0 │ A        │ [_]
    1 │ B        │ 1 [_]
    2 │ A        │ [1] 1
    3 │ C        │ [_] 1 1
    4 │ B        │ [_] 1 1 1
    5 │ A        │ [_] 1 1 1 1
    6 │ B        │ 1 [1] 1 1 1
    7 │ B        │ 1 1 [1] 1 1
    8 │ B        │ 1 1 1 [1] 1
    9 │ B        │ 1 1 1 1 [1]
   10 │ B        │ 1 1 1 1 1 [_]
   11 │ A        │ 1 1 1 1 [1] 1
   12 │ C        │ 1 1 1 [1] 1 1
   13 │ HALT     │ 1 1 1 1 [1] 1
```

## Example: 4-state Busy Beaver (107 steps)

For n = 4 the champion writes 13 marks in 107 steps (Brady 1964).
Transition table:

| State | Read | Write | Move | Next |
|-------|------|-------|------|------|
| A     | 0    | 1     | R    | B    |
| A     | 1    | 1     | L    | B    |
| B     | 0    | 1     | L    | A    |
| B     | 1    | 0     | L    | C    |
| C     | 0    | 1     | R    | HALT |
| C     | 1    | 1     | L    | D    |
| D     | 0    | 1     | R    | D    |
| D     | 1    | 0     | R    | A    |

First and last few steps:

```
 Step │ State    │ Tape
──────┼──────────┼────────────────────────────────
    0 │ A        │ [_]
    1 │ B        │ 1 [_]
    2 │ A        │ [1] 1
    3 │ B        │ [_] 1 1
    4 │ A        │ [_] 1 1 1
   ...   (107 steps total)
  104 │ A        │ 1 [1] 1 1 1 1 1 1 1 1 1 1 1
  105 │ B        │ [1] 1 1 1 1 1 1 1 1 1 1 1 1
  106 │ C        │ [_] _ 1 1 1 1 1 1 1 1 1 1 1 1
  107 │ HALT     │ 1 [_] 1 1 1 1 1 1 1 1 1 1 1 1

  → 13 marks in 107 steps
```

## Example: 5-state Busy Beaver (47 million steps)

For n = 5 the champion writes 4,098 marks in 47,176,870 steps (Marxen & Buntrock 1989).  
`trace` is infeasible at this scale; the demo uses `runFast`.

Transition table:

| State | Read | Write | Move | Next |
|-------|------|-------|------|------|
| A     | 0    | 1     | R    | B    |
| A     | 1    | 1     | L    | C    |
| B     | 0    | 1     | R    | C    |
| B     | 1    | 1     | R    | B    |
| C     | 0    | 1     | R    | D    |
| C     | 1    | 0     | L    | E    |
| D     | 0    | 1     | L    | A    |
| D     | 1    | 1     | L    | D    |
| E     | 0    | 1     | R    | HALT |
| E     | 1    | 0     | L    | A    |

```
  Steps:    47,176,870  (expected 47,176,870)
  Marks:         4,098  (expected 4,098)
  Time:          ~1.6 s
```

---

## Design notes  
`Symbol`, `Direction`, `TmState`, `Tape`, `Configuration`, and `TuringMachine` are all records.
Every `Tape` operation (write, move) returns a new `Tape`; the original is untouched.
Computation history is trivially replayable: save any `Configuration`, run from there.

**`Tape` is a `ListZipper`.**  
The focused cell is the zipper's focus; left and right neighbours are the zipper's context.
Moving past either end extends the tape silently with `Blank` — the caller never sees an
`Optional` or an exception.

**Stack safety via `Bounce`.**  
`TuringMachine.run` drives a trampoline loop (`fp/trampoline/Bounce`).
The call depth stays at O(1) regardless of how many steps the machine takes.

**`TmState` is a sum type.**  
`sealed interface TmState permits Halt, Run` — a `switch` over `TmState` must handle
both variants or the compiler rejects it.  The type system enforces the termination contract.
