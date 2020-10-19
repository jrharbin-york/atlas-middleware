package carsspecific.ros.rosmapping;

public class UFldNodeCommsProcess extends MOOSProcess {
	
	public UFldNodeCommsProcess(MOOSCommunity parent) {
		super("uFldNodeComms", parent);
		setProperty("COMMS_RANGE", 500);
		setProperty("CRITICAL_RANGE", 25);
		setProperty("MIN_MSG_INTERVAL", 15);
		setProperty("MAX_MSG_LENGTH", 1000);
		setProperty("GROUPS", true);
		setProperty("VIEW_NODE_RPT_PULSES", false);
	}
}
