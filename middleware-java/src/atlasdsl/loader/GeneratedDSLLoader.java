package atlasdsl.loader;

import atlasdsl.*;
import atlasdsl.faults.*;
import atlassharedclasses.*;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

public class GeneratedDSLLoader implements DSLLoader {
	public Mission loadMission() throws DSLLoadFailed {
	final double MISSION_END_TIME = 500.0;
	Mission mission = new Mission(MISSION_END_TIME, false);
	
	Computer c1 = new Computer("controller");
	mission.addComputer(c1);
	
		Robot rtb3_0 = new Robot("tb3_0");
		rtb3_0.setPointComponentProperty("startLocation", new Point(-85.0,-150.0,0.0));
		rtb3_0.setDoubleComponentProperty("maxSpeed", 5.0);
		rtb3_0.setDoubleComponentProperty("startSpeed", 1.5);
		rtb3_0.setDoubleComponentProperty("maxDepth", 0.0);
		
 
		Sensor srtb3_0_1 = new Sensor(SensorType.SONAR);
		srtb3_0_1.setParent(rtb3_0);
		srtb3_0_1.setDoubleComponentProperty("swathWidth", 20.0);
		srtb3_0_1.setDoubleComponentProperty("detectionProb", 0.99);
		rtb3_0.addSubcomponent(srtb3_0_1);
			
			
 
		Sensor srtb3_0_2 = new Sensor(SensorType.GPS_POSITION);
		srtb3_0_2.setParent(rtb3_0);
		rtb3_0.addSubcomponent(srtb3_0_2);
			
			
 
			
			MotionSource srtb3_0_3 = new MotionSource(5.0);
			rtb3_0.addSubcomponent(srtb3_0_3);
			
			
		mission.addRobot(rtb3_0);
		Robot rtb3_1 = new Robot("tb3_1");
		rtb3_1.setPointComponentProperty("startLocation", new Point(190.0,-150.0,0.0));
		rtb3_1.setDoubleComponentProperty("maxSpeed", 5.0);
		rtb3_1.setDoubleComponentProperty("startSpeed", 1.5);
		rtb3_1.setDoubleComponentProperty("maxDepth", 0.0);
		
 
		Sensor srtb3_1_1 = new Sensor(SensorType.SONAR);
		srtb3_1_1.setParent(rtb3_1);
		srtb3_1_1.setDoubleComponentProperty("swathWidth", 10.0);
		srtb3_1_1.setDoubleComponentProperty("detectionProb", 0.99);
		rtb3_1.addSubcomponent(srtb3_1_1);
			
			
 
		Sensor srtb3_1_2 = new Sensor(SensorType.GPS_POSITION);
		srtb3_1_2.setParent(rtb3_1);
		rtb3_1.addSubcomponent(srtb3_1_2);
			
			
 
			
			MotionSource srtb3_1_3 = new MotionSource(5.0);
			rtb3_1.addSubcomponent(srtb3_1_3);
			
			
		mission.addRobot(rtb3_1);
		Robot rtb3_2 = new Robot("tb3_2");
		rtb3_2.setPointComponentProperty("startLocation", new Point(-85.0,-45.0,0.0));
		rtb3_2.setDoubleComponentProperty("maxSpeed", 5.0);
		rtb3_2.setDoubleComponentProperty("startSpeed", 1.5);
		rtb3_2.setDoubleComponentProperty("maxDepth", 0.0);
		
 
		Sensor srtb3_2_1 = new Sensor(SensorType.SONAR);
		srtb3_2_1.setParent(rtb3_2);
		srtb3_2_1.setDoubleComponentProperty("swathWidth", 25.0);
		srtb3_2_1.setDoubleComponentProperty("detectionProb", 0.99);
		rtb3_2.addSubcomponent(srtb3_2_1);
			
			
 
		Sensor srtb3_2_2 = new Sensor(SensorType.GPS_POSITION);
		srtb3_2_2.setParent(rtb3_2);
		rtb3_2.addSubcomponent(srtb3_2_2);
			
			
 
			
			MotionSource srtb3_2_3 = new MotionSource(5.0);
			rtb3_2.addSubcomponent(srtb3_2_3);
			
			
		mission.addRobot(rtb3_2);
	
	
	
	
 
 
 
		
		Robot [] grp1 = {rtb3_0,rtb3_1,rtb3_2}; 
		GoalParticipants gpmutualAvoidance = new StaticParticipants(grp1, mission);
		
		
		
		GoalTemporalConstraints gt1 = new GoalTemporalConstraints(0.0, 1200.0);
		
		
		GoalAction ga1 = new AvoidOthers(4.0);
		
		
		
		
		GoalRegion grmutualAvoidance = new StaticGoalRegion(
			new Region(new Point(0.0, 0.0, 0.0),
			           new Point(1000.0, 1000.0, 0.0)));
		
		
		Goal mutualAvoidance = new Goal("mutualAvoidance", mission, gt1, gpmutualAvoidance, Optional.of(grmutualAvoidance), ga1);
		
		
		mission.addGoal("mutualAvoidance", mutualAvoidance);
 
 
 
		
		Robot [] grp2 = {rtb3_0,rtb3_1,rtb3_2}; 
		GoalParticipants gpstayInRegion = new StaticParticipants(grp2, mission);
		
		
		
		GoalTemporalConstraints gt2 = new GoalTemporalConstraints(0.0, 1200.0);
		
		
		
		
		GoalAction ga2 = new StayInRegion(false);
		
		
		GoalRegion grstayInRegion = new StaticGoalRegion(
			new Region(new Point(-150.0, -260.0, -40.0),
			           new Point(245.0, 20.0, 100.0)));
		
		
		Goal stayInRegion = new Goal("stayInRegion", mission, gt2, gpstayInRegion, Optional.of(grstayInRegion), ga2);
		
		
		mission.addGoal("stayInRegion", stayInRegion);
 
 
 
		
		Robot [] grp3 = {rtb3_0,rtb3_1,rtb3_2}; 
		GoalParticipants gptrackDistances = new StaticParticipants(grp3, mission);
		
		
		
		GoalTemporalConstraints gt3 = new GoalTemporalConstraints(0.0, 1190.0);
		
		
		
		GoalAction ga3 = new TrackDistances();
		
		
		
		GoalRegion grtrackDistances = new StaticGoalRegion(
			new Region(new Point(-150.0, -260.0, -40.0),
			           new Point(245.0, 20.0, 100.0)));
		
		
		Goal trackDistances = new Goal("trackDistances", mission, gt3, gptrackDistances, Optional.of(grtrackDistances), ga3);
		
		
		mission.addGoal("trackDistances", trackDistances);
	

	
 
	Message msgDETECTION_FRANK = new Message("DETECTION_FRANK", rtb3_0, c1);
	mission.addMessage(msgDETECTION_FRANK); 
 
	Message msgDETECTION_GILDA = new Message("DETECTION_GILDA", rtb3_1, c1);
	mission.addMessage(msgDETECTION_GILDA); 
 
	Message msgDETECTION_HENRY = new Message("DETECTION_HENRY", srtb3_1_3, c1);
	mission.addMessage(msgDETECTION_HENRY); 
	
	
	return mission;
	}
}