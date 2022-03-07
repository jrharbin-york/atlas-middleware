package fuzzingengine.operationparamsinfo;

import java.util.Random;

public class DoubleRangeOperationParameter extends DoubleOperationParameter {
	private double lower;
	private double upper;
	
	// TODO: random generator 
	private Random rng = new Random();

	public DoubleRangeOperationParameter(String name, double lower, double upper) {
		super(name);
		this.lower = lower;
		this.upper = upper;
	}

	public Object getSpecificValue() {
		return lower + (upper-lower) * rng.nextDouble();
	}
}
