package d021248.mytry.core.lang.fp.monads;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StateMonadTest {

  // ── Reader ────────────────────────────────────────────────────────────────

  record Config(String host, int port) {}

  @Nested
  @DisplayName("Reader monad")
  class ReaderTests {

    @Test
    @DisplayName("pure yields the value without reading the environment")
    void pureIgnoresEnv() {
      Reader<Config, Integer> r = Reader.pure(42);
      assertThat(r.run(new Config("localhost", 8080))).isEqualTo(42);
    }

    @Test
    @DisplayName("ask exposes the entire environment as the result")
    void askExposesEnv() {
      Reader<Config, Config> r = Reader.ask();
      var cfg = new Config("host", 80);
      assertThat(r.run(cfg)).isSameAs(cfg);
    }

    @Test
    @DisplayName("asks projects a single field from the environment")
    void asksProjectsField() {
      Reader<Config, String> r = Reader.asks(Config::host);
      assertThat(r.run(new Config("localhost", 8080))).isEqualTo("localhost");
    }

    @Test
    @DisplayName("map transforms the result without changing the environment")
    void mapTransformsResult() {
      var r = Reader.<Config, Integer>asks(Config::port).map(p -> "port=" + p);
      assertThat(r.run(new Config("h", 9000))).isEqualTo("port=9000");
    }

    @Test
    @DisplayName("flatMap sequences two readers sharing the same environment")
    void flatMapSequences() {
      var r =
          Reader.<Config, String>asks(Config::host)
              .flatMap(
                  h ->
                      Reader.<Config, Integer>asks(Config::port).map(p -> "%s:%d".formatted(h, p)));
      assertThat(r.run(new Config("example.com", 443))).isEqualTo("example.com:443");
    }

    @Test
    @DisplayName("local runs a sub-computation with a modified environment")
    void localModifiesEnv() {
      var r =
          Reader.<Config, String>asks(Config::host)
              .local(cfg -> new Config(cfg.host().toUpperCase(), cfg.port()));
      assertThat(r.run(new Config("localhost", 80))).isEqualTo("LOCALHOST");
    }
  }

  // ── Writer ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Writer monad")
  class WriterTests {

    @Test
    @DisplayName("of constructs a writer with the given log and value")
    void ofConstructsWriter() {
      var w = Writer.of("log", 42);
      assertThat(w.log()).isEqualTo("log");
      assertThat(w.value()).isEqualTo(42);
    }

    @Test
    @DisplayName("pure returns value with the supplied empty log")
    void pureHasEmptyLog() {
      var w = Writer.<String, Integer>pure(10, "");
      assertThat(w.log()).isEmpty();
      assertThat(w.value()).isEqualTo(10);
    }

    @Test
    @DisplayName("map transforms the value and leaves the log unchanged")
    void mapTransformsValue() {
      var w = Writer.of("step; ", 5).map(n -> n * 2);
      assertThat(w.log()).isEqualTo("step; ");
      assertThat(w.value()).isEqualTo(10);
    }

    @Test
    @DisplayName("flatMap threads the value and combines logs with the given operation")
    void flatMapCombinesLogs() {
      var w =
          Writer.of("a; ", 1)
              .flatMap(n -> Writer.of("b; ", n + 1), String::concat)
              .flatMap(n -> Writer.of("c; ", n * 3), String::concat);
      assertThat(w.log()).isEqualTo("a; b; c; ");
      assertThat(w.value()).isEqualTo(6);
    }
  }

  // ── State ─────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("State monad")
  class StateTests {

    @Test
    @DisplayName("pure yields value without modifying state")
    void pureDoesNotModifyState() {
      var s = State.<Integer, String>pure("hello");
      assertThat(s.eval(99)).isEqualTo("hello");
      assertThat(s.exec(99)).isEqualTo(99);
    }

    @Test
    @DisplayName("get exposes current state as result")
    void getExposesState() {
      var s = State.<Integer>get();
      assertThat(s.eval(42)).isEqualTo(42);
      assertThat(s.exec(42)).isEqualTo(42);
    }

    @Test
    @DisplayName("put replaces state and produces a null result")
    void putReplacesState() {
      var s = State.<Integer>put(100);
      assertThat(s.exec(0)).isEqualTo(100);
    }

    @Test
    @DisplayName("modify transforms state with the supplied function")
    void modifyTransformsState() {
      var s = State.<Integer>modify(n -> n + 1);
      assertThat(s.exec(5)).isEqualTo(6);
    }

    @Test
    @DisplayName("map transforms result without touching state")
    void mapTransformsResult() {
      var s = State.<Integer>get().map(n -> "n=" + n);
      assertThat(s.eval(7)).isEqualTo("n=7");
      assertThat(s.exec(7)).isEqualTo(7);
    }

    @Test
    @DisplayName("flatMap threads state through two sequential computations")
    void flatMapThreadsState() {
      // get=3, modify(+3) -> state=6, get=6
      var s =
          State.<Integer>get()
              .flatMap(n -> State.<Integer>modify(x -> x + n).flatMap(_ -> State.<Integer>get()));
      assertThat(s.eval(3)).isEqualTo(6);
      assertThat(s.exec(3)).isEqualTo(6);
    }

    @Test
    @DisplayName("eval runs computation and returns only the result")
    void evalReturnsResult() {
      var s = State.<Integer>get().map(n -> n * 2);
      assertThat(s.eval(5)).isEqualTo(10);
    }

    @Test
    @DisplayName("exec runs computation and returns only the final state")
    void execReturnsState() {
      var s = State.<Integer>modify(n -> n + 10);
      assertThat(s.exec(5)).isEqualTo(15);
    }
  }
}
