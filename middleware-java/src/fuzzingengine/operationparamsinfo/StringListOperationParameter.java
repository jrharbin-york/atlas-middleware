package fuzzingengine.operationparamsinfo;

import java.util.ArrayList;
import java.util.List;

public class StringListOperationParameter extends StringOperationParameter {
	private List<String> values = new ArrayList<String>();

	StringListOperationParameter(String name) {
		super(name);
	}
	
	StringListOperationParameter(String name, List<String> values) {
		super(name);
		this.values = values;
	}

	public Object getSpecificValue() {
		// TODO: random
		return values.get(0);
	}
}
