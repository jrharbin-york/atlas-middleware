package fuzzingengine.operationparamsinfo;

import java.util.ArrayList;
import java.util.List;

public class DoubleListOperationParameter extends DoubleOperationParameter {
	List<Double> values = new ArrayList<Double>();

	DoubleListOperationParameter(String name, List<Double> values) {
		super(name);
		this.values = values;
	}

	// TODO: random
	Object getSpecificValue() {
		return values.get(0);
	}
}
