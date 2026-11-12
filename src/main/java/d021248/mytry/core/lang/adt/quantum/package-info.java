/// Quantum computing simulation built on sealed algebraic data types.
///
/// A toy quantum simulator that models qubit states and gate operations using
/// Java's sealed interfaces and pattern matching — demonstrating how ADTs apply
/// naturally to physics simulation.
///
/// Key types:
///
/// - {@link d021248.mytry.core.lang.adt.quantum.QubitState} — sealed ADT representing
///   {@code |0⟩}, {@code |1⟩}, and superposition states
/// - {@link d021248.mytry.core.lang.adt.quantum.QuantumOp} — sealed ADT of quantum gate
///   operations (Hadamard, Pauli-X, CNOT, Measure …)
/// - {@link d021248.mytry.core.lang.adt.quantum.QuantumSimulator} — applies ops to qubit
///   states via exhaustive pattern matching
///
/// See {@link d021248.mytry.core.lang.adt.quantum.QuantumSimulatorDemo} for a full walkthrough.
///
/// @since 1.0
package d021248.mytry.core.lang.adt.quantum;
