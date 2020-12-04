package carsspecific.moos.moosmapping;

public class PLoggerProcess extends MOOSProcess {
	public PLoggerProcess(MOOSCommunity parent, String loggerFilename, Boolean isShoreside) {
		super("pLogger", parent);
		
		resetProperty("AppTick", 10);
		resetProperty("CommsTick", 10);
		setProperty("File", loggerFilename);
		
		setProperty("Path", "./");
		setProperty("SyncLog", "true @ 0.2");
		setProperty("AsyncLog", "true");
		setProperty("FileTimeStamp", "true");
		setProperty("WildCardLogging", "true");
		
		if (isShoreside) {
			// Set extra properties here
		} else {
			
		}
	}
}
