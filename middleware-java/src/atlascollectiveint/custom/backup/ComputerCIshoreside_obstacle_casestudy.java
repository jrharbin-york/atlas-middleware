package atlascollectiveint.custom.backup;

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

class ComputerCIshoreside_obstacle_casestudy {
	public static void init() {
		API.startVehicle("gilda");
		API.startVehicle("henry");
	}

	public static void SONARDetectionHook(SensorDetection detection, String robotName) {
		CollectiveIntLog.logCI("SONARDetectionHook");
	}

	public static void GPS_POSITIONDetectionHook(Double x, Double y, String robotName) {
		//robotLocations.put(robotName, new Point(x, y));
		//CollectiveIntLog.logCI("GPS_POSITIONDetectionHook");
	}

	public static void CAMERADetectionHook(SensorDetection detection, String robotName) {
		CollectiveIntLog.logCI("CAMERADetectionHook");
	}
}