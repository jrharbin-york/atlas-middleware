package atlasdsl;

public class SimulatorVariable {
	
	public enum VariableTag {
		GENERIC,
	  	VELOCITY,
	  	POSITION,
	  	SET_VELOCITY,
	  	TIME,
	}
	
	private String varName;
	private String simType;
	private VariableTag tag;
	private boolean isVehicleSpecific;
	private boolean propagateToCI;
	
	public SimulatorVariable(String varName, String simType, VariableTag tag, boolean isVehicleSpecific, boolean propagateToCI) {
		this.varName = varName;
		this.simType = simType;
		this.tag = tag;
		this.isVehicleSpecific = isVehicleSpecific;
		this.propagateToCI = propagateToCI;
	}
	
	public String getVarName() {
		return varName;
	}
	
	public VariableTag getTag() {
		return tag;
	}
	
	public String getSimType() {
		return simType;
	}
}