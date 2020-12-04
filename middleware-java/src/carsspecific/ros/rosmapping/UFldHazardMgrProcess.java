package carsspecific.ros.rosmapping;

public class UFldHazardMgrProcess extends MOOSProcess {
	public UFldHazardMgrProcess(MOOSCommunity parent, String robotName, double swathWidth, double detectionProb) {
		super("uFldHazardMgr", parent);
		setProperty("swath_width", swathWidth);
		setProperty("sensor_pd", detectionProb);
		setProperty("report_name", robotName);
	}
}
