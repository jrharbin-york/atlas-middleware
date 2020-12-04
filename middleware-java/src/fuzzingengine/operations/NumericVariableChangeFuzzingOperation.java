package fuzzingengine.operations;

import java.util.Optional;
import java.util.Random;

import fuzzingengine.DoubleLambda;
import middleware.core.CARSVariableUpdate;

public class NumericVariableChangeFuzzingOperation extends FuzzingOperation {
	private static final boolean debugging = false;
	private String regexNumberScanner = "[+-]?([0-9]*\\.)?[0-9]+";
	
	private final double defaultFixedChange = 0.0;
	private String fixedChange = Double.toString(defaultFixedChange);
	private Optional<DoubleLambda> generateDouble = Optional.empty();
	
	public NumericVariableChangeFuzzingOperation(DoubleLambda generateDouble) {
		this.generateDouble = Optional.of(generateDouble);
	}
	
	public NumericVariableChangeFuzzingOperation(double fixedChange) {
		this.fixedChange = Double.toString(fixedChange);
	}
	
	public static NumericVariableChangeFuzzingOperation Random(double lower, double upper) {
		Random r = new Random();
		double diff = upper - lower;
		NumericVariableChangeFuzzingOperation op = new NumericVariableChangeFuzzingOperation(() -> lower + (diff * r.nextDouble()));
		return op;
	}

	public String getReplacement(String inValue) {
		return fixedChange;
	}

	public <E> E fuzzTransformEvent(E event) {
		if (event instanceof CARSVariableUpdate) {
			CARSVariableUpdate cvu = (CARSVariableUpdate)event;
			CARSVariableUpdate newUpdate = new CARSVariableUpdate(cvu);
			// TODO: handle checking the type of the update here - ensure we
			// only fuzz a standard operation
			
			// Need to be able to fuzz parts of a structural message
			String change = fixedChange;
			if (generateDouble.isPresent()) {
				DoubleLambda dl = generateDouble.get();
				change = Double.toString(dl.op());
			}
			newUpdate.setValue(change);
			return (E)newUpdate;
		} else {
			return event;
		}
	}
	
}
