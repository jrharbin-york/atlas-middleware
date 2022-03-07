package fuzzingengine.operationparamsinfo;

public class DoubleConstantOperationParameter extends DoubleOperationParameter {
	private double value;

	public DoubleConstantOperationParameter(String name, double value) {
		super(name);
		this.value = value;
	}

	Object getSpecificValue() {
		return value;
	}

}