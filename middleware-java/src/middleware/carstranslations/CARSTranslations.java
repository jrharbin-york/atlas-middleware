package middleware.carstranslations;

import java.util.HashMap;

import middleware.core.*;

public abstract class CARSTranslations {
	public abstract void sendCARSUpdate(String robotName, Object key, Object value);
	public abstract void startRobot(String robotName);
	public abstract void setOutputProducers(HashMap<String,ActiveMQProducer> producers);
	
	public void sendBackEvent(CARSVariableUpdate event, String reflectBackName) {
		String vehicleName = event.getVehicleName();
		String key = reflectBackName;
		String value = event.getValue();
		System.out.println("sendBackEvent: vehicleName=" + vehicleName + ",key=" + key + ",value=" + value);
		sendCARSUpdate(vehicleName, key, value);
	}
}
