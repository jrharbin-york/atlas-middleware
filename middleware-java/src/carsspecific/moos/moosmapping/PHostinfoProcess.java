package carsspecific.moos.moosmapping;

public class PHostinfoProcess extends MOOSProcess {
	private String defaultHostIP = "localhost";
	// ASSUMPTION: the system is always running upon localhost
	
	public PHostinfoProcess(MOOSCommunity parent) {
		super("pHostInfo", parent);
		setProperty("DEFAULT_HOSTIP", defaultHostIP);
	}
}
