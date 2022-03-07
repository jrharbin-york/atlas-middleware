package atlasdsl;

public class GoalVariable {
	private String name;
	private String variableType;
	private boolean vehicleSpecific;
	
	public GoalVariable(String name, String variableType, boolean vehicleSpecific) {
		this.name = name;
		this.variableType = variableType;
		this.vehicleSpecific = vehicleSpecific;
	}
	
	public String getName() {
		return name;
	}
	
	public String getVariableType() {
		return variableType;
	}
	
	public boolean isVehicleSpecific() {
		return vehicleSpecific;
	}
}