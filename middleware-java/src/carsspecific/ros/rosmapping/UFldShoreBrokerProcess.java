package carsspecific.ros.rosmapping;

public class UFldShoreBrokerProcess extends MOOSProcess {
	public UFldShoreBrokerProcess(ComputerCommunity parent) {
		super("uFldShoreBroker", parent);		
		
		setProperty("QBRIDGE", "DEPLOY, RETURN, STATION_KEEP, NODE_REPORT, NODE_MESSAGE");
		setProperty("QBRIDGE", "MOOS_MANUAL_OVERRIDE, APPCAST_REQ");
		setProperty("BRIDGE", "src=UP_LOITER_$N, alias=UP_LOITER");
		setProperty("BRIDGE", "src=DRIFT_VECTOR_ADD, alias=DRIFT_VECTOR_ADD");
		setProperty("BRIDGE", "src=HELM_MAP_CLEAR, alias=HELM_MAP_CLEAR");
		setProperty("BRIDGE", "src=APPCAST_REQ");
		
		// TODO: these should be conditionally included on the shoreside if sensors are
		// present somewhere in the simulation 
		//setProperty("bridge", "src=UHZ_CONFIG_ACK_$V,       alias=UHZ_CONFIG_ACK");
		//setProperty("bridge", "src=UHZ_DETECTION_REPORT_$V, alias=UHZ_DETECTION_REPORT");
		//setProperty("bridge", "src=HAZARDSET_REQUEST_$V,    alias=HAZARDSET_REQUEST");
	}
}
