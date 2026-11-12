package d021248.mytry.core.lang.fp.monadtransformer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MonadTransformerTest {

  // ── ReaderT ───────────────────────────────────────────────────────────────

  record Config(String host, int port, boolean secure) {}

  @Nested
  @DisplayName("ReaderT<E, A>")
  class ReaderTTests {

    @Test
    @DisplayName("pure always returns Optional.of(value) regardless of environment")
    void pureReturnsValue() {
      ReaderT<Config, Integer> r = ReaderT.pure(42);
      assertThat(r.run(new Config("localhost", 8080, false))).contains(42);
    }

    @Test
    @DisplayName("fail always returns Optional.empty() regardless of environment")
    void failReturnsEmpty() {
      ReaderT<Config, Integer> r = ReaderT.fail();
      assertThat(r.run(new Config("localhost", 8080, false))).isEmpty();
    }

    @Test
    @DisplayName("ask exposes the entire environment as Optional.of(env)")
    void askExposesEnv() {
      var cfg = new Config("host", 80, true);
      assertThat(ReaderT.<Config>ask().run(cfg)).contains(cfg);
    }

    @Test
    @DisplayName("asks projects a single field from the environment")
    void asksProjectsField() {
      var r = ReaderT.<Config, String>asks(Config::host);
      assertThat(r.run(new Config("example.com", 443, true))).contains("example.com");
    }

    @Test
    @DisplayName("map transforms the value inside Optional")
    void mapTransformsValue() {
      var r = ReaderT.<Config, Integer>asks(Config::port).map(p -> "port=" + p);
      assertThat(r.run(new Config("h", 9000, false))).contains("port=9000");
    }

    @Test
    @DisplayName("map on a failed ReaderT propagates empty")
    void mapOnFailPropagatesEmpty() {
      ReaderT<Config, Integer> r = ReaderT.<Config, Integer>fail().map(n -> n * 2);
      assertThat(r.run(new Config("h", 80, false))).isEmpty();
    }

    @Test
    @DisplayName("flatMap sequences two ReaderT computations sharing the environment")
    void flatMapSequencesComputations() {
      var r =
          ReaderT.<Config, String>asks(Config::host)
              .flatMap(
                  h ->
                      ReaderT.<Config, Integer>asks(Config::port)
                          .map(p -> "%s:%d".formatted(h, p)));
      assertThat(r.run(new Config("example.com", 443, true))).contains("example.com:443");
    }

    @Test
    @DisplayName("flatMap short-circuits when the first step fails")
    void flatMapShortCircuitsOnFail() {
      var r = ReaderT.<Config, String>fail().flatMap(s -> ReaderT.pure("unreachable"));
      assertThat(r.run(new Config("h", 80, false))).isEmpty();
    }

    @Test
    @DisplayName("flatMap short-circuits when the continuation returns fail")
    void flatMapShortCircuitsInContinuation() {
      var r =
          ReaderT.<Config, Boolean>asks(Config::secure)
              .flatMap(sec -> sec ? ReaderT.pure("https") : ReaderT.fail());
      assertThat(r.run(new Config("h", 80, true))).contains("https");
      assertThat(r.run(new Config("h", 80, false))).isEmpty();
    }

    @Test
    @DisplayName("local runs the computation with a modified environment")
    void localModifiesEnv() {
      var r =
          ReaderT.<Config, String>asks(Config::host)
              .local(cfg -> new Config(cfg.host().toUpperCase(), cfg.port(), cfg.secure()));
      assertThat(r.run(new Config("localhost", 80, false))).contains("LOCALHOST");
    }

    @Test
    @DisplayName("local on a failed ReaderT still returns empty")
    void localOnFailIsEmpty() {
      ReaderT<Config, String> r =
          ReaderT.<Config, String>fail().local(cfg -> new Config("other", 80, false));
      assertThat(r.run(new Config("localhost", 80, false))).isEmpty();
    }
  }

  // ── StateT ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("StateT<S, A>")
  class StateTTests {

    @Test
    @DisplayName("pure succeeds with the value and passes state unchanged")
    void purePreservesState() {
      var s = StateT.<Integer, String>pure("hello");
      var result = s.run(99);
      assertThat(result).isPresent();
      assertThat(result.get().value()).isEqualTo("hello");
      assertThat(result.get().state()).isEqualTo(99);
    }

    @Test
    @DisplayName("fail always returns Optional.empty()")
    void failReturnsEmpty() {
      StateT<Integer, String> s = StateT.fail();
      assertThat(s.run(0)).isEmpty();
      assertThat(s.run(100)).isEmpty();
    }

    @Test
    @DisplayName("get returns the current state as both value and next state")
    void getReturnsState() {
      var s = StateT.<Integer>get();
      var result = s.run(42);
      assertThat(result).isPresent();
      assertThat(result.get().value()).isEqualTo(42);
      assertThat(result.get().state()).isEqualTo(42);
    }

    @Test
    @DisplayName("put replaces the state and returns null as value")
    void putChangesState() {
      var s = StateT.<Integer>put(100);
      var result = s.run(0);
      assertThat(result).isPresent();
      assertThat(result.get().value()).isNull();
      assertThat(result.get().state()).isEqualTo(100);
    }

    @Test
    @DisplayName("modify applies a function to state and returns the new state as value")
    void modifyTransformsState() {
      var s = StateT.<Integer>modify(n -> n + 1);
      var result = s.run(5);
      assertThat(result).isPresent();
      assertThat(result.get().value()).isEqualTo(6);
      assertThat(result.get().state()).isEqualTo(6);
    }

    @Test
    @DisplayName("map transforms the value and threads state unchanged")
    void mapTransformsValue() {
      var s = StateT.<Integer>get().map(n -> "count=" + n);
      var result = s.run(7);
      assertThat(result).isPresent();
      assertThat(result.get().value()).isEqualTo("count=7");
      assertThat(result.get().state()).isEqualTo(7);
    }

    @Test
    @DisplayName("map on a failed StateT propagates empty")
    void mapOnFailPropagatesEmpty() {
      StateT<Integer, String> s = StateT.<Integer, String>fail().map(v -> v + "!");
      assertThat(s.run(0)).isEmpty();
    }

    @Test
    @DisplayName("flatMap threads state through two sequential computations")
    void flatMapThreadsState() {
      var s =
          StateT.<Integer>get()
              .flatMap(
                  n ->
                      StateT.<Integer>modify(x -> x + n)
                          .flatMap(newN -> StateT.pure("result=" + newN)));
      var result = s.run(3);
      assertThat(result).isPresent();
      assertThat(result.get().value()).isEqualTo("result=6");
      assertThat(result.get().state()).isEqualTo(6);
    }

    @Test
    @DisplayName("flatMap short-circuits when a step fails, discarding subsequent state changes")
    void flatMapShortCircuitsOnFail() {
      var s =
          StateT.<Integer>get()
              .flatMap(n -> n < 3 ? StateT.<Integer>modify(x -> x + 1) : StateT.fail());
      assertThat(s.run(2)).isPresent();
      assertThat(s.run(3)).isEmpty();
      assertThat(s.run(4)).isEmpty();
    }

    @Test
    @DisplayName("bounded counter increments up to max then fails")
    void boundedCounterDemo() {
      // Simulate three increments on a counter bounded at 3
      Function<Integer, StateT<Integer, String>> increment =
          max ->
              StateT.<Integer>get()
                  .flatMap(
                      n ->
                          n < max
                              ? StateT.<Integer>modify(x -> x + 1).map(s -> "tick " + s)
                              : StateT.fail());

      var step = increment.apply(3);
      assertThat(step.run(0)).isPresent(); // 0 < 3: ok
      assertThat(step.run(2)).isPresent(); // 2 < 3: ok
      assertThat(step.run(3)).isEmpty(); // 3 < 3 is false: fail
      assertThat(step.run(5)).isEmpty(); // 5 < 3 is false: fail
    }
  }
}
