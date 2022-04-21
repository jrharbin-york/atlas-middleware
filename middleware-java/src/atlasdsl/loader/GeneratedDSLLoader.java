package atlasdsl.loader;

import atlasdsl.*;
import atlasdsl.faults.*;
import atlassharedclasses.*;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

public class GeneratedDSLLoader implements DSLLoader {
	public Mission loadMission() throws DSLLoadFailed {
	final double MISSION_END_TIME = 450.0;
	Mission mission = new Mission(MISSION_END_TIME, true);
	
	mission.addSimulatorVariable(new SimulatorVariable("/clock", "rosgraph_msgs/Clock", SimulatorVariable.VariableTag.TIME, false, false));
	mission.addSimulatorVariable(new SimulatorVariable("/amcl_pose", "geometry_msgs/PoseWithCovarianceStamped", SimulatorVariable.VariableTag.POSITION, true, true));
	mission.addSimulatorVariable(new SimulatorVariable("/roomCompleted", "std_msgs/Int32", SimulatorVariable.VariableTag.GENERIC, true, true));
	mission.addSimulatorVariable(new SimulatorVariable("/rooms", "std_msgs/String", SimulatorVariable.VariableTag.GENERIC, true, false));
	
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
			
 
			
			Battery srtb3_0_3 = new Battery(220000); 
			srtb3_0_3.setParent(rtb3_0);
			rtb3_0.addSubcomponent(srtb3_0_3);
			
			
			
			
		mission.addRobot(rtb3_0);
		Robot rtb3_1 = new Robot("tb3_1");
		rtb3_1.setPointComponentProperty("startLocation", new Point(3.45,-3.24,0.0));
		rtb3_1.setDoubleComponentProperty("maxSpeed", 5.0);
		rtb3_1.setDoubleComponentProperty("startSpeed", 1.5);
		
 
			
			
		Sensor srtb3_1_1 = new Sensor(SensorType.GPS_POSITION);
		srtb3_1_1.setParent(rtb3_1);
		rtb3_1.addSubcomponent(srtb3_1_1);
			
			
 
			
			
			
			MotionSource srtb3_1_2 = new MotionSource();
			rtb3_1.addSubcomponent(srtb3_1_2);
			
 
			
			Battery srtb3_1_3 = new Battery(220000); 
			srtb3_1_3.setParent(rtb3_1);
			rtb3_1.addSubcomponent(srtb3_1_3);
			
			
			
			
		mission.addRobot(rtb3_1);
	
	
	EnvironmentalObject eo1 = new EnvironmentalObject(1, new Point(3.01,-4.41,0.0), false);
	mission.addObject(eo1);
	EnvironmentalObject eo2 = new EnvironmentalObject(2, new Point(2.13,-4.45,0.0), false);
	mission.addObject(eo2);
	EnvironmentalObject eo3 = new EnvironmentalObject(3, new Point(1.21,-4.38,0.0), false);
	mission.addObject(eo3);
	EnvironmentalObject eo4 = new EnvironmentalObject(4, new Point(0.32,-4.33,0.0), false);
	mission.addObject(eo4);
	EnvironmentalObject eo5 = new EnvironmentalObject(5, new Point(-0.7,-4.4,0.0), false);
	mission.addObject(eo5);
	EnvironmentalObject eo6 = new EnvironmentalObject(6, new Point(-1.65,-4.34,0.0), false);
	mission.addObject(eo6);
	
	EnvironmentalObject eo7 = new EnvironmentalObject(13, new Point(-3.5,-2.27,0.0), false);
	mission.addObject(eo7);
	EnvironmentalObject eo8 = new EnvironmentalObject(14, new Point(-2.59,-2.29,0.0), false);
	mission.addObject(eo8);
	EnvironmentalObject eo9 = new EnvironmentalObject(15, new Point(-1.68,-2.31,0.0), false);
	mission.addObject(eo9);
	EnvironmentalObject eo10 = new EnvironmentalObject(16, new Point(-0.64,-2.34,0.0), false);
	mission.addObject(eo10);
	EnvironmentalObject eo11 = new EnvironmentalObject(17, new Point(0.18,-2.4,0.0), false);
	mission.addObject(eo11);
	EnvironmentalObject eo12 = new EnvironmentalObject(18, new Point(1.11,-2.34,0.0), false);
	mission.addObject(eo12);
	
	
 
 
		
		Robot [] grp1 = {rtb3_0,rtb3_1}; 
		GoalParticipants gptrackDistances = new StaticParticipants(grp1, mission);
		
		
		
		GoalTemporalConstraints gt1 = new GoalTemporalConstraints(0.0, 1190.0);
		
		
		
		GoalAction ga1 = new TrackDistances();
		
		
		
		
		
		
		GoalRegion grtrackDistances = new StaticGoalRegion(
			new Region(new Point(-150.0, -260.0, -40.0),
			           new Point(245.0, 20.0, 100.0)));
		
		
		Goal trackDistances = new Goal("trackDistances", mission, gt1, gptrackDistances, Optional.of(grtrackDistances), ga1);
		
		
		mission.addGoal("trackDistances", trackDistances);
 
 
		
		Robot [] grp2 = {rtb3_0,rtb3_1}; 
		GoalParticipants gpcheckRoomsCompleted = new StaticParticipants(grp2, mission);
		
		
		
		GoalTemporalConstraints gt2 = new GoalTemporalConstraints(0.0, 1500.0);
		
		
		
		
		
		
		GoalAction ga2 = new CheckRoomsCompletedGaussianEnergy(14000.0, 3000.0, 4364337254L);
		
		
		
		GoalRegion grcheckRoomsCompleted = new StaticGoalRegion(
			new Region(new Point(0.0, 0.0, 0.0),
			           new Point(1000.0, 1000.0, 0.0)));
		
		
		Goal checkRoomsCompleted = new Goal("checkRoomsCompleted", mission, gt2, gpcheckRoomsCompleted, Optional.of(grcheckRoomsCompleted), ga2);
		
		
		mission.addGoal("checkRoomsCompleted", checkRoomsCompleted);
 
 
		
		Robot [] grp3 = {rtb3_0,rtb3_1}; 
		GoalParticipants gptrackEnergyHealthcare = new StaticParticipants(grp3, mission);
		
		
		
		GoalTemporalConstraints gt3 = new GoalTemporalConstraints(0.0, 1500.0);
		
		
		
		
		GoalAction ga3 = new TrackEnergyConsumption(40.0, 11.0);
		
		
		
		
		
		GoalRegion grtrackEnergyHealthcare = new StaticGoalRegion(
			new Region(new Point(0.0, 0.0, 0.0),
			           new Point(10.0, 10.0, 2.0)));
		
		
		Goal trackEnergyHealthcare = new Goal("trackEnergyHealthcare", mission, gt3, gptrackEnergyHealthcare, Optional.of(grtrackEnergyHealthcare), ga3);
		
		
		mission.addGoal("trackEnergyHealthcare", trackEnergyHealthcare);
	

	
	
	
	return mission;
	}
}