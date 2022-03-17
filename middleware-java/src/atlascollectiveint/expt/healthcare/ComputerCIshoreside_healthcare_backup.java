package atlascollectiveint.expt.healthcare;

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

public class ComputerCIshoreside_healthcare_backup {

	private static Map<String,List<Integer>> workassignments = new HashMap<String,List<Integer>>(); 
	private static Map<String,Point> robotLocations = new HashMap<String,Point>(); 
	
	public static Map<String,List<Integer>> fixedWorkAssignments() {
		List<Integer> rooms_tb0 = new ArrayList<Integer>();
		rooms_tb0.add(1);
		rooms_tb0.add(2);
		rooms_tb0.add(6);
		
		List<Integer> rooms_tb1 = new ArrayList<Integer>();
		rooms_tb1.add(3);
		rooms_tb1.add(5);
		rooms_tb1.add(7);
		
		List<Integer> rooms_tb2 = new ArrayList<Integer>();
		rooms_tb2.add(4);
		rooms_tb2.add(8);
		rooms_tb2.add(9);
		
		workassignments.put("tb3_0", rooms_tb0);
		workassignments.put("tb3_1", rooms_tb1);
		workassignments.put("tb3_2", rooms_tb2);
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
		Map<String,List<Integer>> fixedRooms = fixedWorkAssignments();
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