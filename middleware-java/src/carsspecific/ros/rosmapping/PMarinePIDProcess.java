package carsspecific.ros.rosmapping;

public class PMarinePIDProcess extends MOOSProcess {
	public PMarinePIDProcess(MOOSCommunity parent) {
		super("pMarinePID", parent);
		
		setProperty("VERBOSE", true);
		setProperty("DEPTH_CONTROL", true);
		setProperty("ACTIVE_START", true);
		
		setProperty("YAW_PID_KP", 0.4);
		setProperty("YAW_PID_KD", 0.1);
		setProperty("YAW_PID_KI", 0.0);
		setProperty("YAW_PID_INTEGRAL_LIMIT", 0.07);
		
		setProperty("SPEED_PID_KP", 0.4);
		setProperty("SPEED_PID_KD", 0.1);
		setProperty("SPEED_PID_KI", 0.0);
		setProperty("SPEED_PID_INTEGRAL_LIMIT", 0.07);

		// Vertical setting from mission s7_golf
		setProperty("pitch_pid_kp", 0.12);
		setProperty("pitch_pid_kd", 0);
		setProperty("pitch_pid_ki", 0.004);
		setProperty("pitch_pid_integral_limit", 0.05);
		
		setProperty("z_to_pitch_pid_kp", 0.12);
		setProperty("z_to_pitch_pid_kd", 0);
		setProperty("z_to_pitch_pid_ki", 0.004);
		setProperty("z_to_pitch_pid_integral_limit", 0.05);
		
		setProperty("MAXRUDDER", 100);
		setProperty("MAXTHRUST", 100);
		setProperty("MAXPITCH", 15);
		setProperty("MAXELEVATOR", 13);
		setProperty("SPEED_FACTOR", 20);
	}
}
