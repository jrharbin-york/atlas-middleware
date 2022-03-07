package fuzzingengine.operationparamsinfo;

public class StringConstantOperationParameter extends StringOperationParameter {
	private String value;
	
	public StringConstantOperationParameter(String name, String value) {
		super(name);
		this.value = value;
	}
	
	public Object getSpecificValue() {
		return value;
	}
}
