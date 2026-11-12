package d021248.mytry.core.lang.fp.free;

import module java.base;

/// The functor for a minimal key-value store DSL.
///
/// Each variant holds the operation's arguments **plus** a continuation `next`
/// (type parameter `A`) describing what to do after the operation completes.
/// This "continuation in the functor" is the standard way to lift a DSL into
/// a Free monad in languages without higher-kinded types.
///
/// Only `StoreOp` knows about the store protocol.  `Free` knows nothing about
/// it — the two concerns are fully separated.
sealed interface StoreOp<A> permits StoreOp.Get, StoreOp.Put, StoreOp.Delete {

  /// Reads `key`; the continuation receives `Optional.empty()` on a miss.
  record Get<A>(String key, Function<Optional<String>, A> next) implements StoreOp<A> {}

  /// Writes `value` at `key`, then proceeds with `next`.
  record Put<A>(String key, String value, A next) implements StoreOp<A> {}

  /// Removes `key`, then proceeds with `next`.
  record Delete<A>(String key, A next) implements StoreOp<A> {}
}
