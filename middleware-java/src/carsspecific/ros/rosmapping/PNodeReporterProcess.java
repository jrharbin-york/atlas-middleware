package carsspecific.ros.rosmapping;
import atlasdsl.*;

public class PNodeReporterProcess extends MOOSProcess {
	// This is currently the default from the 
	// Setting vtype in the parent robot can override it
	private String defaultVehicleType = "KAYAK";
	
	public PNodeReporterProcess(MOOSCommunity parent, Robot robot) {
		super("pNodeReporter", parent);
		
		String vehicleType = defaultVehicleType;
		vehicleType = robot.vehicleTypeAsString();
		setProperty("VESSEL_TYPE", vehicleType);
	}
}
