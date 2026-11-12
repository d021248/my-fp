/// Applicative functor — the layer between Functor and Monad.
///
/// ## The hierarchy
///
/// | Level | Key operation | Composition |
/// |-------|--------------|-------------|
/// | Functor | `map` | Transform a value |
/// | **Applicative** | `ap`, `liftA2` | Independent effects |
/// | Monad | `flatMap` | Dependent effects |
///
/// The key insight: **Applicative enables independent composition.**  Two
/// `Validated` checks can be run simultaneously and their errors accumulated;
/// two `Optional` look-ups can be combined without caring about order.  A
/// Monad (`flatMap`) must be sequential because each step can depend on the
/// previous result.
///
/// ## Key types
///
/// - [Validated] — accumulating validation; `ap` merges error lists from both sides
///
/// ## Relationship to `errorhandling/validation`
///
/// [d021248.mytry.core.lang.errorhandling.validation.Validation] is the
/// Java-idiomatic counterpart of [Validated]. It provides the same
/// error-accumulation behaviour with a practical API (`zip`, `toResult()`,
/// `ifValid`/`ifInvalid`) instead of the theory-first `ap`/`liftA2` names.
/// Use [Validated] to understand the Applicative abstraction;
/// use `Validation` for production code.
///
/// ## Sub-packages
///
/// - [d021248.mytry.core.lang.fp.applicative.broken] — early-exit
///   validation that hides errors after the first failure
///
/// @since 1.0
package d021248.mytry.core.lang.fp.applicative;
