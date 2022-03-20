package atlascollectiveint.expt.healthcare;

import atlascollectiveint.api.*;
import atlascollectiveint.logging.CollectiveIntLog;
import atlassharedclasses.*;

import java.lang.Double;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class ComputerCIshoreside_healthcare_random {

	private static Map<String,Point> robotLocations = new HashMap<String,Point>();
	private final static int MAX_ROOM_ID = 9;
	private static final long TEMP_SEED = 54347246L;
	private static Random rng;
	
	public static Map<String,List<Integer>> randomWorkAssignments() {
		
		Map<String,List<Integer>> workassignments = new HashMap<String,List<Integer>>(); 
		
		List<Integer> pendingRooms = new ArrayList<Integer>();
		List<String> allRobots = new ArrayList<String>();
		allRobots.add("tb3_0");
		allRobots.add("tb3_1");
		allRobots.add("tb3_2");
		
		for (int r = 1; r <= MAX_ROOM_ID; r++) {
			pendingRooms.add(r);
		}
		
		while (!pendingRooms.isEmpty()) {
			for (String robotID : allRobots) {
				// Choose a random element using rng and remove it from pendingRooms
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
				System.out.println("randomWorkAssignments: assigning to " + robotID + " random room " + randomRoom);
				System.out.println("pendingRooms = " + pendingRooms);
			}
		}
		
		return workassignments;
	}
	
	private static void sendRobotWork(String robotName, List<Integer> robotRooms) {
		String workString = robotRooms.stream()
										.map(String::valueOf)
										.collect(Collectors.joining(","));
		CollectiveIntLog.logCI("Sending to robot " + robotName + " work string " + workString);
		API.sendSimulatorVariable(robotName, "/rooms", workString, true);
	}
	
	private static void sendAllWorkAssignments(Map<String, List<Integer>> allRooms) {
		for (Map.Entry<String,List<Integer>> e : allRooms.entrySet()) {
			String robotName = e.getKey();
			List<Integer> thisRobotRooms = e.getValue();
			sendRobotWork(robotName, thisRobotRooms);
		}
	}
	
	public static void init() {
		rng = new Random(TEMP_SEED);
		Map<String,List<Integer>> fixedRooms = randomWorkAssignments();
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
	}
	
	public static void BehaviourVariableHook(String key, String value, String robotName_uc, Double timeNow) {
		System.out.println("BehaviourVariableHook: robotName = " + robotName_uc + ",key = " + key + ",value=" + value);
	}
}