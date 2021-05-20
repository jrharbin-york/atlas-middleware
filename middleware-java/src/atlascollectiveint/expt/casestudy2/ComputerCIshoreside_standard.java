package atlascollectiveint.expt.casestudy2;

import atlascollectiveint.api.*;
import atlascollectiveint.logging.CollectiveIntLog;
import atlassharedclasses.*;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.Double;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

public class ComputerCIshoreside_standard {

	// TODO: get these from the model?
	static final double TIME_BEFORE_SWITCHING = 150;
	static final double TIME_TO_AUTO_RETURN = 1050;
	static final double END_TIME = 1190.0;
	
	static Region region1 = new Region(new Point(170, -100), new Point(209, -60));
	static Region region2 = new Region(new Point(-75, -100), new Point(-35, -60));
	static Map<String,Integer> waypointCompleteCounts = new HashMap<String,Integer>();
	static Region gildaRegion = region1;
	static Region henryRegion = region2;
	
	public static void alternateRegions() {
		Region tmp = gildaRegion;
		gildaRegion = henryRegion;
		henryRegion = tmp;
	}
	
	public static void setVehicleRegions() {
		API.setPatrolAroundRegion("gilda", gildaRegion, 10,
				"UUV_COORDINATE_UPDATE_INITIAL_GILDA");
		API.setPatrolAroundRegion("henry", henryRegion, 10,
				"UUV_COORDINATE_UPDATE_INITIAL_HENRY");
	}
	
	public static void recordCountWaypoints() {
		try {
			FileWriter output = new FileWriter("/tmp/waypointCount.log");
			for (Map.Entry<String, Integer> eo_d : waypointCompleteCounts.entrySet()) {
				String robotName = eo_d.getKey();
				int count = eo_d.getValue();				
				output.write(robotName + "," + count + "\n");
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void incrementCompleteCount(String robotName) {
		System.out.println("robotName = " + robotName);
		if (!waypointCompleteCounts.containsKey(robotName)) {
			waypointCompleteCounts.put(robotName, 0);
		}
		
		Integer current = waypointCompleteCounts.get(robotName);
		waypointCompleteCounts.put(robotName, current+1);
	}
	
	public static void init() {
		API.startVehicle("gilda");
		API.startVehicle("henry");
		setVehicleRegions();
		
		PeriodicTimer tSwitchRegion = new PeriodicTimer(TIME_BEFORE_SWITCHING, (t -> {
			alternateRegions();
			setVehicleRegions();
		}));
		
		// Set all the robots to return at a given time
		OneOffTimer tReturn = OneOffTimer.atTime(TIME_TO_AUTO_RETURN, (t -> {
			API.returnHome("gilda");
			API.returnHome("henry");
		}));
		
		// Record the count at the endtime
		OneOffTimer tEnd = OneOffTimer.atTime(END_TIME, (t -> {
			recordCountWaypoints();
		}));
		
		API.registerTimer("returnHome", tReturn);
		API.registerTimer("switchRegions", tSwitchRegion);
		API.registerTimer("recordCountWaypoints", tEnd);
	}


	public static void SONARDetectionHook(SensorDetection detection, String robotName) {
		CollectiveIntLog.logCI("SONARDetectionHook");
	}

	public static void GPS_POSITIONDetectionHook(Double x, Double y, String robotName) {

	}

	public static void CAMERADetectionHook(SensorDetection detection, String robotName) {
		CollectiveIntLog.logCI("CAMERADetectionHook");
	}
	
	public static void EnergyUpdateHook(EnergyUpdate energyUpdate, String robotName) {

	}
	
	public static void BehaviourVariableHook(String key, String value, String robotName, Double timeNow) {
		incrementCompleteCount(robotName);
		System.out.println("Waypoint complete count incremented for " + robotName + " to " + waypointCompleteCounts.get(robotName));
	}
}