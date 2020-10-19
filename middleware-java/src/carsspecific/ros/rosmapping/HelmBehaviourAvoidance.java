package carsspecific.ros.rosmapping;

public class HelmBehaviourAvoidance extends MOOSBehaviour {
	public HelmBehaviourAvoidance(MOOSProcess parent, String vehicleName) {
		super("BHV_AvoidCollision", parent);
		setProperty("name", "avdcollision_");
		setProperty("pwt", 200);
		setProperty("condition", "(AVOID=true) and (CONSTHEADING=false)");
		setProperty("updates", "CONTACT_INFO");
		setProperty("endflag", "CONTACT_RESOLVED = $[CONTACT]");
		setProperty("templating", "spawn");
		setProperty("contact", "to-be-set");
		setProperty("on_no_contact_ok", true);
		setProperty("extrapolate", true);
		setProperty("decay", "30,60");
		setProperty("pwt_outer_dist", 50);
		setProperty("pwt_inner_dist", 20);
		setProperty("completed_dist", 75);
		setProperty("min_util_cpa_dist", 3);
		setProperty("max_util_cpa_dist", 7);
		setProperty("pwt_grade", "linear");
		setProperty("bearing_line_config", "white:0, green:0.65, yellow:0.8, red:1.0");
	}
}
