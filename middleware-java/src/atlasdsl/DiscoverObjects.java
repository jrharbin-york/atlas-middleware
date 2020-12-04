package atlasdsl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import atlasdsl.GoalResult.GoalResultStatus;
import atlassharedclasses.Point;
import middleware.core.ATLASCore;
import middleware.logging.ATLASLog;

public class DiscoverObjects extends GoalAction {
	
	private class DiscoveryRecord {
		private Point location;
		private String robotName;
		private double time;
		private int objectID;

		
		public DiscoveryRecord(int objectID, Point location, String robotName, double time) {
			this.objectID = objectID;
			this.location = location;
			this.robotName = robotName;
			this.time = time;
		}
	}
	
	// How to get get notifications that the discovery has been made?
	
	// A map per object, which names the robots that have located this particular object
	private Map<EnvironmentalObject,Map<String,DiscoveryRecord>> locatedObjects = new HashMap<EnvironmentalObject,Map<String,DiscoveryRecord>>();
	private Optional<GoalResult> result = Optional.empty();
	private int robotsNeededPerObject;
	// TODO: need to ask the core for the detection logs! - stored when oncoming from the simulator
	// may be impacted by faults
	private ATLASCore core;
	private Mission mission;
	
	public DiscoverObjects(List<EnvironmentalObject> targetObjects, int robotsNeededPerObject) {
		System.out.println("DiscoverObjects created");
		this.robotsNeededPerObject = robotsNeededPerObject;
		for (EnvironmentalObject eo : targetObjects) {
			locatedObjects.put(eo, new HashMap<String,DiscoveryRecord>());
		}
	}
	
	public void updateDistanceToObjects() {
		// TODO: track the robot distance to their respective objects
		// Maintain the minimal distance of any robot to each object, update it improved
	}
	
	public void testIfComplete() {	
		// If so, set the GoalResult to represent that they are done
		boolean complete = true;
		for (Entry<EnvironmentalObject, Map<String, DiscoveryRecord>> me : locatedObjects.entrySet()) {
			EnvironmentalObject eo = me.getKey();
			Map<String, DiscoveryRecord> robots = me.getValue();
			if (robots.size() < robotsNeededPerObject) {
				complete = false;
			}
		}
		
		if (complete) {
			result = Optional.of(new GoalResult(GoalResultStatus.COMPLETED));
		}
	}
	
	protected Optional<GoalResult> test(Mission mission, GoalParticipants participants) {
		// The GoalResult is ready when they are all ready
		testIfComplete();
		return result;
	}
	
	private synchronized void registerRobotDetection(int objectID, String robotName, double time) {
		Optional<EnvironmentalObject> eo_o = mission.getEnvironmentalObject(objectID);
		
		if (eo_o.isPresent()) {
			EnvironmentalObject eo = eo_o.get();
			Map<String,DiscoveryRecord> m = locatedObjects.get(eo);
						
			// Add the robot to the map - if it is the first detection for that robotName
			if (m.get(robotName) == null) {
				DiscoveryRecord dr = new DiscoveryRecord(objectID, (Point)(eo), robotName, time);
				ATLASLog.logGoalMessage(this, time + "," + robotName + "," + Integer.toString(objectID));
				m.put(robotName,dr);
			}
		}
	}

	protected void setup(ATLASCore core, Mission mission, Goal g) throws GoalActionSetupFailure {
		this.mission = mission;
		core.setupSensorWatcher((detection) -> 
		{
			// Need to dispatch on the sensor type!
			// For now, all sensors trigger it!
			//if (detection.getSensorType() == SensorType.SONAR) {
				double time = core.getTime();
				int objectID = (int) detection.getField("objectID");
				String robotName = (String) detection.getField("robotName");
				registerRobotDetection(objectID,robotName,time);
			//}
		});
	}
}
