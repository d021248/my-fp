package d021248.mytry.core.lang.fp.comonad;

import java.util.function.Function;

/// The Env comonad: a value `A` paired with a read-only environment `E`.
///
/// `Env<E, A>` is the categorical dual of the Reader monad. Where `Reader`
/// computes `E → A`, `Env` carries the environment alongside the result.
///
/// ## Comonad operations
///
/// | Operation    | Description |
/// |--------------|-------------|
/// | `extract()`  | Return the value, ignoring the environment |
/// | `extend(f)`  | Apply a context-sensitive function, keeping the environment |
/// | `ask()`      | Return the environment (dual of `Reader.ask()`) |
/// | `local(f)`   | Run with a modified environment |
/// | `map(f)`     | Transform the value type |
///
/// ## Monad vs Comonad
///
/// ```
/// Reader<E, A> = E → A           (environment injected in)
/// Env<E, A>    = (E, A)          (environment carried alongside)
/// ```
///
/// ## Example
///
/// ```java
/// record Config(int precision) {}
///
/// var pi = Env.of(new Config(4), Math.PI);
///
/// var formatted = pi.extend(ctx ->
///     ("%." + ctx.ask().precision() + "f").formatted(ctx.extract())
/// );
/// System.out.println(formatted.extract());  // 3.1416
/// ```
public record Env<E, A>(E env, A value) {

  /// Constructs an Env with the given environment and value.
  public static <E, A> Env<E, A> of(E env, A value) {
    return new Env<>(env, value);
  }

  // ─── Comonad operations ────────────────────────────────────────────────

  /// Returns the value, ignoring the environment.
  public A extract() {
    return value;
  }

  /// Returns the environment.
  public E ask() {
    return env;
  }

  /// Applies a context-sensitive function `f`, keeping the same environment.
  public <B> Env<E, B> extend(Function<Env<E, A>, B> f) {
    return new Env<>(env, f.apply(this));
  }

  /// Transforms the value type.
  public <B> Env<E, B> map(Function<A, B> f) {
    return new Env<>(env, f.apply(value));
  }

  /// Returns an Env with an environment modified by `f`.
  public Env<E, A> local(Function<E, E> f) {
    return new Env<>(f.apply(env), value);
  }
}
