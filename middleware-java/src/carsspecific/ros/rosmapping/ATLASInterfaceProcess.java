package carsspecific.ros.rosmapping;

import activemq.portmapping.PortMappings;

public class ATLASInterfaceProcess extends MOOSProcess {
	public ATLASInterfaceProcess(MOOSCommunity parent, String vehicleName) {
		super("ATLASDBInterface", parent);
		setProperty("ACTIVEMQ_PORT", 61616);
		setProperty("ACTIVEMQ_TOPIC_PROD", PortMappings.portForMOOSWatch(vehicleName));
		setProperty("ACTIVEMQ_TOPIC_CONS", PortMappings.portForMOOSDB(vehicleName));
	}
	
	public void addWatchVariable(String varname) {
		setProperty("WATCH_VAR", varname);
	}
}
