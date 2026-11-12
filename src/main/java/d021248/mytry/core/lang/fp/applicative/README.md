# Applicative Functor — `core/lang/fp/applicative`

This package demonstrates the **Applicative functor** pattern, the middle layer
of the Functor → Applicative → Monad hierarchy, using accumulating validation as
a concrete example.

---

## The Functor → Applicative → Monad Hierarchy

| Level | Key operation | What it can express |
|-------|--------------|---------------------|
| Functor | `map` | Transform a value inside a context |
| **Applicative** | `ap`, `liftA2` | Combine **independent** effects |
| Monad | `flatMap` | Sequence **dependent** steps |

Every Monad is an Applicative; every Applicative is a Functor.  The step up from
Functor to Applicative adds the ability to combine two contexts whose contents do
not depend on each other.  The step up to Monad adds the ability to make the next
context depend on the value produced by the previous one.

---

## `Validated<E, A>` — the key type

```
Validated<E, A>
  ├── Valid<E, A>(A value)         — successful result
  └── Invalid<E, A>(List<E> errors) — one or more accumulated failures
```

### `ap` — applying a wrapped function

```
ap : Validated<E, A→B>  →  Validated<E, A>  →  Validated<E, B>
```

When both the wrapped function **and** the argument are invalid, `ap` **merges**
their error lists.  This is the defining behaviour of Applicative: it processes
both sides independently and accumulates results.

```java
Validated<String, Function<Integer, String>> vf = Validated.invalid("fn-error");
Validated<String, Integer> va = Validated.invalid("val-error");

va.ap(vf);
// → Invalid(["fn-error", "val-error"])   — both errors collected
```

### `liftA2` — combining two independent validations

```
liftA2 : (A, B → C)  →  Validated<E, A>  →  Validated<E, B>  →  Validated<E, C>
```

`liftA2` is the idiomatic way to combine two validations.  It is implemented
purely in terms of `map` + `ap`:

```java
static <E, A, B, C> Validated<E, C> liftA2(
        BiFunction<A, B, C> f, Validated<E, A> va, Validated<E, B> vb) {
    return vb.ap(va.map(a -> b -> f.apply(a, b)));
}
```

### `ap` accumulates vs `flatMap` short-circuits

| Operation | Both valid | First invalid | Both invalid |
|-----------|-----------|---------------|--------------|
| `ap` | applies function | propagates errors | **merges** both error lists |
| `flatMap` | applies function | propagates errors | ignores second — only first errors returned |

```java
// ap — both error lists merged
Validated.<String, Integer>invalid("err-a")
    .ap(Validated.invalid("err-fn"));
// → Invalid(["err-fn", "err-a"])

// flatMap — second step never executed
Validated.<String, Integer>invalid("err-a")
    .flatMap(n -> Validated.invalid("err-b"));
// → Invalid(["err-a"])   — "err-b" never seen
```

---

## Form Validation Example

Three field validators run as independent `Validated` values; `liftA2` combines
them in pairs, accumulating all errors in a single pass:

```java
record FormInput(String name, int age, String email) {}
record NameAndAge(String name, int age) {}

static Validated<String, FormInput> validateForm(String name, int age, String email) {
    var vNameAge = Validated.liftA2(NameAndAge::new, validateName(name), validateAge(age));
    return Validated.liftA2(
            (na, e) -> new FormInput(na.name(), na.age(), e),
            vNameAge, validateEmail(email));
}

validateForm("", -5, "not-an-email");
// → Invalid(["name must not be blank", "age must be non-negative, got: -5",
//            "email must contain '@', got: not-an-email"])
```

All three errors are reported in one shot, compared to the `broken/` sub-package
which stops after the first.

---

## Files

| File | Description |
|------|-------------|
| `Validated.java` | Sealed interface — `Valid` / `Invalid` with `map`, `ap`, `flatMap`, `liftA2` |
| `ValidatedDemo.java` | Runnable demo: all-valid, all-invalid, and `flatMap` short-circuit |
| `package-info.java` | Package-level Javadoc |
| `broken/EarlyExitValidation.java` | Contrasting early-exit approach that hides all but the first error |
| `broken/package-info.java` | Sub-package Javadoc |

---

## Running the Demo

```bash
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.applicative.ValidatedDemo
```

To see the contrasting broken approach:

```bash
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.applicative.broken.EarlyExitValidation
```

---

## Further Reading

- Haskell `Applicative` typeclass documentation — the original formulation
- "Applicative Programming with Effects" (McBride & Paterson, 2008) — foundational paper
- Cats `Validated` — Scala library implementation of the same pattern
