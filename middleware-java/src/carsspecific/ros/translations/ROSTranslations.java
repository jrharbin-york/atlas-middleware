package carsspecific.ros.translations;

import java.util.HashMap;
import javax.jms.JMSException;
import middleware.carstranslations.CARSTranslations;
import middleware.core.ActiveMQProducer;

public class ROSTranslations extends CARSTranslations {
	
	HashMap<String,ActiveMQProducer> producers;
	
	public ROSTranslations() {

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
			prod.sendMessage(msg);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void startRobot(String robotName) { 
		sendCARSUpdate(robotName, "MOOS_MANUAL_OVERRIDE", "false");
	}
}
