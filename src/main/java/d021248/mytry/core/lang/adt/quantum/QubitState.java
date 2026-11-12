package d021248.mytry.core.lang.adt.quantum;

import d021248.mytry.core.lang.adt.quantum.QubitState.Decohered;
import d021248.mytry.core.lang.adt.quantum.QubitState.Deterministic;
import d021248.mytry.core.lang.adt.quantum.QubitState.Entangled;
import d021248.mytry.core.lang.adt.quantum.QubitState.Superposition;

public sealed interface QubitState permits Deterministic, Superposition, Entangled, Decohered {

  public record Deterministic(boolean value) implements QubitState {}

  public record Superposition(double amplitudeZero, double amplitudeOne) implements QubitState {}

  public record Entangled(int pairedWithQubit, boolean correlated) implements QubitState {}

  public record Decohered(String reason) implements QubitState {}
}
