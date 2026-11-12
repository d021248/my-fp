package d021248.mytry.core.lang.fp.church;

import module java.base;

/// Church encodings of booleans, natural numbers, pairs, and the Y combinator.
///
/// Every value is represented as a pure function. Arithmetic, branching, and
/// even recursion need no language primitives beyond function application.
///
/// ## Usage
///
/// ```java
/// import static d021248.mytry.core.lang.fp.church.Church.*;
///
/// var two   = fromInt(2);
/// var three = fromInt(3);
/// System.out.println(toInt(add(two, three)));       // 5
/// System.out.println(toInt(mul(two, three)));       // 6
///
/// var factorial = Y(f -> n -> n <= 1 ? 1 : n * f.apply(n - 1));
/// System.out.println(factorial.apply(5));           // 120
/// ```
public final class Church {

  private Church() {}

  // -------------------------------------------------------------------------
  // Church Booleans
  // -------------------------------------------------------------------------

  /// A Church-encoded boolean: a two-branch selector.
  ///
  /// `TRUE` returns the first argument; `FALSE` returns the second.
  @FunctionalInterface
  public interface Bool {
    Object select(Object ifTrue, Object ifFalse);
  }

  /// The Church encoding of `true`: returns the first (then) branch.
  public static final Bool TRUE = (t, f) -> t;

  /// The Church encoding of `false`: returns the second (else) branch.
  public static final Bool FALSE = (t, f) -> f;

  /// Logical AND of two Church booleans.
  public static Bool and(Bool a, Bool b) {
    return (t, f) -> a.select(b.select(t, f), f);
  }

  /// Logical OR of two Church booleans.
  public static Bool or(Bool a, Bool b) {
    return (t, f) -> a.select(t, b.select(t, f));
  }

  /// Logical NOT of a Church boolean.
  public static Bool not(Bool b) {
    return (t, f) -> b.select(f, t);
  }

  /// Converts a Church boolean to a Java `boolean`.
  public static boolean toBoolean(Bool b) {
    return (boolean) b.select(true, false);
  }

  /// Lifts a Java `boolean` into a Church boolean.
  public static Bool fromBoolean(boolean v) {
    return v ? TRUE : FALSE;
  }

  // -------------------------------------------------------------------------
  // Church Numerals
  // -------------------------------------------------------------------------

  /// A Church numeral: n-fold application of a successor function to a seed.
  ///
  /// `ZERO.apply(s).applyAsInt(z)` returns `z`.
  /// `fromInt(3).apply(s).applyAsInt(z)` returns `s(s(s(z)))`.
  @FunctionalInterface
  public interface Num {
    IntUnaryOperator apply(IntUnaryOperator successor);
  }

  /// The Church numeral for zero: applies the successor function zero times.
  public static final Num ZERO = s -> z -> z;

  /// Returns the successor of a Church numeral.
  public static Num succ(Num n) {
    return s -> z -> s.applyAsInt(n.apply(s).applyAsInt(z));
  }

  /// Adds two Church numerals: applies `s` m+n times to `z`.
  public static Num add(Num m, Num n) {
    return s -> z -> m.apply(s).applyAsInt(n.apply(s).applyAsInt(z));
  }

  /// Multiplies two Church numerals: applies `m.apply(s)` exactly `n` times.
  public static Num mul(Num m, Num n) {
    return s -> n.apply(m.apply(s));
  }

  /// Converts a Church numeral to a Java `int` by counting successor applications.
  public static int toInt(Num n) {
    return n.apply(z -> z + 1).applyAsInt(0);
  }

  /// Lifts a non-negative Java `int` into a Church numeral.
  public static Num fromInt(int k) {
    var acc = ZERO;
    for (var i = 0; i < k; i++) acc = succ(acc);
    return acc;
  }

  /// Returns `true` if the Church numeral represents zero.
  ///
  /// Uses a Church-faithful encoding: applies a constant-1 successor to a
  /// base of 0.  For n=0 no application occurs, yielding 0 (false);
  /// for n>0 at least one application returns 1 (true).  Avoids the
  /// full summation of `toInt`.
  public static boolean isZero(Num n) {
    return n.apply(_ -> 1).applyAsInt(0) == 0;
  }

