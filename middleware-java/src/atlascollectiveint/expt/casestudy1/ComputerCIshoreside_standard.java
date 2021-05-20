package atlascollectiveint.expt.casestudy1;

import atlascollectiveint.api.*;
import atlascollectiveint.logging.CollectiveIntLog;
import atlasdsl.DiscoverObjects;
import atlasdsl.Goal;
import atlasdsl.GoalAction;
import atlasdsl.GoalRegion;
import atlasdsl.MissingProperty;
import atlasdsl.Mission;
import atlasdsl.Robot;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import atlassharedclasses.*;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.Double;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ComputerCIshoreside_standard {

	private static Mission mission;

	// The shoreside copy of the robot information
	private static List<String> sweepRobots = new ArrayList<String>();
	private static List<String> cameraRobots = new ArrayList<String>();
	private static List<String> allRobots = new ArrayList<String>();

	private static HashMap<String,Double> robotSensorWidths = new LinkedHashMap<String, Double>();
	private static HashMap<String, Point> robotLocations = new LinkedHashMap<String, Point>();
	private static HashMap<Integer, Integer> detectionCounts = new LinkedHashMap<Integer, Integer>();
	private static HashMap<String, Boolean> robotIsConfirming = new LinkedHashMap<String, Boolean>();
	private static HashMap<String, Region> robotSweepRegions = new LinkedHashMap<String, Region>();
	private static HashMap<String, Double> robotSpeeds = new LinkedHashMap<String, Double>();
	
	private static HashMap<String, Double> robotCompleteTimes = new LinkedHashMap<String,Double>();
	
	private static final Region DEFAULT_REGION = new Region(new Point(-50.0, -230.0), new Point(200.0, -30.0));
	private static Region fullRegion = DEFAULT_REGION;
	
	private static double END_TIME;
	private static double END_TIME_OFFSET = 10.0;
	
	private static int numberOfVerificationsMalicious;
	private static int numberOfVerificationsBenign;
	
	private static final double SWEEP_RADIUS = 60.0;
	private static final double VERTICAL_STEP_SIZE_INITIAL_SWEEP = 30;
	private static final double VERTICAL_STEP_SIZE_CONFIRM_SWEEP = 29;
	private static final int VERTICAL_ROWS_STATIC_SPLIT = 1;

	private static final double TIME_SPENT_VERIFYING = 600.0;

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
	
    private static Optional<String> chooseRobotByDistance(Point loc, String excludeRobot) {

    	Map<String,Double> dists = robotDistancesTo(loc);
    	Optional<Map.Entry<String, Double>> res = dists.entrySet().stream()
    			// Check it isn't the exclude robot
    			.filter(e -> e.getKey().compareTo(excludeRobot) != 0)
    			// Check robot is not already confirming!
    			.filter(e -> !robotIsConfirming.get(e.getKey()))
    			.sorted(Map.Entry.comparingByValue())
    			.findFirst();
    	// TODO: check sort order here
    	
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
	
	public static List<String> getAllRobotsByDepth(boolean onSurface) throws MissingProperty {
		List<String> rmap = new ArrayList<String>();
		for (Robot robot : mission.getAllRobots()) {
			if ((robot.getDoubleComponentProperty("maxDepth") < 1e6) == onSurface) {
				rmap.add(robot.getName());
			}
		}
		return rmap;
	}

	public static void setSystemStateFromModel() throws DSLLoadFailed, MissingProperty {
		loadDSL();
		robotSpeeds = getRobotNamesAndSpeeds();
		robotSensorWidths = getRobotSensorWidths();
		
		// The sweep robots are the surface-only robots
		sweepRobots = getAllRobotsByDepth(true);
		// The camera robots are the depth-capable robots
		cameraRobots = getAllRobotsByDepth(false);

		// Setup robot state
		for (String r : sweepRobots) {
			robotIsConfirming.put(r, false);
			allRobots.add(r);
		}

		for (String r : cameraRobots) {
			robotIsConfirming.put(r, false);
			allRobots.add(r);
		}
		
		Goal sensorSweep = mission.getGoalByName("primarySensorSweep");
		Goal discoverObjects = mission.getGoalByName("findTestObjects");
		GoalAction discoverObjectAct = discoverObjects.getAction();
		
		if (discoverObjectAct instanceof DiscoverObjects) {
			DiscoverObjects dobj = (DiscoverObjects)discoverObjectAct;
			numberOfVerificationsMalicious = dobj.verificationsMalicious();
			numberOfVerificationsBenign = dobj.verificationsBenign();
			System.out.println("numberOfVerificationsMalicious = " + numberOfVerificationsMalicious);
			System.out.println("numberOfVerificationsBenign = " + numberOfVerificationsBenign);
		}
		
		Optional<GoalRegion> r_o = sensorSweep.getGoalRegion();
		if (r_o.isPresent()) {
				r_o.get();
				GoalRegion go = r_o.get();
				fullRegion = go.getFirstRegion().get();
		}
	}

	/** Returns constant region size proportions */
	public static Map<String, Double> regionProportionsConstant(List<String> robots) {
		HashMap<String, Double> regionProportions = new HashMap<String, Double>();

		double constantProp = 1.0 / robots.size();
		for (Map.Entry<String, Double> rs : robotSpeeds.entrySet()) {
			// only count the named robots
			if (robots.contains(rs.getKey())) {
				regionProportions.put(rs.getKey(), constantProp);
			}
		}

		return regionProportions;
	}

	// Just try setting up the regions here as a constant strip
	public static Map<String, Region> staticRegionSplitBySpeeds(Region fullRegion, List<String> robots) {
		HashMap<String, Region> assignments = new HashMap<String, Region>();

		int count = robots.size();
		int hcount = count;
		int vcount = VERTICAL_ROWS_STATIC_SPLIT;
		double subheight = fullRegion.height() / vcount;

		int xindex = 0;
		double xl = fullRegion.left();
		double yb = fullRegion.bottom();
		for (Map.Entry<String, Double> rs : regionProportionsConstant(robots).entrySet()) {
			double subwidth = fullRegion.width() * rs.getValue() * vcount;
			Region subr = new Region(xl, yb, xl + subwidth, yb + subheight);
			// Need to know the total proportion taken up on a particular row,
			// then scale the height according to this!
			xl += subwidth;
			xindex++;
			if (xindex == hcount) {
				xindex = 0;
				yb += subheight;
				xl = fullRegion.left();
			}
			assignments.put(rs.getKey(), subr);
		}
		return assignments;
	}

	public static void loadDSL() throws DSLLoadFailed {
		DSLLoader dslloader = new GeneratedDSLLoader();
		mission = dslloader.loadMission();
		// Things to load from the database:
		// the number of robots and their speed properties
	}

	public static HashMap<String, Double> getRobotNamesAndSpeeds() throws MissingProperty {
		HashMap<String, Double> rSpeeds = new HashMap<String, Double>();
		for (Robot robot : mission.getAllRobots()) {
			double speed = robot.getDoubleComponentProperty("startSpeed");
			rSpeeds.put(robot.getName(), speed);
		}
		return rSpeeds;
	}
	
	public static HashMap<String, Double> getRobotSensorWidths() throws MissingProperty {
		HashMap<String, Double> rSensorWidths = new HashMap<String, Double>();
		for (Robot robot : mission.getAllRobots()) {		
			// Use a constant width
			double width = VERTICAL_STEP_SIZE_INITIAL_SWEEP;
			System.out.println("Robot " + robot.getName() + " - sensor width=" + width);
			rSensorWidths.put(robot.getName(), width);
		}
		return rSensorWidths;
	}

	public static void init() {
		try {
			System.out.println("init");
			// Load the DSL contents, and load some information from the vehicles
			setSystemStateFromModel();
			System.out.println("robotSpeeds = " + robotSpeeds);
			System.out.println("fullRegion = " + fullRegion);

			Map<String, Region> regionAssignments = staticRegionSplitBySpeeds(fullRegion, sweepRobots);
			CollectiveIntLog.logCI("ComputerCIshoreside.init - regionAssignments length = " + regionAssignments.size());

			END_TIME = mission.getEndTime();
			
			// Start all the robots first
			for (Map.Entry<String, Region> e : regionAssignments.entrySet()) {
				String robot = e.getKey();
				Region region = e.getValue();
				CollectiveIntLog.logCI("Starting sweep robot " + robot);
				API.startVehicle(robot);
			}

			// Then set the region assignments
			for (Map.Entry<String, Region> e : regionAssignments.entrySet()) {
				String robot = e.getKey();
				Region region = e.getValue();
				Double sensorWidth = robotSensorWidths.get(robot);
				if (sensorWidth == null) {
					sensorWidth = VERTICAL_STEP_SIZE_INITIAL_SWEEP;
				}
				
				API.setPatrolAroundRegion(robot, region, sensorWidth,
						("UUV_COORDINATE_UPDATE_INIITAL_" + robot.toUpperCase()), 1);

				CollectiveIntLog.logCI("Setting robot " + robot + " to scan region " + region.toString());
				robotSweepRegions.put(robot, region);
			}

			// Record the count at the endtime
			OneOffTimer tEnd = OneOffTimer.atTime((END_TIME - END_TIME_OFFSET), (t -> {
				recordTimings();
			}));
			
			API.registerTimer("recordTimings", tEnd);
			
		} catch (DSLLoadFailed e1) {
			CollectiveIntLog.logCI("DSL Loading Failed");
			e1.printStackTrace();
		} catch (MissingProperty e1) {
			CollectiveIntLog.logCI("Missing property during processing");
			e1.printStackTrace();
		}
	}

	private static void recordTimings() {
		try {
			FileWriter output = new FileWriter("/tmp/completeTimings.log");
			for (String robotName : sweepRobots) {
				double time = END_TIME;
				if (robotCompleteTimes.containsKey(robotName)) {
					time = robotCompleteTimes.get(robotName);
				}
				output.write(robotName + "," + time + "\n");
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public static void verifyOneRobot(SensorDetection detection, List<String> candidateRobots,
			String detectingRobotName) {
		Point loc = (Point) detection.getField("location");
		Optional<String> rName_o = chooseRobotByDistance(loc, detectingRobotName);
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

	public static void SONARDetectionHook(SensorDetection detection, String robotName) {
		int label = (Integer) detection.getField("objectID");
		String detectionType = (String) detection.getField("type");

		if (freshDetection(label)) {
			if (detectionType.equals("benign")) {
				for (int i = 0; i < numberOfVerificationsBenign; i++) {
					verifyOneRobot(detection, sweepRobots, robotName);
				}
			} else {
				int numV = Math.max(numberOfVerificationsMalicious, 0);
				for (int i = 0; i < numV; i++) {
					verifyOneRobot(detection, sweepRobots, robotName);
				}
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
		CollectiveIntLog.logCI(
				"Camera detection of object ID " + objectID + "at location" + " type " + detectionType + robotName);
	}
	
	public static void EnergyUpdateHook(EnergyUpdate energyUpdate, String robotName) {
		//System.out.println("EnergyUpdateHook - energy value is " + energyUpdate.getEnergyValue());
	}
	
	public static void BehaviourVariableHook(String key, String value, String robotName_uc, Double timeNow) {
		String robotName = robotName_uc.toLowerCase();
		System.out.println("BehaviourVariableHook: robotName = " + robotName + ",key = " + key + ",value=" + value);
		
		if (!robotIsConfirming.get(robotName)) {
			if (!robotCompleteTimes.containsKey(robotName)) {
				robotCompleteTimes.put(robotName, timeNow);
			}
		}
	}
}