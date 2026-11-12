package d021248.mytry.core.lang.fp.lazy;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LazyTest {

  // -------------------------------------------------------------------------
  // Factory
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("of creates a Lazy that starts unevaluated")
  void ofStartsUnevaluated() {
    var lazy = Lazy.of(() -> 42);
    assertThat(lazy.isEvaluated()).isFalse();
  }

  @Test
  @DisplayName("eager creates a Lazy that is already evaluated")
  void eagerIsEvaluated() {
    var lazy = Lazy.eager("hello");
    assertThat(lazy.isEvaluated()).isTrue();
    assertThat(lazy.get()).isEqualTo("hello");
  }

  @Test
  @DisplayName("of rejects a null supplier")
  void ofRejectsNull() {
    assertThatNullPointerException().isThrownBy(() -> Lazy.of(null));
  }

  // -------------------------------------------------------------------------
  // Evaluation
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("get returns the supplier's value")
  void getReturnsSupplierValue() {
    assertThat(Lazy.of(() -> 99).get()).isEqualTo(99);
  }

  @Test
  @DisplayName("get marks the Lazy as evaluated")
  void getMarksAsEvaluated() {
    var lazy = Lazy.of(() -> "x");
    lazy.get();
    assertThat(lazy.isEvaluated()).isTrue();
  }

  @Test
  @DisplayName("supplier is called exactly once even when get is called multiple times")
  void supplierCalledOnce() {
    var calls = new AtomicInteger();
    var lazy =
        Lazy.of(
            () -> {
              calls.incrementAndGet();
              return "v";
            });
    lazy.get();
    lazy.get();
    lazy.get();
    assertThat(calls.get()).isEqualTo(1);
  }

  @Test
  @DisplayName("null result is evaluated exactly once and cached")
  void nullResultCached() {
    var calls = new AtomicInteger();
    var lazy =
        Lazy.<String>of(
            () -> {
              calls.incrementAndGet();
              return null;
            });
    assertThat(lazy.get()).isNull();
    assertThat(lazy.get()).isNull();
    assertThat(calls.get()).isEqualTo(1);
  }

  // -------------------------------------------------------------------------
  // Thread safety
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("supplier is called exactly once under concurrent access")
  void supplierCalledOnceUnderConcurrency() throws InterruptedException {
    var calls = new AtomicInteger();
    var lazy =
        Lazy.of(
            () -> {
              calls.incrementAndGet();
              return "shared";
            });
    var threads = 100;
    var latch = new CountDownLatch(1);
    var workers = new Thread[threads];

    for (var i = 0; i < threads; i++) {
      workers[i] =
          Thread.ofVirtual()
              .start(
                  () -> {
                    try {
                      latch.await();
                    } catch (InterruptedException _) {
                    }
                    lazy.get();
                  });
    }
    latch.countDown();
    for (var t : workers) t.join();

    assertThat(calls.get()).isEqualTo(1);
  }

  // -------------------------------------------------------------------------
  // map
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("map creates a new unevaluated Lazy")
  void mapCreatesNewUnevaluated() {
    var base = Lazy.of(() -> "hello");
    var mapped = base.map(String::toUpperCase);
    assertThat(mapped.isEvaluated()).isFalse();
  }

  @Test
  @DisplayName("map applies the function when evaluated")
  void mapAppliesFunction() {
    assertThat(Lazy.of(() -> "world").map(String::toUpperCase).get()).isEqualTo("WORLD");
  }

  @Test
  @DisplayName("map evaluates the base Lazy on first get")
  void mapEvaluatesBase() {
    var base = Lazy.of(() -> "base");
    var mapped = base.map(s -> s + "-mapped");
    mapped.get();
    assertThat(base.isEvaluated()).isTrue();
  }

  @Test
  @DisplayName("map rejects null function")
  void mapRejectsNull() {
    assertThatNullPointerException().isThrownBy(() -> Lazy.of(() -> "x").map(null));
  }

  // -------------------------------------------------------------------------
  // flatMap
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("flatMap chains two Lazy computations")
  void flatMapChains() {
    var result = Lazy.of(() -> "hello").flatMap(s -> Lazy.of(() -> s.length())).get();
    assertThat(result).isEqualTo(5);
  }

  @Test
  @DisplayName("flatMap creates a new unevaluated Lazy")
  void flatMapCreatesUnevaluated() {
    var chained = Lazy.of(() -> "x").flatMap(s -> Lazy.of(() -> s.toUpperCase()));
    assertThat(chained.isEvaluated()).isFalse();
  }

  @Test
  @DisplayName("flatMap evaluates the base and intermediate Lazy on first get")
  void flatMapEvaluatesBothLazies() {
    var base = Lazy.of(() -> "base");
    var chained = base.flatMap(s -> Lazy.of(() -> s + "-chained"));
    chained.get();
    assertThat(base.isEvaluated()).isTrue();
  }

  @Test
  @DisplayName("flatMap rejects null function")
  void flatMapRejectsNull() {
    assertThatNullPointerException().isThrownBy(() -> Lazy.of(() -> "x").flatMap(null));
  }

  @Test
  @DisplayName("flatMap does not nest Lazy — avoids Lazy<Lazy<T>>")
  void flatMapAvoidsNesting() {
    Lazy<String> inner = Lazy.eager("inner");
    var flat = Lazy.of(() -> "outer").flatMap(_ -> inner);
    assertThat(flat.get()).isEqualTo("inner");
  }

  // -------------------------------------------------------------------------
  // Monad laws
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Monad left identity: Lazy.eager(a).flatMap(f).get() == f(a).get()")
  void monadLeftIdentity() {
    Function<Integer, Lazy<String>> f = n -> Lazy.eager("v" + n);
    assertThat(Lazy.eager(42).flatMap(f).get()).isEqualTo(f.apply(42).get());
  }

  @Test
  @DisplayName("Monad right identity: lazy.flatMap(Lazy::eager).get() == lazy.get()")
  void monadRightIdentity() {
    var lazy = Lazy.of(() -> "hello");
    assertThat(lazy.flatMap(Lazy::eager).get()).isEqualTo(lazy.get());
  }

  @Test
  @DisplayName("Monad associativity: flatMap(f).flatMap(g) == flatMap(x -> f(x).flatMap(g))")
  void monadAssociativity() {
    var lazy = Lazy.of(() -> 3);
    Function<Integer, Lazy<Integer>> f = n -> Lazy.eager(n * 2);
    Function<Integer, Lazy<Integer>> g = n -> Lazy.eager(n + 10);

    var left = lazy.flatMap(f).flatMap(g).get();

    Function<Integer, Lazy<Integer>> composed = x -> f.apply(x).flatMap(g);
    var right = lazy.flatMap(composed).get();
    assertThat(left).isEqualTo(right);
  }

  // -------------------------------------------------------------------------
  // toString
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("toString shows unevaluated label before evaluation")
  void toStringUnevaluated() {
    assertThat(Lazy.of(() -> "x").toString()).isEqualTo("Lazy[unevaluated]");
  }

  @Test
  @DisplayName("toString shows the value after evaluation")
  void toStringEvaluated() {
    var lazy = Lazy.of(() -> "hello");
    lazy.get();
    assertThat(lazy.toString()).isEqualTo("Lazy[hello]");
  }
}
