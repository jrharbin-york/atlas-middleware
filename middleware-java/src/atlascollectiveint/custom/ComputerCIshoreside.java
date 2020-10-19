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
import java.util.Optional;
import java.util.stream.Collectors;

class ComputerCIshoreside {

	// The shoreside CI's copy of the robot information
	private static List<String> sweepRobots = new ArrayList<String>();
	private static List<String> cameraRobots = new ArrayList<String>();
	private static List<String> allRobots = new ArrayList<String>();

	private static HashMap<String, Point> robotLocations = new LinkedHashMap<String, Point>();
	private static HashMap<Integer, Integer> detectionCounts = new LinkedHashMap<Integer, Integer>();
	private static HashMap<String, Boolean> robotIsConfirming = new LinkedHashMap<String, Boolean>();
	private static HashMap<String, Region> robotSweepRegions = new LinkedHashMap<String, Region>();
	private static HashMap<String, Double> robotSpeeds = new LinkedHashMap<String, Double>();
	private static Region fullRegion;

	private static final double SWEEP_RADIUS = 50.0;
	private static final double VERTICAL_STEP_SIZE_INITIAL_SWEEP = 30;
	private static final double VERTICAL_STEP_SIZE_CONFIRM_SWEEP = 10;
	private static final int VERTICAL_ROWS_STATIC_SPLIT = 2;

	private static final double TIME_SPENT_VERIFYING = 500.0;
	private static final double CAMERA_DIVE_DEPTH = 20.0;

	private static boolean freshDetection(int label) {
		Integer c = detectionCounts.get(label);
		if (c == null) {
			detectionCounts.put(label, 0);
			return true;
		} else {
			detectionCounts.put(label, c + 1);
			return false;
		}
	}

