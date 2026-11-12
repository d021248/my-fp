/// Algebraic Data Types (ADTs) in Java — sealed interfaces + records.
///
/// An ADT is a type defined by its *variants*. In Haskell, Rust, or Scala, ADTs
/// are a first-class language feature (`data`, `enum`, `sealed trait`). Since
/// Java 17 (sealed) + Java 16 (records) + Java 21 (pattern matching), Java can
/// express them idiomatically.
///
/// ## What makes an ADT?
///
/// 1. A **sealed interface** enumerates all variants — the compiler knows the
///    full set and can check exhaustiveness in `switch`.
/// 2. Each variant is a **record** — immutable, structural equality, pattern
///    deconstructable.
/// 3. **Pattern matching** (`switch` expressions with record patterns) replaces
///    the visitor pattern for ad-hoc operations.
///
/// ## Key types
///
/// - {@link d021248.mytry.core.lang.adt.JsonValue} — JSON modelled as a
///     recursive ADT with six variants
/// - {@link d021248.mytry.core.lang.adt.Either} — classic Left/Right sum type
///     with map, flatMap, fold
///
/// ## Comparison to OOP
///
/// | Concern | OOP (Visitor) | ADT (sealed + switch) |
/// |---------|---------------|----------------------|
/// | Adding a variant | modify every Visitor | compiler flags every incomplete switch |
/// | Adding an operation | add a Visitor subclass | add a switch expression |
/// | Exhaustiveness | runtime (or lint) | **compile-time** |
/// | Boilerplate | accept(), visit() per variant | zero — records + switch |
///
/// @see d021248.mytry.core.lang.pattern
/// @see d021248.mytry.core.lang.record
///
/// @since 1.0
package d021248.mytry.core.lang.adt;
