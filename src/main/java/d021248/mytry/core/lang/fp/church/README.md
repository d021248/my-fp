# `core/lang/fp/church` — Church Encodings & the Y Combinator

Church encodings express **data as functions**. There are no integers, no
booleans, no pairs — just lambdas. Everything you would normally store in a
field, you store in a closure. This is how Alonzo Church proved, in the 1930s,
that lambda calculus is Turing-complete using only function application and
abstraction.

---

## Why does this matter?

Church encodings are not practical — Java already has `int`, `boolean`, and
records. Their value is conceptual:

- They show that **data and behaviour are the same thing**. A Church boolean
  *is* an if-expression. A Church numeral *is* a loop.
- They illuminate how every functional language runtime actually works under
  the hood.
- They introduce the **Y combinator**, which shows how recursion can be derived
  from first principles without any self-referential language feature.

---

## Church Booleans

A Church boolean is a function of two arguments: it returns the first for
`true`, the second for `false`.

```
TRUE  = λt. λf. t        — always picks the first  branch
FALSE = λt. λf. f        — always picks the second branch
```

```java
Bool TRUE  = (t, f) -> t;
Bool FALSE = (t, f) -> f;
```

Logical operators follow naturally:

```java
// AND: if a, then ask b; otherwise return f directly
Bool and(Bool a, Bool b) = (t, f) -> a.select(b.select(t, f), f);

// NOT: swap the two branches
Bool not(Bool b)         = (t, f) -> b.select(f, t);
```

---

## Church Numerals

A Church numeral `n` is a **higher-order function** that applies a successor
function `s` exactly `n` times to a starting value `z`:

```
ZERO  = λs. λz. z            — apply s zero times
ONE   = λs. λz. s z          — apply s once
TWO   = λs. λz. s (s z)      — apply s twice
```

```java
Num ZERO = s -> z -> z;
Num succ(Num n) = s -> z -> s.applyAsInt(n.apply(s).applyAsInt(z));
```

**Addition** applies `s` a total of `m + n` times:

```java
Num add(Num m, Num n) = s -> z -> m.apply(s).applyAsInt(n.apply(s).applyAsInt(z));
```

**Multiplication** applies `m.apply(s)` exactly `n` times — because applying
a function `m` times, `n` times, applies it `m × n` times:

```java
Num mul(Num m, Num n) = s -> n.apply(m.apply(s));
```

To convert back to a Java `int`, count how many times `z+1` is applied:

```java
int toInt(Num n) = n.apply(z -> z + 1).applyAsInt(0);
```

---

## Church Pairs

A Church pair holds two values `a` and `b` by storing a lambda that applies a
selector function to them:

```
pair a b = λf. f a b          — store a and b in a closure
fst p    = p (λa. λb. a)      — extract first
snd p    = p (λa. λb. b)      — extract second
```

```java
Pair pair(Object a, Object b) = f -> f.apply(a, b);
Object fst(Pair p)            = p.apply((a, _) -> a);
Object snd(Pair p)            = p.apply((_, b) -> b);
```

---

## The Self-Reference Problem

Recursion seems to require a function that refers to itself:

```java
Function<Integer, Integer> factorial = n -> n <= 1 ? 1 : n * factorial.apply(n - 1);
//                                                               ^^^^^^^^^ compile error
```

Java rejects this — the variable `factorial` is not in scope inside the
lambda that initialises it. See
[`broken/SelfReferenceLambdaDemo.java`](broken/SelfReferenceLambdaDemo.java)
for a detailed look at this error.

---

## The Y Combinator: Recursion Without Self-Reference

The **Y combinator** (also called the fixed-point combinator) derives
recursion from a non-recursive *one-step description*.

### Intuition

Instead of writing a function that calls itself, you write a function `f` that
receives *itself* as an argument `self` and calls `self` instead:

```
f = λself. λn. if n ≤ 1 then 1 else n × (self n-1)
```

The Y combinator is the function that takes this `f` and produces the
recursive version:

```
Y f = f (Y f)           — fixed-point equation
```

`Y f` is the fixed point of `f`: applying `f` to it produces the same value.

### Why Java cannot use the naive Y

The standard lambda-calculus Y combinator causes infinite expansion under
eager (strict) evaluation:

```
Y f = f (Y f) = f (f (Y f)) = f (f (f (Y f))) = ...
```

Java (like most languages) evaluates arguments before calling functions, so
this loops forever. The solution is to wrap the recursive call in a lambda to
delay it:

```
Y f = (let step = λself. f (λa. self self a) in step step)
```

### Implementation

```java
interface Recursive<A, B> {
    B apply(Recursive<A, B> self, A a);   // self-applicable helper
}

<A, B> Function<A, B> Y(Function<Function<A,B>, Function<A,B>> f) {
    Recursive<A, B> step = (self, a) -> f.apply(arg -> self.apply(self, arg)).apply(a);
    return a -> step.apply(step, a);
}
```

The `Recursive` interface breaks the self-reference cycle that Java's type
system would otherwise reject. `step` receives itself as `self`, then builds
the recursive `Function<A,B>` by wrapping `self.apply(self, ...)` in a lambda
— that lambda is the delayed recursive call.

### Usage

```java
import static d021248.mytry.core.lang.fp.church.Church.*;

Function<Integer, Integer> factorial = Y(f -> n -> n <= 1 ? 1 : n * f.apply(n - 1));
System.out.println(factorial.apply(10));    // 3628800

Function<Integer, Integer> fibonacci = Y(f -> n -> n <= 1 ? n : f.apply(n-1) + f.apply(n-2));
System.out.println(fibonacci.apply(10));    // 55
```

The lambda passed to `Y` is *non-recursive*: it receives `f` as its "self"
and calls `f` instead of referring to itself by name.

---

## Comparison with related packages

| Package | What it solves |
|---|---|
| [`trampoline`](../trampoline/) | Stack-safe recursion on the JVM — prevents `StackOverflowError` for deep call stacks |
| [`cps`](../cps/) | Makes continuations (what to do next) explicit; `callcc` enables non-local exit without exceptions |
| `church` | Derives booleans, numbers, pairs, and recursion from pure lambda calculus — no language primitives needed |

Church encodings and the Y combinator are the starting point; `trampoline` and
`cps` are the practical tools that solve the JVM-specific problems those ideas
expose.

---

## How to Run

```bash
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.church.ChurchDemo
```
