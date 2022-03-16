package carsspecific.ros.carsqueue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.jms.JMSException;
import javax.json.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import atlasdsl.*;
import atlassharedclasses.ATLASObjectMapper;
import atlassharedclasses.GPSPositionReading;
import atlassharedclasses.Point;
import atlassharedclasses.SpeedReading;
import carsspecific.ros.carsqueue.ROSTopicUpdate.ATLASTag;
import carsspecific.ros.connection.ROSConnection;

import edu.wpi.rail.jrosbridge.*;
import edu.wpi.rail.jrosbridge.callback.TopicCallback;
import edu.wpi.rail.jrosbridge.messages.Message;
import fuzzingengine.FuzzingEngine;
import fuzzingengine.FuzzingSimMapping;
import fuzzingengine.FuzzingSimMapping.VariableSpecification;
import middleware.core.*;

public class ROSEventQueue_AMCL extends CARSLinkEventQueue<ROSEvent> {
	// This is used in the subscriptions to ensure we do not duplicate them - e.g.
	// by subscribing twice to the same topic
	private Map<String, Boolean> topicSubscriptions = new HashMap<String, Boolean>();

	private final boolean DEBUG_PRINT_RAW_MESSAGE = true;
	private final boolean DEBUG_PRINT_CLOCK_MESSAGES = false;
	
	private final String USE_CLOCK_TOPIC = "/clock";
	
	private Mission mission;
	private Ros ros;
	private static final long serialVersionUID = 1L;

	private static final boolean DEBUG_PRINT_DESERIALISED_MSGS = true;
	private FuzzingEngine fuzzEngine;
	
	private ActiveMQProducer outputToCI;

	private HashMap<String, Double> robotSpeeds = new HashMap<String, Double>();

	public void setupSpeeds() {
		for (Robot r : mission.getAllRobots()) {
			robotSpeeds.put(r.getName(), 0.0);
		}
	}
	
	public Ros getROS() {
		return ros;
	}

	public ROSEventQueue_AMCL(ATLASCore core, Mission mission, int queueCapacity, FuzzingEngine fuzzEngine) {
		super(core, queueCapacity, '.');
		this.mission = mission;
		this.fuzzEngine = fuzzEngine;
		setupSpeeds();
		atlasOMapper = new ATLASObjectMapper();
		outputToCI = core.getCIProducer();
	}

	public void run() {
		super.run();
	}

	public void registerAfter() {

	}

