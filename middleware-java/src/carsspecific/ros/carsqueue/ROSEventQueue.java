package carsspecific.ros.carsqueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.JMSException;

import com.fasterxml.jackson.core.JsonProcessingException;

import activemq.portmapping.PortMappings;

import atlasdsl.*;
import atlassharedclasses.*;
import carsspecific.moos.carsqueue.MOOSEvent;
import middleware.core.*;

public class ROSEventQueue extends CARSLinkEventQueue<MOOSEvent> {

	private static final boolean DEBUG_PRINT_DESERIALISED_MSGS = false;
	private static final boolean ALWAYS_REQUEST_CLASSIFICATION = true;

	private static final long serialVersionUID = 1L;
	private Pattern nodeReportScanner;
	private Pattern detectionScanner;
	private Pattern hazardScanner;
	private Mission mission;
	private List<ActiveMQConsumer> activeConsumers = new ArrayList<ActiveMQConsumer>();
	private ActiveMQProducer outputToCI;

	private class NoSensorForRobot extends Exception {
		NoSensorForRobot() {

		}
	}

	public ROSEventQueue(ATLASCore core, Mission mission, int queueCapacity) {
		super(core, queueCapacity, '.');
		this.mission = mission;
		nodeReportScanner = Pattern.compile("NAME=([^,]+),X=([^,]+),Y=([^,]+)");
		detectionScanner = Pattern.compile("x=([^,]+),y=([^,]+),label=([^,]+),vname=([^,]+)");
		hazardScanner = Pattern.compile("vname=([^,]+),x=([^,]+),y=([^,]+),label=([^,]+),type=([^,]+),color=([^,]+),width=([^,]+)");
		atlasOMapper = new ATLASObjectMapper();
		outputToCI = core.getCIProducer();
	}

	public void run() {
		super.run();
	}

	public void registerAfter() {

	}

	public void setup() {
		// Setup the ActiveMQ connection for the simulation side

		// Setup MOOS-side interface for the middleware
		for (Robot r : mission.getAllRobots()) {
			// TODO: either this class will be custom-generated, or
			// various generated hooks with be added at code generation time
			// TODO: change to ActiveMQRobotConsumer?
			String robotName = r.getName();
			ActiveMQConsumer consumer = new ActiveMQConsumer(robotName, (PortMappings.portForMOOSWatch(robotName)),
					this);
			activeConsumers.add(consumer);
			startThread(consumer, false);
		}

		for (Computer c : mission.getAllComputers()) {
			// TODO: either this class will be custom-generated, or
			// various generated hooks with be added at code generation time
			// TODO: change to ActiveMQRobotConsumer?
			ActiveMQConsumer consumer = new ActiveMQConsumer(c.getName(), (PortMappings.portForMOOSWatch(c.getName())),
					this);
			activeConsumers.add(consumer);
			startThread(consumer, false);
		}
	}

