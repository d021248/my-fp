package d021248.mytry.core.lang.adt.quantum;

import d021248.mytry.core.lang.adt.quantum.QuantumOp.CNot;
import d021248.mytry.core.lang.adt.quantum.QuantumOp.EnvironmentalNoise;
import d021248.mytry.core.lang.adt.quantum.QuantumOp.Hadamard;
import d021248.mytry.core.lang.adt.quantum.QuantumOp.Measure;
import d021248.mytry.core.lang.adt.quantum.QuantumOp.PhaseShift;
import d021248.mytry.core.lang.adt.quantum.QubitState.Decohered;
import d021248.mytry.core.lang.adt.quantum.QubitState.Deterministic;
import d021248.mytry.core.lang.adt.quantum.QubitState.Entangled;
import d021248.mytry.core.lang.adt.quantum.QubitState.Superposition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class QuantumSimulator {

  private final Map<Integer, QubitState> register = new HashMap<>();

  public QuantumSimulator(int numQubits) {
    for (int i = 0; i < numQubits; i++) {
      register.put(i, new Deterministic(false));
    }
  }

  public void executeCircuit(List<QuantumOp> circuit) {
    for (QuantumOp op : circuit) {
      applyOp(op);
    }
  }

  private void applyOp(QuantumOp op) {
    switch (op) {
      case Hadamard(int idx) ->
          register.put(
              idx,
              switch (register.get(idx)) {
                case Deterministic _ -> new Superposition(0.707, 0.707);
                case Superposition _ -> new Deterministic(true);
                case Entangled _ ->
                    new Decohered("Entangled qubits cannot be put into superposition directly");
                case Decohered d -> d;
              });

      case CNot(int control, int target) -> {
        switch (register.get(control)) {
          case Deterministic(boolean val) when val -> flipQubit(target);
          case Superposition _ -> {
            register.put(control, new Entangled(target, true));
            register.put(target, new Entangled(control, true));
            System.out.println("Qubit " + control + " and " + target + " ENTANGLED!");
          }
          default -> { // no action requiredfor non-deterministic qubits
          }
        }
      }

      case PhaseShift(int idx, double rad) -> {
        switch (register.get(idx)) {
          case Superposition s when Math.abs(rad - Math.PI) < 0.001 ->
              register.put(idx, new Superposition(s.amplitudeZero(), -s.amplitudeOne()));
          default -> { // no action requiredfor non-deterministic qubits
          }
        }
      }

      case Measure(int idx) -> {
        System.out.print("Measurement at qubit " + idx + ": ");
        switch (register.get(idx)) {
          case Deterministic(boolean val) -> System.out.println(val ? "1" : "0");
          case Superposition _ -> {
            boolean result = Math.random() > 0.5;
            register.put(idx, new Deterministic(result));
            System.out.println(
                result ? "1 (collapsed from superposition)" : "0 (collapsed from superposition)");
          }
          case Entangled(int partner, _) -> {
            // spooky action at a distance: measuring one qubit instantly collapses its
            // partner
            boolean result = Math.random() > 0.5;
            register.put(idx, new Deterministic(result));
            register.put(partner, new Deterministic(result));
            System.out.println(
                result ? "1 (entanglement collapsed!)" : "0 (entanglement collapsed!)");
          }
          case Decohered(String reason) -> System.out.println("ERROR: " + reason);
        }
      }

      case EnvironmentalNoise(double p) when p > 0.5 -> {
        for (Map.Entry<Integer, QubitState> entry : register.entrySet()) {
          switch (entry.getValue()) {
            case Superposition _ ->
                entry.setValue(new Decohered("Quantum decoherence due to thermal noise"));
            case Entangled _ ->
                entry.setValue(new Decohered("Quantum decoherence due to thermal noise"));
            default -> { // no action requiredfor non-deterministic qubits
            }
          }
        }
        System.out.println("System decohered! Quantum states were lost.");
      }

      case EnvironmentalNoise(_) -> { // no action requiredfor non-deterministic qubits
      }
    }
  }

  private void flipQubit(int idx) {
    switch (register.get(idx)) {
      case Deterministic(boolean val) -> register.put(idx, new Deterministic(!val));
      default -> { // no action requiredfor non-deterministic qubits
      }
    }
  }
}
