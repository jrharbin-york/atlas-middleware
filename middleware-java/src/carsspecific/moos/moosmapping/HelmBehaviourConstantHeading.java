package carsspecific.moos.moosmapping;

public class HelmBehaviourConstantHeading extends MOOSBehaviour {
	public HelmBehaviourConstantHeading(MOOSProcess parent) {
		super("BHV_ConstantHeading", parent);
		setProperty("name", "constheading_");
		setProperty("pwt", 100);
		setProperty("condition", "MODE==CONSTHEADING");
		setProperty("updates", "UP_HEADING");
		setProperty("heading", 0.0);
		setProperty("duration", "no-time-limit");
	}
}