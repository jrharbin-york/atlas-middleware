package atlasdsl.loader;

import atlasdsl.*;
import atlasdsl.faults.*;
import atlassharedclasses.*;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import fuzzexperiment.runner.metrics.*;


public class GeneratedDSLLoader implements DSLLoader {
	public Mission loadMission() throws DSLLoadFailed {
	final double MISSION_END_TIME = 150.0;
	final boolean STOP_ON_NO_ENERGY = false;
	Mission mission = new Mission(MISSION_END_TIME, STOP_ON_NO_ENERGY);
	
	Computer c1 = new Computer("shoreside");
	mission.addComputer(c1);
	
		Robot ruav_1 = new Robot("uav_1");
		ruav_1.setPointComponentProperty("startLocation", new Point(8.0,-8.0,0.0));
		
			
		mission.addRobot(ruav_1);
		Robot ruav_2 = new Robot("uav_2");
		ruav_2.setPointComponentProperty("startLocation", new Point(8.0,8.0,0.0));
		
			
		mission.addRobot(ruav_2);
	
	
	
	
 
 
		
		Robot [] grp1 = {ruav_1,ruav_2}; 
		GoalParticipants gptrackDistances = new StaticParticipants(grp1, mission);
		
		
		
			GoalTemporalConstraints gt1 = new GoalTemporalConstraints(0.0, 2400.0);
			
			
		
		
		
		GoalAction ga1 = new TrackDistances();
		
		
		
		   
		GoalRegion grtrackDistances = new StaticGoalRegion(
			new Region(new Point(100.0, 100.0, 0.0),
			           new Point(0.0, 0.0, 0.0)));
		
		
		Goal trackDistances = new Goal("trackDistances", mission, gt1, gptrackDistances, Optional.of(grtrackDistances), ga1);
		
			GoalVariable gvar1 = new GoalVariable("/ual/position_goalvar_test", "geometry_msgs/PoseStamped", true);
			trackDistances.addVariable(gvar1);
		
		
		mission.addGoal("trackDistances", trackDistances);
 
 
		
		Robot [] grp2 = {ruav_1,ruav_2}; 
		GoalParticipants gpStayInRegion = new StaticParticipants(grp2, mission);
		
		
		
			GoalTemporalConstraints gt2 = new GoalTemporalConstraints(0.0, MISSION_END_TIME);
		
		
		
		
		GoalAction ga2 = new StayInRegion(false);
		
		
		   
		GoalRegion grStayInRegion = new StaticGoalRegion(
			new Region(new Point(-10.0, -10.0, 0.0),
			           new Point(20.0, 20.0, 10.0)));
		
		
		Goal StayInRegion = new Goal("StayInRegion", mission, gt2, gpStayInRegion, Optional.of(grStayInRegion), ga2);
		
		
		
		
		
		Metric met1 = new OutsideOfOuterRegionViolations();
		StayInRegion.addMetric(met1);
		
		mission.addGoal("StayInRegion", StayInRegion);
	

	
	
	
	return mission;
	}
}