	// TODO: This has to be auto-generated to produce decoders for the specific
	// messages
	// once the test code below is working
	public void handleEventSpecifically(MOOSEvent e) {
		if (e instanceof CARSVariableUpdate) {
			CARSVariableUpdate mup = (CARSVariableUpdate) e;

			if (mup.keyMatches("DB_UPTIME")) {
				Double time = Double.valueOf(mup.getValue());
				try {
					// Update the core's internal time
					core.updateTime(time);
					// If no causality exception, create a time update notification
					// and send it on to the CI
					ATLASTimeUpdate tup = new ATLASTimeUpdate(time);
					String msg = atlasOMapper.serialise(tup);
					outputToCI.sendMessage(msg);

				} catch (CausalityException e1) {
					// If there is a causality error, it may come from merely
					// node reports being received out of order from different
					// vehicles. It is not necessarily fatal.
					// Log it anyway
					System.out.println("Causality exception - time diff " + e1.timeDiff());
				} catch (JsonProcessingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (JMSException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			// Handle NODE_REPORT events
			if (mup.keyStartMatches("NODE_REPORT")) {
				// Extract the x,y coordinates from the contents via regex
				String val = mup.getValue();
				Matcher m = nodeReportScanner.matcher(val);
				if (m.find()) {
					// TODO: exception checks for malformed value here
					String entityName = m.group(1);
					double x = Double.parseDouble(m.group(2));
					double y = Double.parseDouble(m.group(3));
					double speed = 0.0;

					// TODO: for now, assume the position sensor updates are provided directly
					// by base position
					GPSPositionReading gps = new GPSPositionReading(x, y, speed, entityName);

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

					// Find the corresponding robot object and update its location parameters
					Robot r = mission.getRobot(entityName);
					if (r != null) {
						r.setPointComponentProperty("location", new Point(x, y));
					} else {
						Computer c = mission.getComputer(entityName);
						if (c != null) {
							System.out.println("Robot or computer " + mup.getVehicleName()
									+ " not found in middleware internal state");
						}
					}
				}
			}

			if (mup.keyStartMatches("UHZ_HAZARD_REPORT")) {
				System.out.println("SENSOR HAZARD REPORT");
				String val = mup.getValue();
				System.out.println("Hazard report - raw contents " + val);
				Matcher mtch = hazardScanner.matcher(val);
				if (mtch.find()) {
					String vname = mtch.group(1);
					double x = Double.parseDouble(mtch.group(2));
					double y = Double.parseDouble(mtch.group(3));
					int objectID = Integer.parseInt(mtch.group(4));
					String type = mtch.group(5);
					String col = mtch.group(6);
					int width = Integer.parseInt(mtch.group(7));

					Robot r = mission.getRobot(vname);
					Optional<EnvironmentalObject> eo = mission.getEnvironmentalObject(objectID);
					if (r == null) {
						System.out.println(
								"DEBUG: robot not found for detection report: " + vname + "full update: " + val);
					} else {
						if (!eo.isPresent()) {
							System.out.println("DEBUG: detected object not registered in environment " + objectID
									+ "full update " + val);
						} else {
							try {
								System.out.println("INFO: sending out sensor detection");
								// TODO: need to work out what type of sensor is involved? is it a
								// sonar or camera? For now, we just assume the first sensor
								Optional<Sensor> s = r.getFirstSensor();
								if (!s.isPresent()) {
									throw new NoSensorForRobot();
								}
								SensorType st = s.get().getType();
								Message m = new Message(st.name() + "_DETECTION_" + vname.toUpperCase());
								SensorDetection d = new SensorDetection(m, st);
								d.setField("location", new Point(x, y));
								d.setField("robotName", vname);
								d.setField("objectID", objectID);
								d.setField("type", type);
								core.notifySensorDetection(d);

								String msg = atlasOMapper.serialise(d);

								if (DEBUG_PRINT_DESERIALISED_MSGS) {
									System.out.println("DEBUG: serialised message " + msg);
								}
								outputToCI.sendMessage(msg);
							} catch (JMSException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (JsonProcessingException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (NoSensorForRobot e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}
			}

			// Handle sonar detection report events
			if (mup.keyStartMatches("UHZ_DETECTION_REPORT")) {

				System.out.println("SENSOR DETECTION REPORT");
				String val = mup.getValue();
				System.out.println("detection report = " + val);
				Matcher mtch = detectionScanner.matcher(val);
				if (mtch.find()) {
					double x = Double.parseDouble(mtch.group(1));
					double y = Double.parseDouble(mtch.group(2));
					Integer objectID = Integer.parseInt(mtch.group(3));
					String vname = mtch.group(4);

					if (ALWAYS_REQUEST_CLASSIFICATION) {
						// Request classification of the detection from CARS
						core.getCARSTranslationOutput().sendCARSUpdate("shoreside", "UHZ_CLASSIFY_REQUEST",
								"vname=" + vname + ",label=" + objectID);
					}


				}
			}
		} else {
			// TODO: log a warning of an incompatible message
			System.out.println("DEBUG: MOOSEventQueue.handleEvent got incompatible object of class " + e.getClass());
		}
	}
}
