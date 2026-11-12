# `typeclass` — Type Classes

Separate behaviour from data via capability interfaces, without inheritance.

## The problem

Java's usual approach to polymorphism is subclassing: to make a type
`printable`, extend a `Printable` base class or implement a `Printable`
interface. This breaks for:

- **External types** you don't own (e.g. `String`, `Integer`)
- **Multiple instances** of the same behaviour (decimal vs hex `Show<Integer>`)
- **Retroactive extension** without touching existing code

See `broken/` for the explosion of wrapper classes this forces.

## The solution: capability values

A *type class* is an interface whose *instances* are plain values — lambdas,
method references, or records — rather than base classes:

```java
// define the type class
@FunctionalInterface interface Show<A> { String show(A value); }

// instances are values
Show<Integer> decimal = Object::toString;
Show<Integer> hex     = n -> "0x%X".formatted(n);
Show<String>  quoted  = s -> "\"%s\"".formatted(s);
```

## Key type classes

### `Eq<A>` — equality

```java
Eq<String> ci = Eq.caseInsensitive();     // ignores case
Eq<Double> eps = Eq.approximate(1e-9);    // approximate comparison
Eq<Point>  pt  = Eq.<Double>natural()    // derived from field Eq
                   .contramap(Point::x);
```

**Laws** — reflexive, symmetric, transitive.

### `Show<A>` — string representation

```java
Show<Integer> hex = n -> "0x%X".formatted(n);
Show<List<Integer>> hexList = Show.listShow(hex);
// hexList.show(List.of(1, 255)) == "[0x1, 0xFF]"
```

`listShow` and `optionalShow` derive compound `Show` instances automatically.

## The `contramap` combinator

Both `Eq` and `Show` are *contravariant* functors — they work on inputs.
`contramap` lifts an instance for `B` into one for `A` by mapping `A → B`
first:

```java
Show<Person> showPerson = Show.<String>natural().contramap(Person::name);
Eq<Person>   eqByName   = Eq.<String>natural().contramap(Person::name);
```

## Relationship to other packages

- `typeclass` underpins `lens` — a `Lens<S, A>` can be seen as an advanced
  type class instance that captures the "focusable field" capability
- `profunctor` — `ProfunctorFn<A, B>` is a type class over pairs of types

## Running the demo

```bash
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.typeclass.TypeClassDemo
```

## Test coverage

`TypeClassTest` — verifies `Eq` laws, `Show` derived instances, `contramap`,
case-insensitive equality, approximate double equality, and `listShow`.
