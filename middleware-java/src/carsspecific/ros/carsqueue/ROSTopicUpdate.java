package carsspecific.ros.carsqueue;

import javax.json.JsonObject;
import edu.wpi.rail.jrosbridge.messages.Message;

public class ROSTopicUpdate extends ROSEvent {
	// TODO: vehicle name is not always present, e.g. top level topics without a uav_... name
	private String vehicleName;
	
	// This is the lOCAL topic name in the vehicle hierarchy, e.g. without
	// uav_n prefix
	private String topicName;
	
	private JsonObject json;
	//private Message message;
	private double time;
	private ATLASTag atlasTag;
	private String rosType;
	
	public enum ATLASTag {
		VELOCITY,
		ODOMETRY,
		POSE,
		FUZZING_VAR,
		GOALSTATE_VAR,
		SIMULATOR_GENERAL
	}
	
	public ROSTopicUpdate(String vehicleName, ATLASTag atlasTag, String topicName, Message message, double time, String rosType) {
		this.vehicleName = vehicleName;
		this.topicName = topicName;
		//this.message = message;
		this.time = time;
		this.atlasTag = atlasTag;
		this.json = message.toJsonObject();
		this.rosType = rosType;
	}
	
	public ROSTopicUpdate(ROSTopicUpdate rtu) {
		this.vehicleName = rtu.vehicleName;
		this.topicName = rtu.topicName;
		//this.message = rtu.message;
		this.time = rtu.time;
		this.atlasTag = rtu.atlasTag;
		this.json = rtu.json;
		this.rosType = rtu.rosType;
	}
	
	public ROSTopicUpdate(ROSTopicUpdate rtu, JsonObject newJSON) {
		this.vehicleName = rtu.vehicleName;
		this.topicName = rtu.topicName;
		this.time = rtu.time;
		this.atlasTag = rtu.atlasTag;
		this.json = newJSON;
		this.rosType = rtu.rosType;
	}

	public ROSTopicUpdate(ATLASTag tag, String fullTopicName, Message message, double time, String rosType) {
		this.topicName = fullTopicName;
		this.vehicleName = "";
		this.time = time;
		this.atlasTag = tag;
		this.json = message.toJsonObject();
		this.rosType = rosType;
	}

	public boolean tagEquals(ATLASTag t) {
		return atlasTag.equals(t);
	}
	
	//public Message getMessage() {
//		return message;
//	}
	
	public JsonObject getJSON() {
		return json;
	}
	
	public String getVehicleName() {
		return vehicleName;
	}
	
	public String getTopicName() {
		return topicName;
	}
	
	public double getTime() {
		return time;
	}
	
	public String getRosType() {
		return rosType;
	}
}