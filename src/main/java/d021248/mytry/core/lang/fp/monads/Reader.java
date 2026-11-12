package d021248.mytry.core.lang.fp.monads;

import java.util.function.Function;

/// The Reader monad: a computation that reads from an immutable environment `E`
/// and produces a value `A`.
///
/// A `Reader<E, A>` is a function `E → A`. It threads a shared, read-only
/// context (e.g., application configuration) through a chain of computations
/// without passing it as an explicit parameter to every method.
///
/// ## Operations
///
/// | Operation     | Meaning |
/// |---------------|---------|
/// | `pure(a)`     | Yield `a` without reading the environment |
/// | `ask()`       | Expose the entire environment as the result |
/// | `asks(f)`     | Project a single field from the environment |
/// | `local(f)`    | Run a sub-computation with a modified environment |
/// | `map(f)`      | Transform the result value |
/// | `flatMap(f)`  | Sequence two computations sharing the same environment |
///
/// ## Example
///
/// ```java
/// record Config(String host, int port) {}
///
/// Reader<Config, String> baseUrl =
///     Reader.<Config, String>asks(Config::host)
///           .flatMap(host -> Reader.<Config, Integer>asks(Config::port)
///           .map(port -> "http://%s:%d".formatted(host, port)));
///
/// System.out.println(baseUrl.run(new Config("localhost", 8080)));
/// // http://localhost:8080
/// ```
@FunctionalInterface
public interface Reader<E, A> {

  /// Runs this reader computation against the given environment.
  ///
  /// @param env the shared environment value
  /// @return the computed result
  A run(E env);

  // ─── Constructors ──────────────────────────────────────────────────────

  /// Returns a reader that yields `value` without consulting the environment.
  static <E, A> Reader<E, A> pure(A value) {
    return _ -> value;
  }

  /// Returns a reader that exposes the entire environment as the result.
  static <E> Reader<E, E> ask() {
    return env -> env;
  }

  /// Returns a reader that applies `selector` to the environment.
  static <E, A> Reader<E, A> asks(Function<E, A> selector) {
    return selector::apply;
  }

  // ─── Functor / Monad ──────────────────────────────────────────────────

  /// Transforms the result value.
  default <B> Reader<E, B> map(Function<A, B> f) {
    return env -> f.apply(run(env));
  }

  /// Sequences two reader computations sharing the same environment.
  default <B> Reader<E, B> flatMap(Function<A, Reader<E, B>> f) {
    return env -> f.apply(run(env)).run(env);
  }

  /// Runs a sub-computation with an environment modified by `f`.
  default Reader<E, A> local(Function<E, E> f) {
    return env -> run(f.apply(env));
  }
}
