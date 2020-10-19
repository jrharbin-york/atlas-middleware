package carsspecific.moos.moosmapping;

public class ATLASWatchProcess extends MOOSProcess {
	public ATLASWatchProcess(MOOSCommunity parent, String vehicleName) {
		super("ATLASDBWatch", parent);
		setProperty("ACTIVEMQ_PORT", 61616);
		// TODO: something global to assign queue names/port names between the middleware and MOOS code generator
		setProperty("ACTIVEMQ_TOPIC", "MIDDLEWARE-watch-" + vehicleName);
	}
	
	public void addWatchVariable(String varname) {
		setProperty("WATCH_VAR", varname);
	}
}
