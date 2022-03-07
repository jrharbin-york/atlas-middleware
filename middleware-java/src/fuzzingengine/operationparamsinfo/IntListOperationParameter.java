package fuzzingengine.operationparamsinfo;

import java.util.ArrayList;
import java.util.List;

public class IntListOperationParameter extends IntOperationParameter {
	List<Integer> values = new ArrayList<Integer>();
	
	IntListOperationParameter(String name) {
		super(name);
	}

	public Object getSpecificValue() {
		// TODO: random
		return values.get(0);
	}
}
