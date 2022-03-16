package atlassharedclasses;

public class ChangeSimulatorVariable extends BehaviourCommand {
	private String vehicleName;
	private String key;
	private String value;
	private boolean vehicleSpecific;
	
	ChangeSimulatorVariable() {
		
	}
	
	public ChangeSimulatorVariable(String vehicleName, String key, String value, boolean vehicleSpecific) {
		this.vehicleName = vehicleName;
		this.key = key;
		this.value = value;
		this.vehicleSpecific = vehicleSpecific;
	}
	
	public String getVehicleName() {
		return vehicleName;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean isVehicleSpecific() {
		return vehicleSpecific;
	}
}
