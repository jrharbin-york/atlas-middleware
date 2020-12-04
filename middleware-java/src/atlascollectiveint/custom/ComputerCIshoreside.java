package atlascollectiveint.custom;

import atlascollectiveint.api.*;
import atlascollectiveint.logging.CollectiveIntLog;
import atlassharedclasses.*;

import java.lang.Double;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class ComputerCIshoreside {
	// The shoreside CI's copy of the robot information
	private static List<String> sweepRobots = new ArrayList<String>();
	//private static List<String> cameraRobots = new ArrayList<String>();
	private static List<String> allRobots = new ArrayList<String>();

	private static HashMap<String, Point> robotLocations = new LinkedHashMap<String, Point>();
	//private static HashMap<Integer, Integer> detectionCounts = new LinkedHashMap<Integer, Integer>();
	private static HashMap<String, Boolean> robotIsConfirming = new LinkedHashMap<String, Boolean>();
	//private static HashMap<String, Region> robotSweepRegions = new LinkedHashMap<String, Region>();
	private static HashMap<String, Double> robotSpeeds = new LinkedHashMap<String, Double>();
	private static Map<String, Region> regionAssignments = new HashMap<String,Region>();
	
	private static Region lowerRegion;
	private static Region upperRegion;

	private static int rotateCount = 0;
	
	private static final double VERTICAL_STEP_SIZE_INITIAL_SWEEP = 10;

	public static void setupRobotNamesAndRegion() {
		sweepRobots.add("frank");
		sweepRobots.add("ella");
		robotSpeeds.put("frank", 1.5);
		robotSpeeds.put("ella", 1.6);

		for (String r : sweepRobots) {
			robotIsConfirming.put(r, false);
			allRobots.add(r);
		}

		lowerRegion = new Region(new Point(-50.0, -230.0), new Point(200.0, -130.0));
		upperRegion = new Region(new Point(-50.0, -130.0), new Point(200.0, -30.0));
	}
	
	private static void assignRegions() {
		for (Map.Entry<String, Region> e : regionAssignments.entrySet()) {
			String robot = e.getKey();
			Region region = e.getValue();
			API.setPatrolAroundRegion(robot, region, VERTICAL_STEP_SIZE_INITIAL_SWEEP,
					("UUV_COORDINATE_UPDATE_INIITAL_" + robot.toUpperCase()));
			CollectiveIntLog.logCI("Setting robot " + robot + " to scan region " + region.toString());
		}
	}
	
	private static void rotateRegions() {
		regionAssignments.clear();
			
		if ((rotateCount % 2) == 0) {
			regionAssignments.put("ella", upperRegion);
			regionAssignments.put("frank",lowerRegion);
		} else {
			regionAssignments.put("ella", lowerRegion);
			regionAssignments.put("frank",upperRegion);
		}
		assignRegions();
		
		if (rotateCount == 0) {
			API.startVehicle("ella");
			API.startVehicle("frank");
		}
		
		rotateCount++;
	}

	public static void init() {
		//System.out.println("init");
		//setupRobotNamesAndRegion();	
		//rotateRegions();		
		//CollectiveIntLog.logCI("ComputerCIshoreside.init - regionAssignments length = " + regionAssignments.size());
		API.startVehicle("gilda");
		API.startVehicle("henry");
	}

	public static void SONARDetectionHook(SensorDetection detection, String robotName) {
		CollectiveIntLog.logCI("SONARDetectionHook");
	}

	public static void GPS_POSITIONDetectionHook(Double x, Double y, String robotName) {
		robotLocations.put(robotName, new Point(x, y));
		//CollectiveIntLog.logCI("GPS_POSITIONDetectionHook");
	}

	public static void CAMERADetectionHook(SensorDetection detection, String robotName) {
		CollectiveIntLog.logCI("CAMERADetectionHook");
	}
}