package d021248.mytry.core.lang.fp.comonad;

import module java.base;

/// The `Traced M A` comonad — a monoid-indexed value `M → A`.
///
/// `Traced` is the categorical dual of the `Writer` monad:
///
/// | | Writer | Traced |
/// |---|---|---|
/// | Structure | `(A, M)` — value paired with a log | `M → A` — value indexed by context |
/// | `extract` | discards the log, returns the value | evaluates at the monoid identity |
/// | `extend` | appends to the log | prepends to the index before querying |
///
/// `M` must form a **monoid** — it must have an identity element (`mempty`)
/// and an associative combining operation (`mappend`).  Pass these as
/// constructor arguments.
///
/// ## Laws
///
/// 1. **extract ∘ extend f == f** (extending with f then extracting = f itself)
/// 2. **extend extract == identity** (extending with extract is a no-op)
/// 3. **extend f ∘ extend g == extend (f ∘ extend g)** (associativity)
///
/// ## Example — string-indexed annotation
///
/// ```java
/// // Build a traced value over String (monoid: ("", ++))
/// Traced<String, Integer> counter = Traced.of(
///         ctx -> ctx.length(),   // length of the context string
///         "",                    // empty string = mempty
///         String::concat);       // string concatenation = mappend
///
/// counter.extract();             // 0  (length of "")
/// counter.ask("hello");          // 5  (length of "hello")
/// counter.local(s -> s + "!")    // decorates the query before evaluating
///         .ask("hi");            // 3  (length of "hi!")
/// ```
///
/// @param <M> the monoid type used as the index/context
/// @param <A> the value type produced for each monoid element
public record Traced<M, A>(Function<M, A> run, M mempty, BinaryOperator<M> mappend) {

  // ── Factory ───────────────────────────────────────────────────────────────

  /// Creates a `Traced` value with an explicit monoid.
  public static <M, A> Traced<M, A> of(Function<M, A> run, M mempty, BinaryOperator<M> mappend) {
    return new Traced<>(run, mempty, mappend);
  }

  // ── Comonad operations ────────────────────────────────────────────────────

  /// Evaluates this traced value at the monoid identity (`mempty`).
  public A extract() {
    return run.apply(mempty);
  }

  /// Evaluates this traced value at `m`.
  ///
  /// Equivalent to `run(m)` but named after the Reader monad `ask` by analogy.
  public A ask(M m) {
    return run.apply(m);
  }

  /// Applies `f` to every possible context `m`, yielding a new traced value
  /// whose answer at `m` is `f` applied to the sub-traced value that
  /// prepends `m` to every future query.
  ///
  /// This is the comonad `extend` (Cobind) operation.
  public <B> Traced<M, B> extend(Function<Traced<M, A>, B> f) {
    return Traced.of(
        m -> f.apply(Traced.of(m2 -> run.apply(mappend.apply(m, m2)), mempty, mappend)),
        mempty,
        mappend);
  }

  /// Applies `g` to every output of this traced value.
  public <B> Traced<M, B> map(Function<A, B> g) {
    return Traced.of(run.andThen(g), mempty, mappend);
  }

  /// Returns a new `Traced` that transforms the incoming index with `f`
  /// before passing it to this value.
  public Traced<M, A> local(UnaryOperator<M> f) {
    return Traced.of(m -> run.apply(f.apply(m)), mempty, mappend);
  }
}
