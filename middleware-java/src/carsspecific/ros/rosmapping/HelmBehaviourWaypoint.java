package carsspecific.ros.rosmapping;

import atlassharedclasses.Point;

public class HelmBehaviourWaypoint extends MOOSBehaviour {
	// startPos should be e.g. "x=1,y=1"
	public HelmBehaviourWaypoint(MOOSProcess parent, String vehicleName, Point startPos, double speed, double radius, double nm_radius) {
		super("BHV_Waypoint", parent);
		setProperty("name", "waypt_return");
		setProperty("pwt", 100);
		// TODO: Waypoints should be more general than just returning!
		setProperty("condition", "MODE==RETURNING");
		setProperty("updates", "RETURN_UPDATES");
		setProperty("endflag", "STATION_KEEP = true");
		
		setProperty("speed", speed);  // default is 1.3
		setProperty("radius", radius);
		setProperty("nm_radius", nm_radius);
		setProperty("points", startPos.toStringBareCSV());
		setProperty("repeat", 10);
		setProperty("lead", 8);
	}
}
