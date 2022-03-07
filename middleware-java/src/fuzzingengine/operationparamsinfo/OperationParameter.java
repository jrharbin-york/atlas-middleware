package fuzzingengine.operationparamsinfo;

public abstract class OperationParameter {
	private String name;
	abstract Object getSpecificValue(); 
	
	OperationParameter(String name) {
		this.name = name;
	}
}
