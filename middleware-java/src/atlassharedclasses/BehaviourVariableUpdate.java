package atlassharedclasses;

public class BehaviourVariableUpdate {
	private String vehicleName;
	private String variableName;
	private Object variableValue;
	
	BehaviourVariableUpdate() {
		
	}
	
	public BehaviourVariableUpdate(String vehicleName, String variableName, Object variableValue) {
		this.vehicleName = vehicleName;
		this.variableName = variableName;
		this.variableValue = variableValue;
	}
	
	public String getVariableName() {
		return variableName;
	}
	
	public String getVariableValueAsString() {
		return variableValue.toString();
	}
	
	public Object getVariableValue() {
		return variableValue;
	}
	
	public String getVehicleName() {
		return vehicleName;
	}
}
