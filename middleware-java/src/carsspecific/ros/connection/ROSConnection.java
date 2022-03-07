package carsspecific.ros.connection;

import edu.wpi.rail.jrosbridge.JRosbridge;
import edu.wpi.rail.jrosbridge.Ros;

public class ROSConnection {
	private Ros ros;
	private static ROSConnection conn;
	
	private static final String ROS_HOSTNAME = "localhost";
	private static final int ROS_PORT = 8080;
	
	private ROSConnection(String hostname, int port) {
		ros = new Ros(hostname, port, JRosbridge.WebSocketType.ws);
		System.out.println("ROS object created");
		ros.connect();
		System.out.println("ROS connect done");
	}
	
	public Ros getROS() {
		return ros; 
	}
	
	public static ROSConnection getConnection() {
		if (conn == null) {
			conn = new ROSConnection(ROS_HOSTNAME, ROS_PORT);
		}
		return conn;
	}	
}