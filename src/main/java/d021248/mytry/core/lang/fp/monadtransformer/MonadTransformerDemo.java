package d021248.mytry.core.lang.fp.monadtransformer;

/// Demonstrates monad transformers with a bounded counter state machine.
///
/// The example builds a counter that can be incremented but **fails** once
/// it exceeds a maximum value. This models "stateful computation that may
/// abort" — a pattern that requires both `State` (to track the count) and
/// `Optional` (to signal failure), combined by `StateT`.
public final class MonadTransformerDemo {

  // ── StateT demo: bounded counter ─────────────────────────────────────────

  /// Tries to increment the counter; fails if it would exceed `max`.
  static StateT<Integer, String> increment(int max) {
    return StateT.<Integer>get()
        .flatMap(
            current ->
                current < max
                    ? StateT.<Integer>modify(n -> n + 1).map(n -> "tick → %d".formatted(n))
                    : StateT.fail());
  }

  // ── ReaderT demo: config-aware greeting ──────────────────────────────────

  record AppConfig(String prefix, boolean formal) {}

  /// Returns a greeting from the config, or fails if the prefix is blank.
  static ReaderT<AppConfig, String> greeting(String name) {
    return ReaderT.<AppConfig, String>asks(AppConfig::prefix)
        .flatMap(
            prefix ->
                prefix.isBlank()
                    ? ReaderT.fail()
                    : ReaderT.<AppConfig, Boolean>asks(AppConfig::formal)
                        .flatMap(
                            formal ->
                                ReaderT.pure(
                                    formal
                                        ? "%s %s, sir.".formatted(prefix, name)
                                        : "%s %s!".formatted(prefix, name))));
  }

  public static void main() {
    System.out.println("=== StateT: bounded counter ===");

    var program =
        increment(3)
            .flatMap(msg -> StateT.<Integer>get().map(n -> msg + "  (state=%d)".formatted(n)));

    System.out.println("start=0: " + program.run(0)); // Optional[(tick → 1 (state=1), 1)]
    System.out.println("start=2: " + program.run(2)); // Optional[(tick → 3 (state=3), 3)]
    System.out.println("start=3: " + program.run(3)); // Optional.empty (exceeded max)

    // Chaining three increments
    var chain = increment(3).flatMap(_ -> increment(3)).flatMap(_ -> increment(3));
    System.out.println("chain from 0: " + chain.run(0)); // succeeds
    System.out.println("chain from 1: " + chain.run(1)); // fails on 3rd step

    System.out.println("\n=== ReaderT: config-aware greeting ===");

    var formalCfg = new AppConfig("Good day,", true);
    var informalCfg = new AppConfig("Hey,", false);
    var blankCfg = new AppConfig("", false);

    System.out.println(greeting("Alice").run(formalCfg)); // Optional[Good day, Alice, sir.]
    System.out.println(greeting("Bob").run(informalCfg)); // Optional[Hey, Bob!]
    System.out.println(greeting("Carol").run(blankCfg)); // Optional.empty
  }
}
