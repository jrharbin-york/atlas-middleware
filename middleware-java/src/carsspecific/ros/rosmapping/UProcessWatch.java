package carsspecific.ros.rosmapping;

public class UProcessWatch extends MOOSProcess {
	public UProcessWatch(MOOSCommunity parent) {
		super("uProcessWatch", parent);
		setProperty("ALLOW_RETRACTIONS", true);
		setProperty("WATCH_ALL", true);
		setProperty("NOWATCH", "uXMS*");
		setProperty("NOWATCH", "uMS*");
		setProperty("NOWATCH", "uPokeDB*");
		setProperty("SUMMARY_WAIT", 6);
	}
}
