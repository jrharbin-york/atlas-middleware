package middleware.carstranslations;

import java.util.HashMap;
import java.util.List;

import atlassharedclasses.Point;
import middleware.core.*;

public abstract class CARSTranslations {
	// This is the default used if no repeat count is specified
	protected final int SET_COORDINATES_REPEAT_COUNT = 10;
	
	public abstract void sendCARSUpdate(String robotName, Object key, Object value);
	public abstract void vehicleStatusChange(String robotName, boolean start);
	public abstract void startVehicle(String robotName);
	public abstract void stopVehicle(String robotName);
	public abstract void setCoordinates(String robotName, List<Point> coords);
	public abstract void setCoordinates(String robotName, List<Point> coords, int repeatCount);
	public abstract void setOutputProducers(HashMap<String,ActiveMQProducer> producers);
	public abstract void returnHome(String robotName);
	public abstract void simulatorVariableChange(String robotName, String key, String value, boolean vehicleSpecific);
	
	public void sendBackEvent(CARSVariableUpdate event, String reflectBackName) {
		String vehicleName = event.getVehicleName();
		String key = reflectBackName;
		String value = event.getValue();
		sendCARSUpdate(vehicleName, key, value);
	}
}
