package fuzzingengine.operations;

public abstract class FuzzingOperation {
	public abstract <E> E fuzzTransformEvent(E event);
}
