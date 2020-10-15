package fuzzingengine;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;

import fuzzingengine.operations.FuzzingOperation;
import middleware.core.*;
import middleware.logging.ATLASLog;

public class FuzzingEngine {

	private HashMap<String, FuzzingOperation> fuzzingOperations = new LinkedHashMap<String, FuzzingOperation>();
	private HashMap<String, FuzzingOperation> fuzzingComponents = new LinkedHashMap<String, FuzzingOperation>();
	private HashMap<String, String> carsKeysReflection = new LinkedHashMap<String, String>();

	private FuzzingSelectionType fuzzSelType;

	public void addFuzzKey(String in, FuzzingOperation operation) {
		fuzzingOperations.put(in, operation);
	}
	
	public void setFuzzSelectionType(FuzzingSelectionType fuzzSelType) {
		this.fuzzSelType = fuzzSelType;
	}

	public FuzzingEngine() {
		fuzzSelType = FuzzingSelectionType.LOCAL_BY_SPECIFIC_KEYS;
	}

	public FuzzingEngine(String filename) {
		fuzzSelType = FuzzingSelectionType.LOCAL_BY_SPECIFIC_KEYS;

		// Format, key_in, key_out, operation_id,subfield_name
		// Generate an operation from the in/out things

		// Read this filename to generate the fuzzing engine
		// TODO: how to handle changing individual operations
	}

	public void addFuzzKey(String in, String out, FuzzingOperation operation) {
		carsKeysReflection.put(in, out);
		fuzzingOperations.put(in, operation);
	}
	
	public void addFuzzComponents(String componentName, FuzzingOperation operation) {
		fuzzingOperations.put(componentName, operation);
	}

	public void clearKeys() {
		fuzzingOperations.clear();
		carsKeysReflection.clear();
	}

	public <E> Optional<FuzzingOperation> shouldFuzzCARSEvent(E event) {
		ATLASLog.logFuzzing("shouldFuzzCARSEvent called - " + event);
		if (event instanceof CARSVariableUpdate) {
			CARSVariableUpdate cv = (CARSVariableUpdate) event;

			if (fuzzSelType == FuzzingSelectionType.LOCAL_BY_SPECIFIC_KEYS) {
				String k = cv.getKey();
				if (fuzzingOperations.containsKey(k)) {
					return Optional.of(fuzzingOperations.get(k));
				} else {
					return Optional.empty();
				}
			}
			
			if (fuzzSelType == FuzzingSelectionType.LOCAL_BY_SPECIFIC_SOURCE) {
				String componentName = cv.getSourceComponent();
				if (fuzzingComponents.containsKey(componentName)) {
					return Optional.of(fuzzingOperations.get(componentName));
				} else {
					return Optional.empty();
				}
			}
		}

		return Optional.empty();
	}

	public <E> Optional<String> shouldReflectBackToCARS(E event) {
		if (event instanceof CARSVariableUpdate) {
			CARSVariableUpdate cv = (CARSVariableUpdate) event;
			String k = cv.getKey();
			if (carsKeysReflection.containsKey(k)) {
				return Optional.of(carsKeysReflection.get(k));
			}
		}
		return Optional.empty();
	}

	public <E> E fuzzTransformEvent(E event, FuzzingOperation op) {
		return op.fuzzTransformEvent(event);
	}
}
