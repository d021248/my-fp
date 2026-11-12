package d021248.mytry.core.lang.fp.monads;

/// Demonstrates the Reader, Writer, and State monads.
///
/// Each monad models a different computational effect in a purely functional way:
/// Reader for dependency injection, Writer for accumulation/logging, and
/// State for stateful sequential computations.
public final class StateDemo {

  // ── Domain type ───────────────────────────────────────────────────────────

  record Config(String host, int port, boolean debug) {}

  // ── Reader ────────────────────────────────────────────────────────────────

  static Reader<Config, String> baseUrl() {
    return Reader.<Config, String>asks(Config::host)
        .flatMap(
            host ->
                Reader.<Config, Integer>asks(Config::port)
                    .map(port -> "http://%s:%d".formatted(host, port)));
  }

  static Reader<Config, String> greeting() {
    return Reader.<Config, Boolean>asks(Config::debug)
        .map(debug -> debug ? "[DEBUG] Hello" : "Hello");
  }

  // ── Writer ────────────────────────────────────────────────────────────────

  static Writer<String, Integer> pipeline(int n) {
    return Writer.of("start(%d); ".formatted(n), n)
        .flatMap(x -> Writer.of("doubled; ", x * 2), String::concat)
        .flatMap(x -> Writer.of("plus-one; ", x + 1), String::concat);
  }

  // ── State ─────────────────────────────────────────────────────────────────

  static State<Integer, String> tick() {
    return State.<Integer>get()
        .flatMap(
            count ->
                State.<Integer>modify(s -> s + 1)
                    .flatMap(_ -> State.<Integer, String>pure("tick #" + (count + 1))));
  }

  static State<Integer, String> tick3() {
    return tick().flatMap(_ -> tick()).flatMap(_ -> tick());
  }

  // ── main ──────────────────────────────────────────────────────────────────

  public static void main() {
    var cfg = new Config("localhost", 8080, true);

    System.out.println("=== Reader Monad ===");
    System.out.println("Base URL  : " + baseUrl().run(cfg));
    System.out.println("Greeting  : " + greeting().run(cfg));
    System.out.println("Prod URL  : " + baseUrl().run(new Config("api.example.com", 443, false)));

    System.out.println("\n=== Writer Monad ===");
    var w = pipeline(5);
    System.out.println("Value     : " + w.value());
    System.out.println("Log       : " + w.log());

    System.out.println("\n=== State Monad ===");
    System.out.println("Result    : " + tick3().eval(0));
    System.out.println("Final cnt : " + tick3().exec(0));
  }
}
