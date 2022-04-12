package atlascollectiveint.expt.healthcare;

import atlascollectiveint.api.*;
import atlascollectiveint.logging.CollectiveIntLog;
import atlasdsl.Battery;
import atlasdsl.EnvironmentalObject;
import atlasdsl.MissingProperty;
import atlasdsl.Mission;
import atlasdsl.Robot;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import atlassharedclasses.*;

import java.lang.Double;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ComputerCIshoreside_healthcare_dynamicenergy {

	private static Map<String, List<Integer>> workassignments = new HashMap<String, List<Integer>>();
	private static Map<String, List<Integer>> outstandingWork = new HashMap<String, List<Integer>>();

	private static Map<String, Double> robotEnergy = new HashMap<String, Double>();
	private static Map<String, Point> robotLocations = new HashMap<String, Point>();
	private static Map<Integer, Point> roomLocations = new HashMap<Integer, Point>();
	private static Map<String, Double> emptyRobots = new HashMap<String, Double>();

	private static final double ENERGY_GO_HOME_THRESHOLD = 19000.0;

	private static Mission mission;

	// This is reset from the DSL
	private static double MIN_BATTERY;

	private static void loadRoomLocationsFromDSL() throws MissingProperty {
		for (EnvironmentalObject e : mission.getEnvironmentalObjects()) {
			Integer roomID = e.getLabel();
			Point p = new Point(e.getX(), e.getY(), e.getZ());
			roomLocations.put(roomID, p);
			CollectiveIntLog.logCI("Room " + roomID + " coordinates " + p.toString());
		}

		for (Robot r : mission.getAllRobots()) {
			String robotName = r.getName();
			Point coord = r.getPointComponentProperty("startLocation");
			robotLocations.put(robotName, coord);
			CollectiveIntLog.logCI("Robot " + robotName + " startLocation is " + coord);
		}
	}

	public static Optional<Integer> closestRoomTo(Point robotLoc, Set<Integer> allowedRooms) {
		Double maxDist = Double.MAX_VALUE;
		Optional<Integer> room = Optional.empty();
		for (Map.Entry<Integer, Point> e : roomLocations.entrySet()) {
			Point roomLoc = e.getValue();
			Integer r = e.getKey();

			if (allowedRooms.contains(r)) {
				double thisDist = roomLoc.distanceTo(robotLoc);
				if (thisDist < maxDist) {
					maxDist = thisDist;
					room = Optional.of(r);
				}
			}
		}

		return room;
	}

	private static void addAssignment(Map<String, List<Integer>> work, String rname, Integer closestRoom) {
		List<Integer> rooms;

		if (!work.containsKey(rname)) {
			rooms = new ArrayList<Integer>();
		} else {
			rooms = work.get(rname);
		}

		rooms.add(closestRoom);
		work.put(rname, rooms);

	}

	private static void addWorkassignment(Map<String, List<Integer>> work, String rname, Integer closestRoom) {
		addAssignment(work, rname, closestRoom);
		addAssignment(outstandingWork, rname, closestRoom);
	}

	private static int robotCountForBattery(String robotID) {
		Robot r = mission.getRobot(robotID);
		if (r != null) {
			List<Battery> bats = r.getBatteries();
			double totalEnergy = bats.stream().mapToDouble(b -> b.getMaxEnergy()).sum();
			System.out.println("Robot " + robotID + "total energy in batteries is " + totalEnergy);
			int robotCount = (int) Math.round(totalEnergy / MIN_BATTERY);
			return robotCount;
		} else {
			return 0;
		}
	}
	
	private static double minEnergyAnyRobot() {
		double minVal = Double.MAX_VALUE;
		for (Map.Entry<String,Double> e : robotEnergy.entrySet()) {
			String robotID = e.getKey();
			if (!emptyRobots.containsKey(robotID)) {
				double energy = e.getValue();
				if (energy < minVal) {
					minVal = energy;
				}
			}
		}
		return minVal;
	}
	
	private static int robotCountForBatteryDynamicEnergy(String robotID) {
		if (robotEnergy.containsKey(robotID)) {
			double thisRobotEnergy = robotEnergy.get(robotID);
			double minEnergy = minEnergyAnyRobot();
			int robotCount = (int)Math.round(thisRobotEnergy / minEnergy);
			return robotCount;
		} else {
			return 0;
		}
	}

	private static double findMinimalBatteryEnergy() {
		double minBattery = Double.MAX_VALUE;
		for (Robot r : mission.getAllRobots()) {
			if (r != null) {
				List<Battery> bats = r.getBatteries();
				double totalEnergy = bats.stream().mapToDouble(b -> b.getMaxEnergy()).sum();
				if (totalEnergy < minBattery) {
					minBattery = totalEnergy;
				}
			}
		}
		System.out.println("Found minimal battery: " + minBattery);
		return minBattery;
	}

	public static Map<String, List<Integer>> workAssignmentsByEnergy(Set<Integer> pendingRooms, boolean useStartingEnergy) {
		Map<String, List<Integer>> assignments = new HashMap<String, List<Integer>>();

		while (!pendingRooms.isEmpty()) {
			for (Map.Entry<String, Point> e : robotLocations.entrySet()) {
				// Need to give the higher energy robots TWO goes at selection in the loop
				String robotID = e.getKey();
				Point robotLoc = e.getValue();
				
				int robotCount;
				if (useStartingEnergy) {
					robotCount = robotCountForBattery(robotID);
				} else {
					robotCount = robotCountForBatteryDynamicEnergy(robotID);
				}
				
				System.out.println("robotCount = " + robotCount);
				for (int i = 0; i < robotCount; i++) {

					// Choose a random element using rng and remove it from pendingRooms
					if (pendingRooms.size() > 0) {
						Optional<Integer> chosenRoom_o = closestRoomTo(robotLoc, pendingRooms);
						if (chosenRoom_o.isPresent()) {
							Integer chosenRoom = chosenRoom_o.get();
							addWorkassignment(assignments, robotID, chosenRoom);
							pendingRooms.remove(chosenRoom);
							System.out.println(
									"randomWorkAssignments: assigning to " + robotID + " random room " + chosenRoom);
							System.out.println("pendingRooms = " + pendingRooms);
						} else {
							System.out.println("No room available");
						}
					}
				}
			}
		}

		return assignments;
	}

	public static void closestExtraWorkAssignments(Set<Integer> pendingRooms, Set<String> excludeRobots) {

		// while there is work pending...
		while (pendingRooms.size() > 0) {
			// round-robin over all robots
			for (Map.Entry<String, Point> erobots : robotLocations.entrySet()) {
				String rname = erobots.getKey();
				if (!excludeRobots.contains(rname)) {
					Point robotLoc = erobots.getValue();
					// find the closest room to the given robot
					Optional<Integer> closestRoom_o = closestRoomTo(robotLoc, pendingRooms);
					if (closestRoom_o.isPresent()) {
						Integer closestRoom = closestRoom_o.get();
						addWorkassignment(workassignments, rname, closestRoom);
						pendingRooms.remove(closestRoom);
					}
				}
			}
		}
	}

	public static void energyBasedExtraWorkAssignments(Set<Integer> pendingRooms, Set<String> excludeRobots) {
		// while there is work pending...
		while (pendingRooms.size() > 0) {
			// round-robin over all robots
			for (Map.Entry<String, Point> erobots : robotLocations.entrySet()) {
				String robotID = erobots.getKey();
				Point robotLoc = erobots.getValue();
				// This is the change to use dynamic energy at this time - if it causes a problem, revert it
				int robotCount = robotCountForBatteryDynamicEnergy(robotID);

				for (int i = 0; i < robotCount; i++) {
					String rname = erobots.getKey();
					if (!excludeRobots.contains(rname)) {
						// find the closest room to the given robot
						Optional<Integer> closestRoom_o = closestRoomTo(robotLoc, pendingRooms);
						if (closestRoom_o.isPresent()) {
							Integer closestRoom = closestRoom_o.get();
							addWorkassignment(workassignments, rname, closestRoom);
							pendingRooms.remove(closestRoom);
						}
					}
				}
			}
		}
	}

	private static void sendRobotWork(String robotName, List<Integer> robotRooms) {
		String workString = robotRooms.stream().map(String::valueOf).collect(Collectors.joining(","));
		CollectiveIntLog.logCI("Sending to robot " + robotName + " work string " + workString);
		API.sendSimulatorVariable(robotName, "/rooms", workString, true);
	}

	private static void sendAllWorkAssignments(Map<String, List<Integer>> allRooms) {
		for (Map.Entry<String, List<Integer>> e : allRooms.entrySet()) {
			String robotName = e.getKey();
			// Do not try to reassign work to an empty robot
			if (!emptyRobots.containsKey(robotName)) {
				List<Integer> thisRobotRooms = e.getValue();
				sendRobotWork(robotName, thisRobotRooms);
			}
		}
	}
	
	public static void loadDSL() throws DSLLoadFailed, MissingProperty {
		DSLLoader dslloader = new GeneratedDSLLoader();
		mission = dslloader.loadMission();
		loadRoomLocationsFromDSL();
		MIN_BATTERY = findMinimalBatteryEnergy();
	}

	public static void init() {
		try {
			loadDSL();
			Set<Integer> pendingRooms = new HashSet<>(roomLocations.keySet());
			workassignments = workAssignmentsByEnergy(pendingRooms, true);
			sendAllWorkAssignments(workassignments);
		} catch (DSLLoadFailed e) {
			e.printStackTrace();
		} catch (MissingProperty e) {
			e.printStackTrace();
		}
	}

	public static void SONARDetectionHook(SensorDetection detection, String robotName) {

	}

	public static void GPS_POSITIONDetectionHook(Double x, Double y, String robotName) {
		// System.out.println("GPS Position Update: " + robotName + ": x=" + x + ",y=" +
		// y);
		robotLocations.put(robotName, new Point(x, y));
	}

	public static void CAMERADetectionHook(SensorDetection detection, String robotName) {

	}

	public static void EnergyUpdateHook(EnergyUpdate energyUpdate, String robotName) {
		
		
		if (energyUpdate.getEnergyValue() < ENERGY_GO_HOME_THRESHOLD) {
			// Battery is empty or at the minimal threshold

			if (!emptyRobots.containsKey(robotName)) {
				// Return the robot which has expired or nearly expired to go back home
				System.out.println("Sending " + robotName + " home...");
				API.returnHome(robotName);

				// Reassign the work that the robot is carrying
				System.out.println("Reassigning work due to no energy on " + robotName + " ...");
				emptyRobots.put(robotName, energyUpdate.getEnergyValue());
				reassignWork(robotName);
				System.out.println("Reassigning work done on " + robotName);
			}
		}
	}

	private static synchronized void reassignWork(String robotName) {
		if (outstandingWork.containsKey(robotName)) {
			Set<Integer> newPendingRooms = new HashSet<>(outstandingWork.get(robotName));
			if (newPendingRooms.size() > 0) {
				energyBasedExtraWorkAssignments(newPendingRooms, emptyRobots.keySet());
				sendAllWorkAssignments(outstandingWork);
			}
		} else {
			CollectiveIntLog.logCI("No work for " + robotName + " to reassign");
		}
	}

	public static void BehaviourVariableHook(String key, String value, String robotName_uc, Double timeNow) {
		System.out.println("BehaviourVariableHook: robotName = " + robotName_uc + ",key = " + key + ",value=" + value);
		String robotName = robotName_uc.toLowerCase();
		if (key.equals("/roomCompleted")) {
			Integer room = Integer.valueOf(value);
			System.out.println("Room completed for action");
			removeOutstandingWork(robotName, room);
		}
	}

	private static void removeOutstandingWork(String robotName, Integer room) {
		if (outstandingWork.containsKey(robotName)) {
			List<Integer> rooms = outstandingWork.get(robotName);
			rooms.remove(Integer.valueOf(room));
			outstandingWork.put(robotName, rooms);
		}

	}
}