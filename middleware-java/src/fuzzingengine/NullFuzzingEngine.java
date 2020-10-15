package fuzzingengine;

public class NullFuzzingEngine extends FuzzingEngine {
	public <E> E fuzzTransformEvent(E event) {
		return event;
	}
}