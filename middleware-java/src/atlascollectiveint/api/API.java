package atlascollectiveint.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import atlascollectiveintgenerator.CollectiveInt;
import atlascollectiveintgenerator.CollectiveIntActiveMQProducer;
import atlassharedclasses.*;

public class API {
	// TODO: to add to the API - speed change requests from the CI?
	
	private static boolean DEBUG_POLYGON_COORDS = true;
	private static int DEFAULT_PATROL_REPEAT_COUNT = 0;
	
	private static HashMap<String,CollectiveIntActiveMQProducer> producers = new HashMap<String,CollectiveIntActiveMQProducer>();
	private static CollectiveInt ci;
	
	private static List<Point> translateRegionToCoordsList(Region r, double desiredStepSize) {
		int stepCount = (int)Math.ceil(r.height() / desiredStepSize);
		double stepSize = r.height() / stepCount;
						
		List<Point> coords = new ArrayList<Point>();
		double left = r.left();
		double right = r.right();
		double top = r.top();
		double bottom = r.bottom();
		
		for (double y = bottom; y < top; y+=2*stepSize) {
			coords.add(new Point(left,y));
			coords.add(new Point(right,y));
			coords.add(new Point(right,y+stepSize));
			coords.add(new Point(left,y+stepSize));
		}
		Collections.reverse(coords);
		return coords;
	}
	
	private static CollectiveIntActiveMQProducer getProducerFor(String robotName) {
		return producers.get(robotName);
	}
	
	public static void registerNewProducer(String communityName, CollectiveIntActiveMQProducer producer) {
		producers.put(communityName, producer);
	}
	
	public static void setCIReference(CollectiveInt ciRef) {
		ci = ciRef;
	}
	
	// TODO: should have a mode, what to do when an invalid robot name is entered
	// if it is invalid, whether to ignore or throw an exception for the call
	private static void sendOrIgnore(BehaviourCommand cmd, String robotName) {
		CollectiveIntActiveMQProducer prod = producers.get(robotName);
		if (prod != null) {
			prod.send(cmd, robotName);
		}
	}
	
	///////////////////////////////// API COMMANDS HERE //////////////////////////////////////////////////////////
		
	public static List<Point> setPatrolAroundRegion(String robotName, Region r, double stepSize, String MessageName, int repeatCount) {
		List<Point> coords = translateRegionToCoordsList(r, stepSize);
		System.out.println("region =\n" + r);
		if (DEBUG_POLYGON_COORDS) {
			System.out.println("coords=" + coords.toString());
		}
		BehaviourCommand cmd = new SetCoordinates(coords, repeatCount, MessageName);
		sendOrIgnore(cmd, robotName);
		return coords;
	}
	
	public static List<Point> setPatrolCoords(String robotName, List<Point> coords, String MessageName, int repeatCount) {
		if (DEBUG_POLYGON_COORDS) {
			System.out.println("coords=" + coords.toString());
		}
		BehaviourCommand cmd = new SetCoordinates(coords, repeatCount, MessageName);
		sendOrIgnore(cmd, robotName);
		return coords;
	}
	
	public static List<Point> setPatrolAroundRegion(String robotName, Region r, double stepSize, String MessageName) {
		return setPatrolAroundRegion(robotName, r, stepSize, MessageName, DEFAULT_PATROL_REPEAT_COUNT);
	}
	
	public static List<Point> setPatrolCoords(String robotName, List<Point> coords, String MessageName) {
		return setPatrolCoords(robotName, coords, MessageName, DEFAULT_PATROL_REPEAT_COUNT);
	}
	
	public static void startVehicle(String robotName) {
		BehaviourCommand cmd = new VehicleStartStopCommand(robotName, true);
		sendOrIgnore(cmd, robotName);
	}
	
	public static void stopVehicle(String robotName) {
		BehaviourCommand cmd = new VehicleStartStopCommand(robotName, false);
		sendOrIgnore(cmd, robotName);
	}
	
	public static void setSweepAroundPoint(String robotName, Point p, double size, double stepSize, String messageName) {
		Region r = Region.squareAroundPoint(p, size);
		setPatrolAroundRegion(robotName, r, stepSize, messageName);
	}

	public static void registerTimer(String timerName, Timer timer) {
		ci.registerTimer(timerName, timer);
	}
	
	public static void setDepth(String robotName, double depth, String messageName) {
		BehaviourCommand cmd = new SetDepth(depth, messageName);
		sendOrIgnore(cmd, robotName);
	}
	
	public static void returnHome(String robotName) {
		BehaviourCommand cmd = new ReturnHome();
		sendOrIgnore(cmd, robotName);
	}
	
	public static void sendSimulatorVariable(String robotName, String key, String value, boolean vehicleSpecific) {
		BehaviourCommand cmd = new ChangeSimulatorVariable(robotName, key, value, vehicleSpecific);
		sendOrIgnore(cmd, robotName);
	}
}
