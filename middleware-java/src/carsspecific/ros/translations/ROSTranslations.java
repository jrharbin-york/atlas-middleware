package carsspecific.ros.translations;

import java.util.HashMap;
import java.util.List;
import atlassharedclasses.Point;
import edu.wpi.rail.jrosbridge.Ros;
import edu.wpi.rail.jrosbridge.Topic;
import edu.wpi.rail.jrosbridge.messages.Message;
import middleware.carstranslations.CARSTranslations;
import middleware.core.ActiveMQProducer;

public class ROSTranslations extends CARSTranslations {
	HashMap<String,ActiveMQProducer> producers;
	Ros ros;
	
	public ROSTranslations(Ros ros) throws CARSTransationSetupFailed {
		if (ros == null) {
			throw new CARSTransationSetupFailed("ROS object reference is null");
		} else {
			this.ros = ros;
		}
	}
	
	public void setOutputProducers(HashMap<String,ActiveMQProducer> producers) {
		this.producers = producers;
	}
	
	public synchronized void sendCARSUpdate(String robotName, Object topicName_o, Object value) {
		// TODO: this only supports string messages!
		String topicName = topicName_o.toString();
		Topic tOut = new Topic(ros, topicName, "std_msgs/String");
		Message toSend = new Message("{\"data\": \""+  value + "\"}");
		tOut.publish(toSend);
	}
	
	public synchronized void vehicleStatusChange(String robotName, boolean newStatus) { 
		System.out.println("ROSTranslations: startRobot unimplemented");
	}

	public void setCoordinates(String robotName, List<Point> coords) {
		System.out.println("ROSTranslations: setCoordinates unimplemented");	
	}

	public void returnHome(String robotName) {
		//System.out.println("ROSTranslations: setCoordinates unimplemented");
		//sendCARSUpdate(robotName, robotName + "/rooms", "");
		sendCARSUpdate(robotName, robotName + "/go_home", "HOME");
	}

	public void setCoordinates(String robotName, List<Point> coords, int repeatCount) {
		
	}

	public void startVehicle(String robotName) {
		
	}

	public void stopVehicle(String robotName) {
		// TODO: this is hardcoded for the current healthcare case study
		sendCARSUpdate(robotName, robotName + "/battery_empty_stop", "STOP");
	}

	public void simulatorVariableChange(String robotName, String key, String value, boolean vehicleSpecific) {
		String topicName;
		if (vehicleSpecific) {
			topicName = robotName + "/" + key;
		} else {
			topicName = key;
		}
		sendCARSUpdate(robotName, topicName, value);
	}
}