	public void handleEventSpecifically(ROSEvent e) {
		System.out.println("EventClass=" + e.getClass().getSimpleName());
		if (e instanceof ROSTopicUpdate) {
			ROSTopicUpdate rtu = (ROSTopicUpdate) e;
			if (rtu.tagEquals(ATLASTag.SIMULATOR_GENERAL)) {
				if (rtu.getTopicName().equals(USE_CLOCK_TOPIC)) {
					if (DEBUG_PRINT_CLOCK_MESSAGES) {
						System.out.println("Clock msg = " + rtu.getJSON());
					}
					JsonObject j = rtu.getJSON().getJsonObject("clock");
					JsonNumber secs = j.getJsonNumber("secs");
					JsonNumber nsecs = j.getJsonNumber("nsecs");
					double time = secs.doubleValue() + nsecs.doubleValue() / 1e9;
					try {
						core.updateTime(time);
					} catch (CausalityException e1) {
						e1.printStackTrace();
					}
				}
			}

			if (rtu.tagEquals(ATLASTag.ODOMETRY)) {
				JsonObject j = rtu.getJSON();
				JsonObject pose1 = j.getJsonObject("pose");
				JsonObject pose2 = pose1.getJsonObject("pose");
				JsonObject pos = pose2.getJsonObject("position");
				JsonNumber jx = pos.getJsonNumber("x");
				JsonNumber jy = pos.getJsonNumber("y");
				JsonNumber jz = pos.getJsonNumber("z");
				Point p = new Point(jx.doubleValue(), jy.doubleValue(), jz.doubleValue());
				System.out.println("ODometry Point:" + p.toString());
				System.out.println();
				GPSPositionReading gps = new GPSPositionReading(p, rtu.getVehicleName());
				core.notifyPositionUpdate(gps);
				
				try {
					String msg = atlasOMapper.serialise(gps);

					if (DEBUG_PRINT_DESERIALISED_MSGS) {
						System.out.println("DEBUG: serialised message " + msg);
					}
					outputToCI.sendMessage(msg);
				} catch (JsonProcessingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (JMSException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			if (rtu.tagEquals(ATLASTag.VELOCITY)) {
				JsonObject j = rtu.getJSON();
				JsonObject tw = j.getJsonObject("twist");
//				JsonObject linear = tw.getJsonObject("linear");
//				JsonNumber jx = linear.getJsonNumber("x");
//				JsonNumber jy = linear.getJsonNumber("y");
//				JsonNumber jz = linear.getJsonNumber("z");
//				Point vel = new Point(jx.doubleValue(), jy.doubleValue(), jz.doubleValue());
//
//				double s = vel.absLength();
//				SpeedReading sr = new SpeedReading(s, rtu.getVehicleName());
//				core.notifySpeedUpdate(sr);
				
				//System.out.println("Vel:" + vel.toString());
			}
		}
	}

	private void standardSubscribe(String fullTopicName, String rosType, ATLASTag tag) {
		if (topicSubscriptions.containsKey(fullTopicName)) {
			System.out.println("Ignoring second subscription attempt to " + fullTopicName);
		} else {
			ROSEventQueue_AMCL rosQueue = this;
			Topic t = new Topic(ros, fullTopicName, rosType);
			topicSubscriptions.put(fullTopicName, true);
			t.subscribe(new TopicCallback() {
				@Override
				public void handleMessage(Message message) {
					if (DEBUG_PRINT_RAW_MESSAGE) {
						System.out.println("From ROSbridge tagged: " + tag.toString() + ":" + message.toString());
					}

					ROSEvent rev = new ROSTopicUpdate(tag, fullTopicName, message, core.getTime(), rosType);
					rosQueue.add(rev);
				}
			});
		}
	}

	private void standardSubscribeVehicle(String vehicleName, ATLASTag tag, String topicName, String rosType) {
		String topicNameFull = "/" + vehicleName + topicName;
		if (topicSubscriptions.containsKey(topicNameFull)) {
			System.out.println("Ignoring second subscription attempt to " + topicNameFull);
		} else {
			System.out.println("Subscribing to " + topicNameFull);
			ROSEventQueue_AMCL rosQueue = this;
			Topic t = new Topic(ros, topicNameFull, rosType);
			topicSubscriptions.put(topicNameFull, true);
			t.subscribe(new TopicCallback() {
				@Override
				public void handleMessage(Message message) {
					if (DEBUG_PRINT_RAW_MESSAGE) {
						System.out.println("From ROSbridge " + topicNameFull + " tagged: " + tag.toString() + ":" + message.toString());
					}

					ROSEvent rev = new ROSTopicUpdate(vehicleName, tag, topicName, message, core.getTime(), rosType);
					rosQueue.add(rev);
					core.setGoalVariable(vehicleName, topicName, message);
				}
			});
		}
	}

//	private void subscribeForStandardVehicleTopics(String vehicleName) {
//		// We always require the position and velocity of vehicles for the middleware
//		// state
//		String velTopicName = "/ual/velocity";
//		String posTopicName = "/ual/pose";
//		String velType = "geometry_msgs/TwistStamped";
//		String posType = "geometry_msgs/PoseStamped";
//		standardSubscribeVehicle(vehicleName, ATLASTag.VELOCITY, velTopicName, velType);
//		standardSubscribeVehicle(vehicleName, ATLASTag.POSE, posTopicName, posType);
//	}

	private void subscribeForStandardVehicleTopics(String vehicleName) {
		// We always require the position and velocity of vehicles for the middleware
		// state
		String velTopicName = "/cmd_vel";
		String posTopicName = "/amcl_pose";
		String velType = "geometry_msgs/Twist";
		String posType = "geometry_msgs/PoseWithCovarianceStamped";
		standardSubscribeVehicle(vehicleName, ATLASTag.VELOCITY, velTopicName, velType);
		standardSubscribeVehicle(vehicleName, ATLASTag.ODOMETRY, posTopicName, posType);
	}

	private void subscribeForFuzzingTopics() {
		// TODO: need a connection from the fuzzing engine to this queue

		// Need the types as well for the keys
		// Also need to know if the topics are per-robot or not...

		FuzzingSimMapping spec = fuzzEngine.getSpec();
		Map<String, VariableSpecification> vspec = spec.getRecords();
		for (Map.Entry<String, VariableSpecification> entry : vspec.entrySet()) {
			String topicName = entry.getKey();
			VariableSpecification v = entry.getValue();
			if (v.isVehicleSpecific()) {
				// TODO: Uses the regexp as a type - rename this to type
				Optional<String> rosType_o = v.getRegexp();
				if (rosType_o.isPresent()) {
					String rosType = rosType_o.get();
					for (Robot r : mission.getAllRobots()) {
						standardSubscribeVehicle(r.getName(), ATLASTag.FUZZING_VAR, topicName, rosType);
					}
				} else {
					System.out.println("Could not set up ROS subscription for fuzzing variable " + topicName
							+ " as no type defined");
				}
			} else {
				// Not vehicle specific fuzzing key

				// TODO: Uses the regexp as a type - rename this to type
				Optional<String> rosType_o = v.getRegexp();
				if (rosType_o.isPresent()) {
					String rosType = rosType_o.get();
					standardSubscribe(topicName, rosType, ATLASTag.FUZZING_VAR);
				}
			}
		}
	}

	private void subscribeForGoalTopics() {
		for (Goal g : mission.getGoals()) {
			for (GoalVariable gv : g.getGoalVariables()) {
				if (gv.isVehicleSpecific()) {
					// TODO: should we only subscribe for participants?
					for (Robot r : mission.getAllRobots()) {
						standardSubscribeVehicle(r.getName(), ATLASTag.GOALSTATE_VAR, gv.getName(),
								gv.getVariableType());
					}
				} else {
					standardSubscribe(gv.getName(), gv.getVariableType(), ATLASTag.GOALSTATE_VAR);
				}
			}
		}
	}

	private void subscribeForSimulatorTopics() {
		standardSubscribe(USE_CLOCK_TOPIC, "rosgraph_msgs/Clock", ATLASTag.SIMULATOR_GENERAL);
	}

	public void setup() {
		final String AMCL_HEALTHCARE_HOST = "localhost";
		final int AMCL_HEALTHCARE_PORT = 9090;
		ros = ROSConnection.getConnection(AMCL_HEALTHCARE_HOST, AMCL_HEALTHCARE_PORT).getROS();

		// Iterate over all the robots in the DSL, set up subscriptions for position and
		// velocity
		for (Robot r : mission.getAllRobots()) {
			subscribeForStandardVehicleTopics(r.getName());
		}

		subscribeForSimulatorTopics();
		subscribeForFuzzingTopics();
		subscribeForGoalTopics();
	}

	public void close() {
		ros.disconnect();
	}
}
