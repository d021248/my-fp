package d021248.mytry.core.lang.adt.quantum;

import d021248.mytry.core.lang.adt.quantum.QuantumOp.CNot;
import d021248.mytry.core.lang.adt.quantum.QuantumOp.EnvironmentalNoise;
import d021248.mytry.core.lang.adt.quantum.QuantumOp.Hadamard;
import d021248.mytry.core.lang.adt.quantum.QuantumOp.Measure;
import java.util.List;

public final class QuantumSimulatorDemo {
  public static void main() {
    QuantumSimulator simulator = new QuantumSimulator(2);

    // Build the circuit declaratively using our ADT sum type
    List<QuantumOp> quantumCircuit =
        List.of(
            new Hadamard(0), // 1. Put qubit 0 into superposition
            new CNot(0, 1), // 2. Entangle qubit 0 with qubit 1
            new EnvironmentalNoise(0.55), // 3. EXOTIC: induce strong noise (p = 55%)
            new Measure(0), // 4. Measure qubit 0
            new Measure(1) // 5. Measure qubit 1 (to observe the effect)
            );

    System.out.println("Starting quantum computer simulation...\n");
    simulator.executeCircuit(quantumCircuit);
  }
}
