package fuzzingengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import fuzzingengine.operationparamsinfo.*;

// Stores all the information for a simulator mapping
public class FuzzingSimMapping {

	public enum VariableDirection {
		INBOUND,
		OUTBOUND
	}
	
	public enum FuzzingNature {
		BINARY,
		CUSTOM_CONFIG,
		NO_MODIFICATIONS
	}
	

	
	public class OpParamSetType {
		private OperationParameterSet opset;
		private String subSpec;
		
		// This gives for a variable: the operation parameter set, and the subspec part of the variable for this operation 
		OpParamSetType(OperationParameterSet opset, String subSpec) {
			this.opset = opset;
			this.subSpec = subSpec;
		}
		
		public OperationParameterSet getOpset() {
			return opset;
		}
		
		public String getSubSpec() {
			return subSpec;
		}
	}
	
	public class VariableSpecification {
		private String component;
		private String variable;
		private String reflectionName;
		private VariableDirection dir;
		private Optional<String> binaryPath;
		private Optional<String> regexp;
		private boolean vehicleSpecific;
		private Optional<TimeSpec> timeSpec;
		private Optional<Double> fuzzProb;
		
		private List<OpParamSetType> opInfo = new ArrayList<OpParamSetType>();
		
		public VariableSpecification(String component, String variable, String reflectionName, VariableDirection dir, Optional<String> binaryPath, Optional<String> regexp, boolean vehicleSpecific, Optional<TimeSpec> timeSpec, Optional<Double> fuzzProb) {
			this.component = component;
			this.variable = variable;
			this.reflectionName = reflectionName;
			this.dir = dir;
			this.binaryPath = binaryPath;
			this.regexp = regexp;
			this.vehicleSpecific = vehicleSpecific;
			this.timeSpec = timeSpec;
			this.fuzzProb = fuzzProb;
		}
		
		public Optional<String> getComponent() {
			return Optional.of(component);
		}
		
		public Optional<TimeSpec> getTimeSpec() {
			return timeSpec;
		}
		
		public Optional<Double> getFuzzProb() {
			return fuzzProb;
		}
		
		public void addOperationParameterSet(OperationParameterSet op, String subSpec) {
			opInfo.add(new OpParamSetType(op, subSpec));
		}
		
		public String getVariable() {
			return variable;
		}
		
		public Optional<String> getPath() {
			return binaryPath;
		}
		
		public Optional<String> getReflectionName_opt() {
			return Optional.of(reflectionName);
		}
		
		public Optional<String> getRegexp() {
			return regexp;
		}
		
		public String getReflectionName() {
			return reflectionName;
		}
		
		public boolean isVehicleSpecific() {
			return vehicleSpecific;
		}
		
		public List<OpParamSetType> getOperationParamSets() {
			return opInfo;
		}
	}
	
	private class FuzzingComponentNatureInfo {
		private String name;
		private FuzzingNature nature;
		private Optional<String> classNameString;
		private Optional<String> fullPath;
		private boolean isEnvironmental;
		
		public FuzzingComponentNatureInfo(String name, FuzzingNature nature, Optional<String> classNameString, Optional<String> fullPath, boolean isEnvironmental) {
			this.name = name;
			this.nature = nature;
			this.classNameString = classNameString;
			this.fullPath = fullPath;
			this.isEnvironmental = isEnvironmental;
		}
	}
	
	// Maps the component name to variable specification
	private HashMap<String, List<VariableSpecification>> records = new HashMap<String,List<VariableSpecification>>();
	// Maps the variable name to variable specifications
	private HashMap<String,VariableSpecification> recordsVariables = new HashMap<String,VariableSpecification>();
	private HashMap<String,FuzzingComponentNatureInfo> componentFuzzingInfo = new HashMap<String,FuzzingComponentNatureInfo>();
	
	private List<String> launchFilePaths = new ArrayList<String>();
	
	public void addRecord(String component, String variable, String reflectionName, VariableDirection dir, Optional<String> binaryPath, Optional<String> regex, boolean vehicleSpecific, Optional<TimeSpec> timeSpec, Optional<Double> fuzzProb) {
		if (!records.containsKey(component)) {
			records.put(component, new ArrayList<VariableSpecification>());
		}
		
		VariableSpecification vs = new VariableSpecification(component, variable, reflectionName, dir, binaryPath, regex, vehicleSpecific, timeSpec, fuzzProb);
		records.get(component).add(vs);
		recordsVariables.put(variable, vs);
	}
	
	public VariableSpecification getRecordForKey(String key) {
		return recordsVariables.get(key);
	}
	
	public Set<VariableSpecification> getRecordsUponComponent(String component) {
		Set<VariableSpecification> recs = new HashSet<VariableSpecification>();
		for (Map.Entry<String, VariableSpecification> e : recordsVariables.entrySet()) {
			Optional<String> s_o = e.getValue().getComponent();
			if (s_o.isPresent()) {
				if (s_o.get().equals(component)) {
					recs.add(e.getValue());
				}
			}		
		}
		return recs;
	}
	
	// Returns all the strings to mutate for a binary
	public HashMap<String,String> getBinaryChanges(String component, HashMap<String,String> inOutNames) {
		//	TODO: returning null hashmap for getBinaryChanges 
		HashMap<String,String> binaryVars = new HashMap<String,String>();
	
		for (Map.Entry<String,String> me : inOutNames.entrySet()) {
			String inkey = me.getKey();
			String outkey = me.getValue();
			if (isBinary(me.getKey())) {
				binaryVars.put(inkey, outkey);
			}
		}
		return binaryVars;
	}

	public boolean isBinary(String component) {
		// Get a record if it exists, indicate that the component nature is set to binary
		FuzzingComponentNatureInfo fcni = componentFuzzingInfo.get(component);
		if (fcni == null) {
			return false;
		} else {
			return (fcni.nature == FuzzingNature.BINARY);
		}
	}
	
	public boolean isEnviromental(String component) {
		// Get a record if it exists, indicate that the component nature is set to binary
		FuzzingComponentNatureInfo fcni = componentFuzzingInfo.get(component);
		if (fcni == null) {
			return false;
		} else {
			return fcni.isEnvironmental;
		}
	}

	public String getFullPath(String component) throws MissingComponentPath {
		FuzzingComponentNatureInfo fcni = componentFuzzingInfo.get(component);
		if (fcni == null) {
			throw new MissingComponentPath(component);
		} else {
			Optional<String> p = fcni.fullPath;
			if (p.isPresent()) {
				return p.get();
			} else throw new MissingComponentPath(component);
		}
	}

	public void setComponentFuzzingInfo(String component, FuzzingNature nature, Optional<String> classNameForConfig, Optional<String> fullPath, boolean isEnvironmental) {
		FuzzingComponentNatureInfo fcni = new FuzzingComponentNatureInfo(component, nature, classNameForConfig, fullPath, isEnvironmental);
		componentFuzzingInfo.put(component,fcni);
	}
	
	public Optional<String> getReflectionKey(String key) {
		VariableSpecification vr = getRecordForKey(key);
		if (vr != null) {
			return Optional.of(vr.getReflectionName());
		} else return Optional.empty();
	}
	
	public Map<String,VariableSpecification> getRecords() {
		return recordsVariables;
	}

	public void addOperationParameterSetForVariable(String var, OperationParameterSet opset, String subSpec) {
		VariableSpecification v = recordsVariables.get(var);
		v.addOperationParameterSet(opset, subSpec);
	}

	public void addLaunchFilePath(String path) {
		launchFilePaths.add(path);
	}

	public List<String> getAllLaunchFilesPaths() {
		return launchFilePaths;
	}
}