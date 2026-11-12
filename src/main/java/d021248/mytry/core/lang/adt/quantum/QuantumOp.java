package d021248.mytry.core.lang.adt.quantum;

import d021248.mytry.core.lang.adt.quantum.QuantumOp.CNot;
import d021248.mytry.core.lang.adt.quantum.QuantumOp.EnvironmentalNoise;
import d021248.mytry.core.lang.adt.quantum.QuantumOp.Hadamard;
import d021248.mytry.core.lang.adt.quantum.QuantumOp.Measure;
import d021248.mytry.core.lang.adt.quantum.QuantumOp.PhaseShift;

/// Sum type for all operations in a quantum circuit.
///
/// Each variant is a distinct gate or noise model.  The sealed constraint
/// guarantees that every `switch` over a `QuantumOp` is exhaustive.
public sealed interface QuantumOp permits Hadamard, CNot, PhaseShift, Measure, EnvironmentalNoise {

  /// Hadamard gate — puts a qubit into superposition (`|0⟩` and `|1⟩` simultaneously).
  record Hadamard(int qubitIndex) implements QuantumOp {}

  /// Controlled-NOT gate — entangles two qubits; target flips only when control is `|1⟩`.
  record CNot(int controlQubit, int targetQubit) implements QuantumOp {}

  /// Phase-shift gate — rotates the quantum phase by the given angle in radians.
  record PhaseShift(int qubitIndex, double radians) implements QuantumOp {}

  /// Measurement — collapses superposition to a classical bit (`0` or `1`).
  record Measure(int qubitIndex) implements QuantumOp {}

  /// Environmental noise — random phase error with probability `errorProbability`.
  record EnvironmentalNoise(double errorProbability) implements QuantumOp {}
}