	private static Map<String, Double> robotDistancesTo(Point loc) {
		Map<String, Double> res;
		res = robotLocations.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey(), e -> loc.distanceTo(e.getValue())));
		return res;
	}

	private static double travelTimeForRobot(String robotName, double dist) {
		Double speed = robotSpeeds.get(robotName);
		if (speed == null) {
			System.out.println(
					"Robot " + robotName + " does not have a speed defined: setting its travel time to maximum");
			return Double.MAX_VALUE;
		} else {
			double time = dist / speed;
			return time;
		}
	}

	// The shoreside chooses a robot to use to confirm detections,
	// excluding the detecting one obviously!
	private static Optional<String> chooseRobot(Point loc, List<String> allowedRobots, String excludeRobot) {
		Map<String, Double> dists = robotDistancesTo(loc);
		Optional<Map.Entry<String, Double>> res = dists.entrySet().stream()
				// Check it isn't the exclude robot
				.filter(e -> e.getKey().compareTo(excludeRobot) != 0 && allowedRobots.contains(e.getKey()))
				// Check robot is not already confirming!
				.filter(e -> !robotIsConfirming.get(e.getKey()))
				// Have to transform to new Map - here the distance is mapped to the travel time
				.collect(Collectors.toMap(Map.Entry::getKey, e -> travelTimeForRobot(e.getKey(), e.getValue())))
				.entrySet().stream().sorted(Map.Entry.comparingByValue()).findFirst();
		if (res.isPresent()) {
			// If a robot found, set it as confirming a detection
			String chosen = res.get().getKey();
			robotIsConfirming.put(chosen, true);
			return Optional.of(res.get().getKey());
		} else {
			// If no robots are known, it will be empty
			return Optional.empty();
		}
	}

	public static void setRobotNamesAndRegion() {
		// For now, hardcode in statically the names of the robots
		sweepRobots.add("frank");
		sweepRobots.add("gilda");
		sweepRobots.add("henry");
		sweepRobots.add("ella");
		cameraRobots.add("brian");
		cameraRobots.add("linda");

		robotSpeeds.put("frank", 1.5);
		robotSpeeds.put("gilda", 1.5);
		robotSpeeds.put("henry", 1.5);
		robotSpeeds.put("ella", 1.6);

		robotSpeeds.put("brian", 0.75);
		robotSpeeds.put("linda", 0.75);

		for (String r : sweepRobots) {
			robotIsConfirming.put(r, false);
			allRobots.add(r);
		}

		for (String r : cameraRobots) {
			robotIsConfirming.put(r, false);
			allRobots.add(r);
		}

		fullRegion = new Region(new Point(-50.0, -230.0), new Point(200.0, -30.0));
	}

	public static Map<String, Region> staticRegionSplit(Region fullRegion, List<String> robots) {
		HashMap<String, Region> assignments = new HashMap<String, Region>();

		int count = robots.size();
		int hcount = count / 2;
		int vcount = VERTICAL_ROWS_STATIC_SPLIT;
		double subwidth = fullRegion.width() / hcount;
		double subheight = fullRegion.height() / vcount;
		for (int i = 0; i < count; i++) {
			String robot = robots.get(i);
			// TODO: fix these expressions
			double xl = fullRegion.left() + i % hcount * subwidth;
			double yb = fullRegion.bottom() + i / vcount * subheight;
			Region subr = new Region(xl, yb, xl + subwidth, yb + subheight);
			assignments.put(robot, subr);
		}
		return assignments;
	}

	public static void init() {
		System.out.println("init");

		setRobotNamesAndRegion();

		Map<String, Region> regionAssignments = staticRegionSplit(fullRegion, sweepRobots);
		CollectiveIntLog.logCI("ComputerCIshoreside.init - regionAssignments length = " + regionAssignments.size());
		for (Map.Entry<String, Region> e : regionAssignments.entrySet()) {
			String robot = e.getKey();
			Region region = e.getValue();
			API.setPatrolAroundRegion(robot, region, VERTICAL_STEP_SIZE_INITIAL_SWEEP,
					("UUV_COORDINATE_UPDATE_INIITAL_" + robot.toUpperCase()));

			CollectiveIntLog.logCI("Starting sweep robot " + robot);
			API.startVehicle(robot);
			
			CollectiveIntLog.logCI("Setting robot " + robot + " to scan region " + region.toString());
			robotSweepRegions.put(robot, region);		
			
		}

		for (String robot : cameraRobots) {
			CollectiveIntLog.logCI("Starting camera robot " + robot);
			API.startVehicle(robot);
			API.setDepth(robot, CAMERA_DIVE_DEPTH, "DEPTH_SET_MESSAGE_" + robot);
			CollectiveIntLog.logCI("Starting camera robot " + robot);
		}
	}

	public static void SONARDetectionHook(SensorDetection detection, String robotName) {
		// On a detection, if the detection is the first time...
		// Send a second robot in to confirm
		// Need to scan the positions to find the best choice

		Point loc = (Point) detection.getField("location");
		int label = (Integer) detection.getField("objectID");
		String detectionType = (String) detection.getField("type");

		List<String> candidateRobots;
		if (detectionType.equals("benign")) {
			candidateRobots = allRobots;
		} else {
			candidateRobots = cameraRobots; 
		}
				
		if (freshDetection(label)) {
			Optional<String> rName_o = chooseRobot(loc, candidateRobots, robotName);
			if (rName_o.isPresent()) {
				String rName = rName_o.get();
				API.setSweepAroundPoint(rName, loc, SWEEP_RADIUS, VERTICAL_STEP_SIZE_CONFIRM_SWEEP,
						("UUV_COORDINATE_UPDATE_VERIFY_" + rName.toUpperCase()));
				CollectiveIntLog.logCI("Setting robot " + rName + " to verify detection");

				// Need to send this robot back to its original action after some time...
				// Register a one-off timer to return the robot to its original activity
				OneOffTimer treturn = OneOffTimer.afterDelay(TIME_SPENT_VERIFYING, (t -> {
					CollectiveIntLog.logCI("Verification timer expired for robot " + rName);
					Region origRegion = robotSweepRegions.get(rName);
					if (origRegion != null) {
						// Set the robot as available for future detections
						robotIsConfirming.put(rName, false);
						CollectiveIntLog.logCI("Setting robot " + rName + " to return to region " + origRegion.toString());
						API.setPatrolAroundRegion(rName, origRegion, VERTICAL_STEP_SIZE_INITIAL_SWEEP,
								("UUV_COORDINATE_UPDATE_INIITAL_" + rName.toUpperCase()));
					}
				}));

				API.registerTimer(rName, treturn);

			} else {
				CollectiveIntLog.logCI("ERROR: No robots avaiable to confirm the detection");
			}
		}
	}

	public static void GPS_POSITIONDetectionHook(Double x, Double y, String robotName) {
		// Update the robot position notification
		robotLocations.put(robotName, new Point(x, y));
	}

	public static void CAMERADetectionHook(SensorDetection detection, String robotName) {
		Point loc = (Point) detection.getField("location");
		int objectID = (Integer) detection.getField("objectID");
		String detectionType = (String) detection.getField("type");
		CollectiveIntLog.logCI("Camera detection of object ID " + objectID + "at location" + " type " + detectionType + robotName);
	}
}