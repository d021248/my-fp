/// # Church Encodings and Y Combinator
///
/// Represents booleans, natural numbers, and pairs as pure higher-order functions —
/// the original Church encodings from untyped lambda calculus.
///
/// ## Key types
///
/// - `Church.Bool` — selects one of two branches; `TRUE` returns the first, `FALSE` the second
/// - `Church.Num` — n-fold application of a successor function to a seed value
/// - `Church.Pair` — applies a selector function to its two stored components
/// - `Church.Y` — fixed-point combinator enabling anonymous recursion without self-reference
///
/// ## Why Church encodings?
///
/// Church encodings show that data and behaviour are interchangeable: a boolean *is* an
/// if-expression, a number *is* a loop. This insight underpins the lambda calculus and
/// influences every functional language.
///
/// @since 1.0
/// @see d021248.mytry.core.lang.fp.trampoline
/// @see d021248.mytry.core.lang.fp.cps
package d021248.mytry.core.lang.fp.church;
