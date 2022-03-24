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
	private Map<Integer,Integer> remainingDetections = new HashMap<Integer,Integer>();
		
	private Optional<GoalResult> result = Optional.empty();
	
	private int verificationsNeededForBenignObject;
	private int verificationsNeededForMaliciousObject;
	
	private ATLASCore core;
	private Mission mission;
	
	public DiscoverObjects(List<EnvironmentalObject> targetObjects, int robotsNeededPerBenignObject, int robotsNeededPerMaliciousObject) {
		System.out.println("DiscoverObjects created");
		this.verificationsNeededForBenignObject = robotsNeededPerBenignObject;
		this.verificationsNeededForMaliciousObject = robotsNeededPerMaliciousObject;
		
		for (EnvironmentalObject eo : targetObjects) {
			locatedObjects.put(eo, new HashMap<String,DiscoveryRecord>());
			// Add one to these values to account for the original verification
			int remaining;
			if (eo.isHazard()) {
				remaining = 1 + robotsNeededPerMaliciousObject;
			} else {
				remaining = 1 + robotsNeededPerBenignObject;
			}
			remainingDetections.put(eo.getLabel(), remaining);
			ATLASLog.logGoalMessage(this, "LOOKINGFOR," + eo.getLabel() + "," + remaining);
		}
	}
	
	public void testIfComplete() {	
		// If so, set the GoalResult to represent that they are done
		boolean complete = true;
		for (Entry<EnvironmentalObject, Map<String, DiscoveryRecord>> me : locatedObjects.entrySet()) {
			EnvironmentalObject eo = me.getKey();
			Map<String, DiscoveryRecord> robots = me.getValue();
			int robotsNeededPerObject = eo.isHazard() ? verificationsNeededForMaliciousObject : verificationsNeededForBenignObject; 
			
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
	
	private void registerRobotDetection(int objectID, String robotName, double time, String type) {
		Optional<EnvironmentalObject> eo_o = mission.getEnvironmentalObject(objectID);
		
		if (eo_o.isPresent()) {
			EnvironmentalObject eo = eo_o.get();
			Map<String,DiscoveryRecord> m = locatedObjects.get(eo);
						
			// Add the robot to the map - if it is the first detection for that robotName
			if (m.get(robotName) == null) {
				DiscoveryRecord dr = new DiscoveryRecord(objectID, (Point)(eo), robotName, time);
				// decrement it, but not beyond zero
				int newDetections = Math.max(0, remainingDetections.get(objectID)-1);
				remainingDetections.put(objectID, newDetections);
				int remainingDetectionsForObject = remainingDetections.get(objectID);
				ATLASLog.logGoalMessage(this, "FOUND," + time + "," + robotName + "," + Integer.toString(objectID) + "," + remainingDetectionsForObject);
				m.put(robotName,dr);
			}
		}
	}

	protected void setup(ATLASCore core, Mission mission, Goal g) throws GoalActionSetupFailure {
		this.mission = mission;
		core.setupSensorWatcher((detection) -> 
		{
				double time = core.getTime();
				int objectID = (int)detection.getField("objectID");
				String robotName = (String) detection.getField("robotName");
				String type = (String)detection.getField("type");
				registerRobotDetection(objectID,robotName,time,type);
		});
	}
	
	public int verificationsBenign() {
		return verificationsNeededForBenignObject;
	}
	
	public int verificationsMalicious() {
		return verificationsNeededForMaliciousObject;
	}
}
