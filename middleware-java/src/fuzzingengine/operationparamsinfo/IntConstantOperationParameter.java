package fuzzingengine.operationparamsinfo;

public class IntConstantOperationParameter extends IntOperationParameter {
	private int value;
	
	public IntConstantOperationParameter(String name, int value) {
		super(name);
		this.value = value;
	}

	public Object getSpecificValue() {
		return value;
	}
}
