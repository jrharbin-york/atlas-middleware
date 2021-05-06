package atlascollectiveint.expt.casestudy2;

import atlascollectiveint.api.*;
import atlascollectiveint.logging.CollectiveIntLog;
import atlasdsl.Goal;
import atlasdsl.GoalAction;
import atlasdsl.Mission;
import atlasdsl.ReturnOnLowEnergy;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import atlassharedclasses.*;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.Double;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

public class ComputerCIshoreside_energytracking {
	
	static Mission mission;
	
	//static final double TIME_BEFORE_SWITCHING = 500;
	static double ENERGY_CRITICAL_LEVEL = 0.0;
	static final double END_TIME = 1190.0;
	
	static boolean gildaFinishedSweep = false;
	static boolean henryFinishedSweep = false;
	
	static Region region1 = new Region(new Point(170, -100), new Point(209, -60));
	static Region region2 = new Region(new Point(-75, -100), new Point(-35, -60));
	static Map<String,Integer> waypointCompleteCounts = new HashMap<String,Integer>();
	static Region gildaRegion = region1;
	static Region henryRegion = region2;
	static Map<String,Boolean> missionEnded = new HashMap<String,Boolean>();
	
	public static void alternateRegions() {
		Region tmp = gildaRegion;
		gildaRegion = henryRegion;
		henryRegion = tmp;
	}
	
	public static void setVehicleRegions() {
		API.setPatrolAroundRegion("gilda", gildaRegion, 10,	"UUV_COORDINATE_UPDATE_INIITIAL_GILDA");
		API.setPatrolAroundRegion("henry", henryRegion, 10,	"UUV_COORDINATE_UPDATE_INIITIAL_HENRY");
	}
	
	public static void loadDSL() throws DSLLoadFailed {
		DSLLoader dslloader = new GeneratedDSLLoader();
		mission = dslloader.loadMission();
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
		try {
			loadDSL();
			API.startVehicle("gilda");
			API.startVehicle("henry");
			setVehicleRegions();
			
			Goal returnTime = mission.getGoalByName("returnOnLowEnergy");
			GoalAction returnAction = returnTime.getAction();
			
			if (returnAction instanceof ReturnOnLowEnergy) {
				ENERGY_CRITICAL_LEVEL = ((ReturnOnLowEnergy)returnAction).getEnergyThreshold();
				System.out.println("ENERGY_CRITICAL_LEVEL = " + ENERGY_CRITICAL_LEVEL);
			}
			
//			PeriodicTimer tSwitchRegion = new PeriodicTimer(TIME_BEFORE_SWITCHING, (t -> {
//				alternateRegions();
//				setVehicleRegions();
//			}));
//			API.registerTimer("switchRegions", tSwitchRegion);
			
			// Record the count at the endtime
			OneOffTimer tEnd = OneOffTimer.atTime(END_TIME, (t -> {
				recordCountWaypoints();
			}));
			

			API.registerTimer("recordCountWaypoints", tEnd);
			
		} catch (DSLLoadFailed e) {
			e.printStackTrace();
		}
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
		if (!missionEnded.containsKey(robotName)) {
			if (energyUpdate.getEnergyValue() < ENERGY_CRITICAL_LEVEL) {
				CollectiveIntLog.logCI("Robot name " + robotName + ": energy is " + energyUpdate.getEnergyValue() + " returning home");
				System.out.println("Robot name " + robotName + " energy is " + energyUpdate.getEnergyValue() + " returning home");
				API.returnHome(robotName);
				missionEnded.put(robotName, true);
			}
		}
	}
	
	public static void BehaviourVariableHook(String key, String value, String robotNameUppercase, Double timeNow) {
		String robotName = robotNameUppercase.toLowerCase();
		incrementCompleteCount(robotName);
		System.out.println("Waypoint complete count incremented for " + robotName + " to " + waypointCompleteCounts.get(robotName));
		
		if (robotName.equals("gilda")) {
			gildaFinishedSweep = true;
			System.out.println("Setting gilda sweep finished to true");
		}
		
		if (robotName.equals("henry")) {
			henryFinishedSweep = true;
			System.out.println("Setting henry sweep finished to true");
		}
		
		if (gildaFinishedSweep && henryFinishedSweep) {
			System.out.println("Swapping vehicle assignments");
			gildaFinishedSweep = false;
			henryFinishedSweep = false;
			alternateRegions();
			setVehicleRegions();
		}
	}
}