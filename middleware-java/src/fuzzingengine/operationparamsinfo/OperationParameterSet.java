package fuzzingengine.operationparamsinfo;

import java.util.ArrayList;
import java.util.List;


public class OperationParameterSet {
	private String name;
	private String operationClassName;
	List<OperationParameter> params = new ArrayList<OperationParameter>();

	public OperationParameterSet(String name, List<OperationParameter> params, String operationClassName) {
		this.name = name;
		this.params = params;
		this.operationClassName = operationClassName;
	}

	public OperationParameterSet(String name, String operationClassName) {
		this.name = name;
		this.operationClassName = operationClassName;
	}

	public void addParameter(OperationParameter p) {
		params.add(p);
	}

	public List<Object> generateSpecific() {
		List<Object> res = new ArrayList<Object>();
		for (OperationParameter p : params) {
			res.add(p.getSpecificValue());
		}
		return res;
	}
	
	public String getOperationClassName() {
		return operationClassName;
	}
}
