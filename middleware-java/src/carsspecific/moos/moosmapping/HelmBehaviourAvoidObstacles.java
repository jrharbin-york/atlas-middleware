package carsspecific.moos.moosmapping;

public class HelmBehaviourAvoidObstacles extends MOOSBehaviour {
	public HelmBehaviourAvoidObstacles(MOOSProcess parent, String vehicleName) {
		super("BHV_AvoidCollision", parent);
		setProperty("name", "avd_obstacles_");
		setProperty("pwt", 500);
		setProperty("condition", "DEPLOY = true");
		setProperty("templating", "spawn");
		setProperty("updates", "OBSTACLE_ALERT");
		setProperty("allowable_ttc", 5);
		setProperty("buffer_dist", 7);
		setProperty("pwt_outer_dist", 20);
		setProperty("pwt_inner_dist", 10);
		setProperty("completed_dist", 25);
		// use_refinery = true in alternate plugin?
	}
}
