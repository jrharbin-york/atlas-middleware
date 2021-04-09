package atlascollectiveint.custom;

import atlascollectiveint.api.*;
import atlascollectiveint.logging.CollectiveIntLog;
import atlassharedclasses.*;

import java.lang.Double;
import java.lang.String;

class ComputerCIshoreside2 {
	static final double TIME_SPENT_VERIFYING = 150;
	static Region region1 = new Region(new Point(170, -113), new Point(209, -73));
	static Region region2 = new Region(new Point(-75, -113), new Point(-35, -73));
	static Region gildaRegion = region1;
	static Region henryRegion = region2;
	
	public static void alternateRegions() {
		Region tmp = gildaRegion;
		gildaRegion = henryRegion;
		henryRegion = tmp;
	}
	
	public static void setVehicleRegions() {
		API.setPatrolAroundRegion("gilda", gildaRegion, 10,
				"UUV_COORDINATE_UPDATE_INIITAL_GILDA");
		API.setPatrolAroundRegion("henry", henryRegion, 10,
				"UUV_COORDINATE_UPDATE_INIITAL_HENRY");
	}
	
	public static void init() {
		System.out.println("ComputerCIShoreside 2 ---------------------------------");
		API.startVehicle("gilda");
		API.startVehicle("henry");
		setVehicleRegions();
		
		PeriodicTimer tSwitchRegion = new PeriodicTimer(TIME_SPENT_VERIFYING, (t -> {
			alternateRegions();
			setVehicleRegions();
		}));
		
		API.registerTimer("switchRegions", tSwitchRegion);
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
		System.out.println("EnergyUpdateHook - energy value is " + energyUpdate.getEnergyValue());
	}
}