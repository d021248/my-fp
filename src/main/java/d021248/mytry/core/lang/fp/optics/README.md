# `optics` — Iso, Lens, Prism, AffineTraversal & Traversal

Composable, first-class access into immutable data structures. An optic is a
getter/setter pair generalised over *how many* targets it focuses on — from a
reversible `Iso` down to the multi-focus `Traversal`. All compose via `andThen`.

## The optics hierarchy

| Optic | Focuses on | Succeeds? | Reversible? |
|---|---|---|---|
| `Iso<S,A>` | exactly one value (lossless) | always | **yes** |
| `Lens<S,A>` | exactly one field (product type) | always | no |
| `Prism<S,A>` | one variant of a sum type | 0 or 1 | no |
| `AffineTraversal<S,A>` | one optional field (Lens ∘ Prism) | 0 or 1 | no |
| `Traversal<S,A>` | all elements of a collection | 0..N | no |

Weaker optics derive from stronger ones: an `Iso` downcasts to a `Lens`, and
`AffineTraversal.fromLensAndPrism(lens, prism)` builds a partial lens.

## Lens

The problem: updating a deeply nested field in an immutable record requires
spelling out every intermediate field. With three or more levels this becomes
unmanageable. A `Lens<S, A>` is a getter/setter pair for field `A` inside
container `S`:

```java
Lens<Person, Address> addressLens = Lens.of(Person::address,
        (p, a) -> new Person(p.name(), a));
Lens<Address, String> cityLens = Lens.of(Address::city,
        (a, c) -> new Address(c, a.country()));

// compose into a lens that focuses on city inside Person
Lens<Person, String> personCity = addressLens.andThen(cityLens);
Person updated = personCity.set(p, "Hamburg");  // clean, no boilerplate
```

**Laws:** get–set (`get(set(s, a)) == a`), set–get (`set(s, get(s)) == s`),
set–set (`set(set(s, a1), a2) == set(s, a2)`).

## Iso

An `Iso<S, A>` converts between `S` and `A` without losing information: both
directions are total functions and the two round-trip laws hold. It sits at the
top of the hierarchy and downcasts to any weaker optic.

## Prism

A `Prism<S, A>` focuses on one variant of a sealed interface.

```java
Prism<Shape, Double> circle = Prism.of(
        s -> s instanceof Shape.Circle c ? Optional.of(c.radius()) : Optional.empty(),
        Shape.Circle::new);

circle.getOption(new Shape.Circle(3.0))  // Optional.of(3.0)
circle.getOption(new Shape.Rect(1, 2))   // Optional.empty()
circle.review(5.0)                        // Shape.Circle(5.0)
```

**Laws:** `getOption(review(a)) == Optional.of(a)`; if `getOption(s) == Optional.of(a)` then `review(a) == s`.

## AffineTraversal

An `AffineTraversal<S, A>` combines a `Lens` and a `Prism`: focus on an optional
sub-field that may not be present.

```java
AffineTraversal<Container, Double> radius =
        AffineTraversal.fromLensAndPrism(shapeLens, circle);

radius.getOption(new Container("a", new Shape.Circle(4.0)))  // Optional.of(4.0)
radius.modify(c, r -> r * 2)  // doubles radius if Circle, no-op if Rect
```

## Traversal

A `Traversal<S, A>` focuses on *all* elements of a collection at once.

```java
Traversal<List<Integer>, Integer> t = Traversal.forList();
t.getAll(List.of(1, 2, 3))              // [1, 2, 3]
t.modify(List.of(1, 2, 3), n -> n * 2)  // [2, 4, 6]
```

**Laws:** purity (`modify(s, identity) == s`), fusion
(`modify(modify(s, f), g) == modify(s, g ∘ f)`).

## Relationship to other packages

- `profunctor` — every optic is a profunctor transformer:
  `Prism ≅ Choice`, `Lens ≅ Strong`, `AffineTraversal ≅ Choice ∩ Strong`
- `zipper` — `ListZipper` is isomorphic to `Store<Int, A>`; its `focus` is a `Lens`,
  and navigating it composes with `Traversal`
- `typeclass` — `Lens` captures the "focusable field" capability as a type-class instance

## Running the demos

```bash
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.optics.LensDemo
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.optics.IsoDemo
mvn exec:java -Dexec.mainClass=d021248.mytry.core.lang.fp.optics.OpticsDemo
```

## Test coverage

- `LensTest` — all three lens laws, `modify`, `andThen` composition
- `IsoTest`, `IsoPropertyTest` — round-trip laws, downcasting to weaker optics
- `OpticsTest` — Prism laws, modify, compose, Traversal operations
- `AffineTraversalTest` — `fromLensAndPrism`, get/set/modify, `andThen`
- `PrismPropertyTest` — Prism laws under property-based testing
