package atlascollectiveint.expt.healthcare;

import atlascollectiveint.api.*;
import atlascollectiveint.logging.CollectiveIntLog;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ComputerCIshoreside_healthcare {

	//private static Map<String,List<Integer>> workassignments = new HashMap<String,List<Integer>>(); 
	private static Map<String,Point> robotLocations = new HashMap<String,Point>(); 
	private static Map<Integer,Point> roomLocations = new HashMap<Integer,Point>();
	
	private static Mission mission;
	
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
		for (Map.Entry<Integer,Point> e : roomLocations.entrySet()) {
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
	
	private static void addWorkassignment(Map<String,List<Integer>> work, String rname, Integer closestRoom) {
		List<Integer> rooms;
		
		if (!work.containsKey(rname)) {
			rooms = new ArrayList<Integer>();
		} else {
			rooms = work.get(rname);
		}
		
		rooms.add(closestRoom);
		work.put(rname, rooms);
		
	}
	
	public static Map<String,List<Integer>> closestWorkAssignments(Set<Integer> pendingRooms) {
		Map<String,List<Integer>> workassignments = new HashMap<String,List<Integer>>();
		
		// while there is work pending...
		while (pendingRooms.size() > 0) {
			// round-robin over all robots
			for (Map.Entry<String,Point> erobots : robotLocations.entrySet()) {
				String rname = erobots.getKey();
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
	
	public static void loadDSL() throws DSLLoadFailed, MissingProperty {
		DSLLoader dslloader = new GeneratedDSLLoader();
		mission = dslloader.loadMission();
		loadRoomLocationsFromDSL();
	}
	
	public static void init() {
		try {
			loadDSL();
			Set<Integer> pendingRooms = roomLocations.keySet();
			Map<String,List<Integer>> fixedRooms = closestWorkAssignments(pendingRooms);
			sendAllWorkAssignments(fixedRooms);
		} catch (DSLLoadFailed e) {
			e.printStackTrace();
		} catch (MissingProperty e) {
			e.printStackTrace();
		}
	}

	public static void SONARDetectionHook(SensorDetection detection, String robotName) {

	}

	public static void GPS_POSITIONDetectionHook(Double x, Double y, String robotName) {
		//System.out.println("GPS Position Update: " + robotName + ": x=" + x + ",y=" + y);
		robotLocations.put(robotName, new Point(x, y));
	}

	public static void CAMERADetectionHook(SensorDetection detection, String robotName) {

	}
	
	public static void EnergyUpdateHook(EnergyUpdate energyUpdate, String robotName) {
		//System.out.println("EnergyUpdateHook - energy value is " + energyUpdate.getEnergyValue());
	}
	
	public static void BehaviourVariableHook(String key, String value, String robotName_uc, Double timeNow) {
		System.out.println("BehaviourVariableHook: robotName = " + robotName_uc + ",key = " + key + ",value=" + value);
	}
}