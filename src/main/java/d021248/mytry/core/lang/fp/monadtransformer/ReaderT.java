package d021248.mytry.core.lang.fp.monadtransformer;

import module java.base;

/// Monad transformer: Reader layered over Optional — `E → Optional<A>`.
///
/// `ReaderT<E, A>` extends the plain `Reader<E, A>` with the ability to
/// **fail**: each step in a pipeline may return `Optional.empty()` instead
/// of a value, and subsequent steps are automatically short-circuited.
///
/// ## Stacking monads
///
/// A plain `Reader<E, A>` models "read-only config injection" but cannot
/// fail. A plain `Optional<A>` models "may be absent" but has no access
/// to shared config. `ReaderT` gives you **both at once**: computations
/// read from an environment `E` and may fail at any point.
///
/// ## Core operations
///
/// | Operation      | Meaning |
/// |----------------|---------|
/// | `pure(a)`      | Succeed with `a`, ignoring the environment |
/// | `fail()`       | Always produce `Optional.empty()` |
/// | `ask()`        | Return the whole environment as the value |
/// | `asks(f)`      | Project one field from the environment |
/// | `map(f)`       | Transform the value inside `Optional` |
/// | `flatMap(f)`   | Sequence, short-circuiting on empty |
/// | `local(f)`     | Run in a locally modified environment |
///
/// ## Example
///
/// ```java
/// record Config(String host, boolean secure) {}
///
/// ReaderT<Config, String> scheme =
///     ReaderT.<Config, Boolean>asks(Config::secure)
///            .flatMap(sec -> sec
///                ? ReaderT.pure("https")
///                : ReaderT.fail());
///
/// System.out.println(scheme.run(new Config("example.com", true)));  // Optional[https]
/// System.out.println(scheme.run(new Config("example.com", false))); // Optional.empty
/// ```
@FunctionalInterface
public interface ReaderT<E, A> {

  /// Runs this computation against `env`, returning a possibly-absent result.
  Optional<A> run(E env);

  // ─── Constructors ─────────────────────────────────────────────────────────

  /// Returns a `ReaderT` that always succeeds with `value`.
  static <E, A> ReaderT<E, A> pure(A value) {
    return _ -> Optional.of(value);
  }

  /// Returns a `ReaderT` that always fails.
  static <E, A> ReaderT<E, A> fail() {
    return _ -> Optional.empty();
  }

  /// Returns a `ReaderT` that exposes the whole environment as the result.
  static <E> ReaderT<E, E> ask() {
    return Optional::of;
  }

  /// Returns a `ReaderT` that projects one value from the environment with `f`.
  static <E, A> ReaderT<E, A> asks(Function<E, A> f) {
    return env -> Optional.of(f.apply(env));
  }

  // ─── Functor / Monad ─────────────────────────────────────────────────────

  /// Transforms the result value; propagates empty.
  default <B> ReaderT<E, B> map(Function<A, B> f) {
    return env -> run(env).map(f);
  }

  /// Sequences two `ReaderT` computations; short-circuits on empty.
  default <B> ReaderT<E, B> flatMap(Function<A, ReaderT<E, B>> f) {
    return env -> run(env).flatMap(a -> f.apply(a).run(env));
  }

  /// Runs a sub-computation with the environment transformed by `f`.
  default ReaderT<E, A> local(UnaryOperator<E> f) {
    return env -> run(f.apply(env));
  }
}
