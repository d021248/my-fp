package d021248.mytry.core.lang.fp.turingmachine;

import d021248.mytry.core.lang.fp.trampoline.Bounce;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/// A Turing Machine — an immutable transition table that drives a `Configuration`
/// step by step until halt.
///
/// ## Structure
///
/// The transition table maps `(Run state, Symbol)` pairs to `Rule` triples
/// `(nextState, writeSymbol, headDirection)`.  A missing entry is an implicit
/// halt: the machine stops without writing.
///
/// ## Stack safety
///
/// `step` returns a `Bounce<Configuration>` rather than calling itself recursively.
/// `run` drives the trampoline loop — the depth of the computation never exceeds O(1)
/// stack frames regardless of the number of steps taken.
///
/// ## Building a machine
///
/// ```java
/// var tm = TuringMachine.builder()
///     .on(run("A"), BLANK, run("B"), MARK, R)
///     .on(run("A"), MARK,  halt(),  MARK, R)
///     .build();
/// ```
public record TuringMachine(Map<Key, Rule> table) {

  // -------------------------------------------------------------------------
  // Table key and rule
  // -------------------------------------------------------------------------

  /// Lookup key: the pair (current state, symbol under the head).
  public record Key(TmState.Run state, Symbol symbol) {}

  /// Transition outcome: what to write, where to move, what state to enter next.
  public record Rule(TmState nextState, Symbol write, Direction move) {}

  // -------------------------------------------------------------------------
  // Execution
  // -------------------------------------------------------------------------

  /// Computes exactly one transition from a non-halted `config`.
  /// Returns the successor `Configuration`, or an implicit-halt configuration
  /// if no rule is defined for the current (state, symbol) pair.
  private Configuration transition(Configuration config) {
    var run = (TmState.Run) config.state();
    var symbol = config.tape().read();
    var rule = table.get(new Key(run, symbol));
    if (rule == null) return new Configuration(TmState.halt(), config.tape());
    return new Configuration(rule.nextState(), config.tape().write(rule.write()).move(rule.move()));
  }

  /// Returns the next `Bounce` step for `config`.
  ///
  /// - If already halted → `Bounce.done(config)`.
  /// - If no transition is defined → implicit halt.
  /// - Otherwise → `Bounce.more(…)` with the successor configuration.
  public Bounce<Configuration> step(Configuration config) {
    if (config.halted()) return Bounce.done(config);
    var next = transition(config);
    return next.halted() ? Bounce.done(next) : Bounce.more(() -> step(next));
  }

  /// Runs the machine to completion and returns the halted `Configuration`.
  /// Stack-safe for arbitrarily many steps.
  public Configuration run(Configuration initial) {
    return Bounce.run(step(initial));
  }

  /// Returns a lazy [java.util.stream.Stream] of every configuration visited, from
  /// `initial` through (and including) the first halted configuration.
  ///
  /// The stream is finite and safe to collect.  Each element is one machine step.
  public Stream<Configuration> trace(Configuration initial) {
    return Stream.iterate(
            Optional.of(initial),
            Optional::isPresent,
            opt -> {
              var c = opt.get();
              return c.halted() ? Optional.empty() : Optional.of(transition(c));
            })
        .map(Optional::get);
  }

  // -------------------------------------------------------------------------
  // Builder
  // -------------------------------------------------------------------------

  public static Builder builder() {
    return new Builder();
  }

  /// Fluent builder for transition tables.
  public static final class Builder {

    private final Map<Key, Rule> table = new LinkedHashMap<>();

    /// Adds a transition: in `state`, reading `read`, write `write`, move `dir`, go to `next`.
    public Builder on(TmState.Run state, Symbol read, TmState next, Symbol write, Direction dir) {
      table.put(new Key(state, read), new Rule(next, write, dir));
      return this;
    }

    public TuringMachine build() {
      return new TuringMachine(Map.copyOf(table));
    }
  }
}
