/// Comonads: Store and Env — the categorical dual of monads.
///
/// While a monad provides `pure` (inject a value) and `flatMap` (sequence),
/// a comonad provides `extract` (get a value) and `extend` (context-sensitive
/// transform).
///
/// | Comonad | Models |
/// |---------|--------|
/// | `Store<S,A>` | Focusable lookup: getter `S → A` plus a current position |
/// | `Env<E,A>` | A value `A` carrying a read-only environment `E` alongside it |
///
/// ## Comonad laws (both types satisfy all three)
///
/// - `w.extend(w -> w.extract()) == w` — extend with extract is a no-op
/// - `w.extend(f).extract() == f(w)` — extract after extend returns `f(w)`
/// - `w.extend(f).extend(g) == w.extend(w -> g(w.extend(f)))` — composition
///
/// ## Sub-packages
///
/// - {@link d021248.mytry.core.lang.fp.comonad.broken} — comparison: mutable
///   context and imperative loops
///
/// @since 1.0
package d021248.mytry.core.lang.fp.comonad;
