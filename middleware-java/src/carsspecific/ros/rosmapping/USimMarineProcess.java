package carsspecific.ros.rosmapping;

import atlassharedclasses.Point;

public class USimMarineProcess extends MOOSProcess {
	public USimMarineProcess(MOOSCommunity parent, Point startPos) {
		super("uSimMarine", parent);
		setProperty("START_POS", startPos.toString() + ",speed = 0, heading=0, depth=0");
		setProperty("PREFIX", "NAV");
	}
}
