package carsspecific.moos.moosmapping;

public class PObstacleMgrProcess extends MOOSProcess {
	public PObstacleMgrProcess(MOOSCommunity parent) {
		super("pObstacleMgr", parent);
		setProperty("point_var", "TRACKED_FEATURE");
	}
}
