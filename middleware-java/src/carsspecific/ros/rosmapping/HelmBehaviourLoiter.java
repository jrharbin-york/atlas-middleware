package carsspecific.ros.rosmapping;

public class HelmBehaviourLoiter extends MOOSBehaviour {
	// loiterPos should be e.g. "x=1,y=1"
	// TODO: convert loiterPos to Point type here for consistency
	public HelmBehaviourLoiter(MOOSProcess parent, String vehicleName, String loiterPos, double loiterSpeed, double radius, double nm_radius) {
		super("BHV_Loiter", parent);
		setProperty("name", "loiter");
		setProperty("pwt", 100);
		setProperty("condition", "MODE==LOITERING");
		setProperty("updates", "UP_LOITER");
		setProperty("runflag", "VEHICLE_UNDERWAY = TRUE");
		setProperty("endflag", "VEHICLE_UNDERWAY = FALSE");
		setProperty("post_suffix", "A");
		setProperty("speed", loiterSpeed);
		setProperty("clockwise", false);
		setProperty("radius", radius);
		setProperty("nm_radius", nm_radius);
		setProperty("polygon", "radial:: " + loiterPos + ", radius=20, pts=8, snap=1, label=" + vehicleName + "_LOITER");
		// TODO: check this works, multiple lines combined
		setProperty("visual_hints", "nextpt_color=white, nextpt_lcolor=khaki, edge_color=orange, vertex_color=white, edge_size=1, vertex_size=2");
	}
}
