package fuzzingengine.operationparamsinfo;

import java.util.Random;

public class IntRangeOperationParameter extends IntOperationParameter {
	private int lower;
	private int upper;
	
	// TODO: centralised random generator 
	private Random rng = new Random();
	
	public IntRangeOperationParameter(String name, int lower, int upper) {
		super(name);
		this.lower = lower;
		this.upper = upper;
	}

	public Object getSpecificValue() {
		return lower + rng.nextInt(upper - lower);
	}
}
