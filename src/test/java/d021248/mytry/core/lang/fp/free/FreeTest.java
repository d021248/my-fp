package d021248.mytry.core.lang.fp.free;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FreeTest {

  // ── helpers ───────────────────────────────────────────────────────────────

  private static <A> A run(Free<A> p, Map<String, String> store) {
    return Free.run(p, Free.mapInterpreter(store));
  }

  // ── monad laws ────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Monad left identity: pure(a).flatMap(f) == f(a)")
  void monadLeftIdentity() {
    Function<String, Free<Integer>> f = s -> Free.pure(s.length());
    var store = new HashMap<String, String>();
    assertThat(run(Free.<String>pure("hello").flatMap(f), store))
        .isEqualTo(run(f.apply("hello"), store));
  }

  @Test
  @DisplayName("Monad right identity: m.flatMap(Free::pure) == m")
  void monadRightIdentity() {
    Free<Optional<String>> m = Free.get("x");
    Free<Optional<String>> bound = m.flatMap(v -> Free.pure(v));
    var s1 = new HashMap<>(Map.of("x", "v"));
    var s2 = new HashMap<>(Map.of("x", "v"));
    assertThat(run(bound, s1)).isEqualTo(run(m, s2));
  }

  @Test
  @DisplayName("Monad associativity: m.flatMap(f).flatMap(g) == m.flatMap(x -> f(x).flatMap(g))")
  void monadAssociativity() {
    var m = Free.get("n");
    Function<Optional<String>, Free<Integer>> f =
        opt -> Free.pure(opt.map(Integer::parseInt).orElse(0));
    Function<Integer, Free<Integer>> g = n -> Free.pure(n * 2);

    var left = m.flatMap(f).flatMap(g);
    Function<Optional<String>, Free<Integer>> fg = x -> f.apply(x).flatMap(g);
    var right = m.flatMap(fg);

    var base = Map.of("n", "5");
    assertThat(run(left, new HashMap<>(base))).isEqualTo(run(right, new HashMap<>(base)));
  }

  // ── store operations ──────────────────────────────────────────────────────

  @Test
  @DisplayName("put followed by get returns the written value")
  void putThenGet() {
    var program = Free.put("name", "Alice").andThen(Free.get("name"));
    var store = new HashMap<String, String>();
    assertThat(run(program, store)).contains("Alice");
    assertThat(store).containsEntry("name", "Alice");
  }

  @Test
  @DisplayName("get on a missing key returns Optional.empty()")
  void getMissingKey() {
    var store = new HashMap<String, String>();
    assertThat(run(Free.get("missing"), store)).isEmpty();
  }

  @Test
  @DisplayName("delete removes the key; subsequent get returns empty")
  void deleteRemovesKey() {
    var store = new HashMap<>(Map.of("tmp", "123"));
    var program = Free.delete("tmp").andThen(Free.get("tmp"));
    assertThat(run(program, store)).isEmpty();
    assertThat(store).doesNotContainKey("tmp");
  }

  @Test
  @DisplayName("programs compose with andThen across multiple puts")
  void multiPutComposition() {
    var program =
        Free.put("a", "1")
            .andThen(Free.put("b", "2"))
            .andThen(Free.put("c", "3"))
            .andThen(Free.get("b"));
    var store = new HashMap<String, String>();
    assertThat(run(program, store)).contains("2");
    assertThat(store).hasSize(3);
  }

  @Test
  @DisplayName("flatMap can branch on the read value")
  void flatMapConditionalBranch() {
    var program =
        Free.get("flag")
            .flatMap(
                flag ->
                    flag.isPresent() ? Free.put("result", "found") : Free.put("result", "missing"))
            .andThen(Free.get("result"));

    var withFlag = new HashMap<>(Map.of("flag", "yes"));
    var withoutFlag = new HashMap<String, String>();

    assertThat(run(program, withFlag)).contains("found");
    assertThat(run(program, withoutFlag)).contains("missing");
  }

  // ── interpreters ──────────────────────────────────────────────────────────

  @Test
  @DisplayName("logging interpreter records get, put, delete in order")
  void loggingInterpreterRecordsOps() {
    var store = new HashMap<String, String>();
    var log = new ArrayList<String>();
    var program = Free.put("x", "1").andThen(Free.get("x")).andThen(Free.delete("x"));
    Free.run(program, Free.loggingInterpreter(Free.mapInterpreter(store), log));
    assertThat(log).containsExactly("put(x, 1)", "get(x)", "delete(x)");
  }

  @Test
  @DisplayName("dry-run interpreter reads from the snapshot but does not mutate it")
  void dryRunDoesNotMutate() {
    var snapshot = Map.of("key", "original");
    var program = Free.put("key", "changed").andThen(Free.get("key"));
    var result = Free.run(program, Free.dryRunInterpreter(snapshot));
    assertThat(result).contains("original"); // write was ignored; read sees snapshot
    assertThat(snapshot).containsEntry("key", "original");
  }

  @Test
  @DisplayName("runFresh convenience method returns result and final store together")
  void runFreshReturnsStoreState() {
    var entry = Free.put("x", "42").andThen(Free.get("x")).runFresh();
    assertThat(entry.value()).contains("42");
    assertThat(entry.store()).containsEntry("x", "42");
  }

  // ── demo program ──────────────────────────────────────────────────────────

  @Test
  @DisplayName("demo program produces the expected final value")
  void demoProgramResult() {
    var entry = FreeDemo.buildProgram().runFresh();
    assertThat(entry.value()).contains("Java 25");
  }
}
