package carsspecific.moos.translations;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.jms.JMSException;

import atlassharedclasses.Point;
import middleware.carstranslations.CARSTranslations;
import middleware.core.*;
import middleware.logging.ATLASLog;

public class MOOSTranslations extends CARSTranslations {
	
	HashMap<String,ActiveMQProducer> producers;
	
	public MOOSTranslations() {

	}
	
	public void setOutputProducers(HashMap<String,ActiveMQProducer> producers) {
		this.producers = producers;
	}
	
	public synchronized void sendCARSUpdate(String robotName, Object key, Object value) {
		Double endTimeOfUpdate = 1000000.0;
		String keyStr = key.toString();
		String valueStr = value.toString();
		String msg = endTimeOfUpdate.toString() + "|" + keyStr + "=" + valueStr;
		ActiveMQProducer prod = producers.get(robotName); 
		try {
			ATLASLog.logCARSOutbound(robotName, msg);
			prod.sendMessage(msg);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void stopVehicle(String robotName) {
		sendCARSUpdate(robotName, "MOOS_MANUAL_OVERRIDE", "true");
	}
	
	public synchronized void startVehicle(String robotName) {
		sendCARSUpdate(robotName, "MOOS_MANUAL_OVERRIDE", "false");
	}
	
	public synchronized void vehicleStatusChange(String robotName, boolean newStatus) {
		if (newStatus) {
			startVehicle(robotName);
		} else {
			stopVehicle(robotName);
		}
	}
	
	private static String pointListToMOOSPolyString(List<Point> coords) {
		String coordsJoined = coords.stream()
				.map(p -> p.toStringBareCSV())
				.collect(Collectors.joining(":"));
		return coordsJoined;
	}
	
	public synchronized void setCoordinates(String robotName, List<Point> coords) {
		setCoordinates(robotName, coords, SET_COORDINATES_REPEAT_COUNT);
	}
	
	public synchronized void setCoordinates(String robotName, List<Point> coords, int repeatCount) {
		String polyUpdate = "polygon=" + pointListToMOOSPolyString(coords) + ":label," + robotName + "_LOITER";
		System.out.println("CIEventQueue - SetCoordinates received: vehicle " + robotName + " : " + polyUpdate);
		sendCARSUpdate(robotName, "UP_WAYPOINT", polyUpdate);
		sendCARSUpdate(robotName, "UP_WAYPOINT", "repeat=" + repeatCount);
	}

	public void returnHome(String robotName) {
		sendCARSUpdate(robotName, "STATION_KEEP", "true");
		
	}

	public void simulatorVariableChange(String robotName, String key, String value, boolean isVehicleSpecific) {
		
	}
}
