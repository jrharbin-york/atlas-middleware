package carsspecific.ros.rosmapping;

public class HelmBehaviourConstantSpeed extends MOOSBehaviour {
	public HelmBehaviourConstantSpeed(MOOSProcess parent) {
		super("BHV_ConstantSpeed", parent);
		setProperty("name", "constspeed_");
		setProperty("pwt", 100);
		setProperty("condition", "MODE==CONSTHEADING");
		setProperty("updates", "UP_HEADINGSPEED");
		setProperty("speed", 1.0);
		setProperty("duration", "no-time-limit");
	}
}