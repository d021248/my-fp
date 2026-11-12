package d021248.mytry.core.lang.fp.monads;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/// The State monad: a computation that threads mutable state `S` through a
/// pipeline, producing a value `A` at each step.
///
/// Conceptually `State<S, A>` is `S → (A, S)` — it takes an input state and
/// returns a result value paired with an updated state, without any actual
/// mutation.
///
/// ## Operations
///
/// | Operation       | Meaning |
/// |-----------------|---------|
/// | `pure(a)`       | Yield `a` without touching state |
/// | `get()`         | Read the current state as the result |
/// | `put(s)`        | Replace the state; result is `null` |
/// | `modify(f)`     | Transform the state with `f`; result is `null` |
/// | `map(f)`        | Transform the result value |
/// | `flatMap(f)`    | Sequence two stateful computations |
/// | `eval(s)`       | Run the computation; return only the result |
/// | `exec(s)`       | Run the computation; return only the final state |
///
/// ## Example
///
/// ```java
/// State<Integer, String> tick =
///     State.<Integer>get()
///          .flatMap(n -> State.<Integer>modify(s -> s + 1)
///          .flatMap(_ -> State.<Integer, String>pure("tick #" + (n + 1))));
///
/// System.out.println(tick.eval(0));  // tick #1
/// System.out.println(tick.exec(0));  // 1
/// ```
@FunctionalInterface
public interface State<S, A> {

  /// Runs this state computation starting from `state`.
  ///
  /// @param state the initial state value
  /// @return a pair of the result and the updated state
  Pair<A, S> run(S state);

  // ─── Constructors ──────────────────────────────────────────────────────

  /// Returns a computation that yields `value` without modifying the state.
  static <S, A> State<S, A> pure(A value) {
    return s -> new Pair<>(value, s);
  }

  /// Returns a computation that exposes the current state as its result.
  static <S> State<S, S> get() {
    return s -> new Pair<>(s, s);
  }

  /// Returns a computation that replaces the state with `newState`.
  // Void has no instances; null is the conventional Java stand-in for the unit type here.
  static <S> State<S, Void> put(S newState) {
    return _ -> new Pair<>(null, newState);
  }

  /// Returns a computation that applies `f` to the state.
  static <S> State<S, Void> modify(UnaryOperator<S> f) {
    return s -> new Pair<>(null, f.apply(s));
  }

  // ─── Functor / Monad ──────────────────────────────────────────────────

  /// Transforms the result value, threading the state unchanged.
  default <B> State<S, B> map(Function<A, B> f) {
    return s -> {
      var p = run(s);
      return new Pair<>(f.apply(p.first()), p.second());
    };
  }

  /// Sequences two state computations, threading state through both.
  default <B> State<S, B> flatMap(Function<A, State<S, B>> f) {
    return s -> {
      var p = run(s);
      return f.apply(p.first()).run(p.second());
    };
  }

  // ─── Runners ──────────────────────────────────────────────────────────

  /// Runs the computation and returns only the result value.
  default A eval(S state) {
    return run(state).first();
  }

  /// Runs the computation and returns only the final state.
  default S exec(S state) {
    return run(state).second();
  }

  // ─── Result pair ──────────────────────────────────────────────────────

  /// Holds the result value `A` and the updated state `S` from a single step.
  record Pair<A, S>(A first, S second) {}
}