  /// Returns the predecessor of a Church numeral; `pred(ZERO) == ZERO`.
  ///
  /// Pure Church `pred` requires a polymorphic numeral `∀A.(A→A)→A→A`,
  /// which Java's type-erased generics cannot encode directly with the
  /// `Num` interface.  This implementation uses the pair-step trick via
  /// a two-element array: `(a,b) → (b,b+1)`, starting from `(0,0)`;
  /// after n steps the first element is `max(0, n-1)`.
  public static Num pred(Num n) {
    int[] pair = {0, 0};
    n.apply(
            x -> {
              pair[0] = pair[1];
              pair[1]++;
              return x + 1;
            })
        .applyAsInt(0);
    return fromInt(pair[0]);
  }

  // -------------------------------------------------------------------------
  // Church Pairs
  // -------------------------------------------------------------------------

  /// A Church-encoded pair: applies a selector to its two components.
  @FunctionalInterface
  public interface Pair {
    Object apply(BiFunction<Object, Object, Object> selector);
  }

  /// Constructs a Church pair holding `fst` and `snd`.
  public static Pair pair(Object fst, Object snd) {
    return f -> f.apply(fst, snd);
  }

  /// Extracts the first component of a Church pair.
  public static Object fst(Pair p) {
    return p.apply((a, _) -> a);
  }

  /// Extracts the second component of a Church pair.
  public static Object snd(Pair p) {
    return p.apply((_, b) -> b);
  }

  // -------------------------------------------------------------------------
  // Y Combinator
  // -------------------------------------------------------------------------

  /// Helper type that breaks the self-reference cycle required by the Y combinator.
  @FunctionalInterface
  private interface Recursive<A, B> {
    B apply(Recursive<A, B> self, A a);
  }

  /// The Y (fixed-point) combinator: derives a recursive function from a non-recursive
  /// one-step description, without any self-reference in the lambda body.
  ///
  /// ```java
  /// var factorial = Y(f -> n -> n <= 1 ? 1 : n * f.apply(n - 1));
  /// ```
  public static <A, B> Function<A, B> Y(Function<Function<A, B>, Function<A, B>> f) {
    Recursive<A, B> step = (self, a) -> f.apply(arg -> self.apply(self, arg)).apply(a);
    return a -> step.apply(step, a);
  }

  // -------------------------------------------------------------------------
  // Church Maybe
  // -------------------------------------------------------------------------

  /// A Church-encoded optional value: two continuations — nothing or just.
  ///
  /// `NOTHING` applies the first (default) argument; `just(v)` applies the
  /// second (just-branch) function to `v`.  No `null` or wrapper object is
  /// needed — the encoding itself *is* the case analysis.
  @FunctionalInterface
  public interface Maybe {
    Object apply(Object nothing, UnaryOperator<Object> just);
  }

  /// The Church encoding of an absent value.
  public static final Maybe NOTHING = (n, _) -> n;

  /// The Church encoding of a present value `v`.
  public static Maybe just(Object v) {
    return (_, j) -> j.apply(v);
  }

  /// Extracts the value from a Church Maybe, returning `defaultVal` if nothing.
  @SuppressWarnings("unchecked")
  public static <A> A fromMaybe(A defaultVal, Maybe m) {
    return (A) m.apply(defaultVal, x -> x);
  }

  // -------------------------------------------------------------------------
  // Church Either
  // -------------------------------------------------------------------------

  /// A Church-encoded sum type with two branches: left and right.
  ///
  /// `left(v)` applies the left-branch function to `v`;
  /// `right(v)` applies the right-branch function to `v`.
  @FunctionalInterface
  public interface ChurchEither {
    Object apply(UnaryOperator<Object> left, UnaryOperator<Object> right);
  }

  /// The Church encoding of a `left` value.
  public static ChurchEither churchLeft(Object v) {
    return (l, _) -> l.apply(v);
  }

  /// The Church encoding of a `right` value.
  public static ChurchEither churchRight(Object v) {
    return (_, r) -> r.apply(v);
  }

  /// Folds a Church Either by applying the appropriate branch function.
  @SuppressWarnings("unchecked")
  public static <A> A foldEither(
      Function<Object, A> left, Function<Object, A> right, ChurchEither e) {
    return (A) e.apply(left::apply, right::apply);
  }
}
