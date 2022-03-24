package atlasdsl.loader;

import atlasdsl.*;
import atlasdsl.faults.*;
import atlassharedclasses.*;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

public class GeneratedDSLLoader implements DSLLoader {
	public Mission loadMission() throws DSLLoadFailed {
	final double MISSION_END_TIME = 1500.0;
	Mission mission = new Mission(MISSION_END_TIME, true);
	
	mission.addSimulatorVariable(new SimulatorVariable("/clock", "rosgraph_msgs/Clock", SimulatorVariable.VariableTag.TIME, false, false));
	mission.addSimulatorVariable(new SimulatorVariable("/amcl_pose", "geometry_msgs/PoseWithCovarianceStamped", SimulatorVariable.VariableTag.POSITION, true, true));
	mission.addSimulatorVariable(new SimulatorVariable("/roomCompleted", "std_msgs/Int32", SimulatorVariable.VariableTag.GENERIC, true, true));
	
	Computer c1 = new Computer("controller");
	mission.addComputer(c1);
	
		Robot rtb3_0 = new Robot("tb3_0");
		rtb3_0.setPointComponentProperty("startLocation", new Point(3.46,-3.6,0.0));
		rtb3_0.setDoubleComponentProperty("maxSpeed", 5.0);
		rtb3_0.setDoubleComponentProperty("startSpeed", 1.5);
		
 
			
			
		Sensor srtb3_0_1 = new Sensor(SensorType.GPS_POSITION);
		srtb3_0_1.setParent(rtb3_0);
		rtb3_0.addSubcomponent(srtb3_0_1);
			
			
 
			
			
			
			MotionSource srtb3_0_2 = new MotionSource();
			rtb3_0.addSubcomponent(srtb3_0_2);
			
 
			
			Battery srtb3_0_3 = new Battery(200); 
			srtb3_0_3.setParent(rtb3_0);
			rtb3_0.addSubcomponent(srtb3_0_3);
			
			
			
			
		mission.addRobot(rtb3_0);
		Robot rtb3_1 = new Robot("tb3_1");
		rtb3_1.setPointComponentProperty("startLocation", new Point(3.46,-3.6,0.0));
		rtb3_1.setDoubleComponentProperty("maxSpeed", 5.0);
		rtb3_1.setDoubleComponentProperty("startSpeed", 1.5);
		
 
			
			
		Sensor srtb3_1_1 = new Sensor(SensorType.GPS_POSITION);
		srtb3_1_1.setParent(rtb3_1);
		rtb3_1.addSubcomponent(srtb3_1_1);
			
			
 
			
			
			
			MotionSource srtb3_1_2 = new MotionSource();
			rtb3_1.addSubcomponent(srtb3_1_2);
			
 
			
			Battery srtb3_1_3 = new Battery(100); 
			srtb3_1_3.setParent(rtb3_1);
			rtb3_1.addSubcomponent(srtb3_1_3);
			
			
			
			
		mission.addRobot(rtb3_1);
		Robot rtb3_2 = new Robot("tb3_2");
		rtb3_2.setPointComponentProperty("startLocation", new Point(-5.59,-3.23,0.0));
		rtb3_2.setDoubleComponentProperty("maxSpeed", 5.0);
		rtb3_2.setDoubleComponentProperty("startSpeed", 1.5);
		
 
			
			
		Sensor srtb3_2_1 = new Sensor(SensorType.GPS_POSITION);
		srtb3_2_1.setParent(rtb3_2);
		rtb3_2.addSubcomponent(srtb3_2_1);
			
			
 
			
			
			
			MotionSource srtb3_2_2 = new MotionSource();
			rtb3_2.addSubcomponent(srtb3_2_2);
			
 
			
			Battery srtb3_2_3 = new Battery(100); 
			srtb3_2_3.setParent(rtb3_2);
			rtb3_2.addSubcomponent(srtb3_2_3);
			
			
			
			
		mission.addRobot(rtb3_2);
	
	
	
	
 
 
 
		
		Robot [] grp1 = {rtb3_0,rtb3_1,rtb3_2}; 
		GoalParticipants gpmutualAvoidance = new StaticParticipants(grp1, mission);
		
		
		
		GoalTemporalConstraints gt1 = new GoalTemporalConstraints(0.0, 500.0);
		
		
		GoalAction ga1 = new AvoidOthers(0.3);
		
		
		
		
		
		
		GoalRegion grmutualAvoidance = new StaticGoalRegion(
			new Region(new Point(0.0, 0.0, 0.0),
			           new Point(1000.0, 1000.0, 0.0)));
		
		
		Goal mutualAvoidance = new Goal("mutualAvoidance", mission, gt1, gpmutualAvoidance, Optional.of(grmutualAvoidance), ga1);
		
		
		mission.addGoal("mutualAvoidance", mutualAvoidance);
 
 
 
		
		Robot [] grp2 = {rtb3_0,rtb3_1,rtb3_2}; 
		GoalParticipants gptrackDistances = new StaticParticipants(grp2, mission);
		
		
		
		GoalTemporalConstraints gt2 = new GoalTemporalConstraints(0.0, 1190.0);
		
		
		
		GoalAction ga2 = new TrackDistances();
		
		
		
		
		
		GoalRegion grtrackDistances = new StaticGoalRegion(
			new Region(new Point(-150.0, -260.0, -40.0),
			           new Point(245.0, 20.0, 100.0)));
		
		
		Goal trackDistances = new Goal("trackDistances", mission, gt2, gptrackDistances, Optional.of(grtrackDistances), ga2);
		
		
		mission.addGoal("trackDistances", trackDistances);
 
 
 
		
		Robot [] grp3 = {rtb3_0,rtb3_1,rtb3_2}; 
		GoalParticipants gpcheckRoomsCompleted = new StaticParticipants(grp3, mission);
		
		
		
		GoalTemporalConstraints gt3 = new GoalTemporalConstraints(0.0, 1500.0);
		
		
		
		
		
		GoalAction ga3 = new CheckRoomsCompleted();
		
		
		
		GoalRegion grcheckRoomsCompleted = new StaticGoalRegion(
			new Region(new Point(0.0, 0.0, 0.0),
			           new Point(1000.0, 1000.0, 0.0)));
		
		
		Goal checkRoomsCompleted = new Goal("checkRoomsCompleted", mission, gt3, gpcheckRoomsCompleted, Optional.of(grcheckRoomsCompleted), ga3);
		
		
		mission.addGoal("checkRoomsCompleted", checkRoomsCompleted);
 
 
 
		
		Robot [] grp4 = {rtb3_0,rtb3_1,rtb3_2}; 
		GoalParticipants gptrackEnergyHealthcare = new StaticParticipants(grp4, mission);
		
		
		
		GoalTemporalConstraints gt4 = new GoalTemporalConstraints(0.0, 1500.0);
		
		
		
		
		GoalAction ga4 = new TrackEnergyConsumption(10.0, 0.1, 100.0);
		
		
		
		
		GoalRegion grtrackEnergyHealthcare = new StaticGoalRegion(
			new Region(new Point(0.0, 0.0, 0.0),
			           new Point(10.0, 10.0, 2.0)));
		
		
		Goal trackEnergyHealthcare = new Goal("trackEnergyHealthcare", mission, gt4, gptrackEnergyHealthcare, Optional.of(grtrackEnergyHealthcare), ga4);
		
		
		mission.addGoal("trackEnergyHealthcare", trackEnergyHealthcare);
	

	
	
	
	return mission;
	}
}