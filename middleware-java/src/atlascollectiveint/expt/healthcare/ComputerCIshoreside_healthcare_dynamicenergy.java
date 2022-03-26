package atlascollectiveint.expt.healthcare;

import atlascollectiveint.api.*;
import atlascollectiveint.logging.CollectiveIntLog;
import atlasdsl.Battery;
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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class ComputerCIshoreside_healthcare_dynamicenergy {

	private static Map<String, Point> robotLocations = new HashMap<String, Point>();

	private final static int MAX_ROOM_ID = 9;

	private final static double MIN_BATTERY = 400.0;

	private static final long TEMP_SEED = 54347246L;
	private static Random rng;
	private static Mission mission;

	private static Map<String, List<Integer>> fixedRooms;

	public static void loadDSL() throws DSLLoadFailed {
		DSLLoader dslloader = new GeneratedDSLLoader();
		mission = dslloader.loadMission();
		// Things to load from the database:
		// the number of robots and their speed properties
	}

	private static int robotCountForBattery(String robotID) {
		Robot r = mission.getRobot(robotID);
		if (r != null) {
			List<Battery> bats = r.getBatteries();
			double totalEnergy = bats.stream().mapToDouble(b -> b.getMaxEnergy()).sum();
			int robotCount = (int) (totalEnergy / MIN_BATTERY);
			return robotCount;
		} else {
			return 0;
		}
	}

	public static Map<String, List<Integer>> scaledRoundRobinWorkAssignments(List<Integer> pendingRooms) {

		Map<String, List<Integer>> workassignments = new HashMap<String, List<Integer>>();
		Map<String, Double> robotEnergyLeft = new HashMap<String, Double>();

		List<String> allRobots = new ArrayList<String>();
		allRobots.add("tb3_0");
		allRobots.add("tb3_1");
		allRobots.add("tb3_2");

		while (!pendingRooms.isEmpty()) {
			for (String robotID : allRobots) {
				// Need to give the higher energy robots TWO goes at selection in the loop
				int robotCount = robotCountForBattery(robotID);
				for (int i = 0; i < robotCount; i++) {

					// Choose a random element using rng and remove it from pendingRooms
					if (pendingRooms.size() > 0) {
						Integer randomRoom = pendingRooms.get(rng.nextInt(pendingRooms.size()));

						List<Integer> robotsChoice;
						if (!workassignments.containsKey(robotID)) {
							robotsChoice = new ArrayList<Integer>();
						} else {
							robotsChoice = workassignments.get(robotID);
						}

						robotsChoice.add(randomRoom);
						pendingRooms.remove(randomRoom);
						workassignments.put(robotID, robotsChoice);
						System.out.println(
								"randomWorkAssignments: assigning to " + robotID + " random room " + randomRoom);
						System.out.println("pendingRooms = " + pendingRooms);
					}
				}
			}
		}

		return workassignments;
	}

	private static void sendRobotWork(String robotName, List<Integer> robotRooms) {
		String workString = robotRooms.stream().map(String::valueOf).collect(Collectors.joining(","));
		CollectiveIntLog.logCI("Sending to robot " + robotName + " work string " + workString);
		API.sendSimulatorVariable(robotName, "/rooms", workString, true);
	}

	private static void sendAllWorkAssignments(Map<String, List<Integer>> allRooms) {
		for (Map.Entry<String, List<Integer>> e : allRooms.entrySet()) {
			String robotName = e.getKey();
			List<Integer> thisRobotRooms = e.getValue();
			sendRobotWork(robotName, thisRobotRooms);
		}
	}

	public static void init() {
		try {
			loadDSL();
		} catch (DSLLoadFailed e) {
			e.printStackTrace();
		}

		rng = new Random(TEMP_SEED);
		List<Integer> pendingRooms = new ArrayList<Integer>();
		for (int r = 1; r <= MAX_ROOM_ID; r++) {
			pendingRooms.add(r);
		}
		fixedRooms = scaledRoundRobinWorkAssignments(pendingRooms);
		sendAllWorkAssignments(fixedRooms);
	}

	public static void SONARDetectionHook(SensorDetection detection, String robotName) {

	}

	public static void GPS_POSITIONDetectionHook(Double x, Double y, String robotName) {
		System.out.println("GPS Position Update: " + robotName + ": x=" + x + ",y=" + y);
		robotLocations.put(robotName, new Point(x, y));
	}

	public static void CAMERADetectionHook(SensorDetection detection, String robotName) {

	}

	public static void EnergyUpdateHook(EnergyUpdate energyUpdate, String robotName) {
		System.out.println("EnergyUpdateHook - energy value is " + energyUpdate.getEnergyValue());
		if (energyUpdate.getEnergyValue() < 1e-6) {
			// Battery is empty
			System.out.println("Reassigning work due to no energy on " + robotName + " ...");
			reassignWork(robotName);
		}
	}

	private static void reassignWork(String robotName) {
		List<Integer> newPendingRooms = fixedRooms.get(robotName);
		fixedRooms = scaledRoundRobinWorkAssignments(newPendingRooms);
		fixedRooms.put(robotName, new ArrayList<Integer>());
	}

	public static void BehaviourVariableHook(String key, String value, String robotName_uc, Double timeNow) {
		System.out.println("BehaviourVariableHook: robotName = " + robotName_uc + ",key = " + key + ",value=" + value);
	}
}