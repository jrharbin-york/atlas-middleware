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
	final double MISSION_END_TIME = 200.0;
	final boolean STOP_ON_NO_ENERGY = false;
	Mission mission = new Mission(MISSION_END_TIME, STOP_ON_NO_ENERGY);
	
	
		Robot ruav_1 = new Robot("uav_1");
		ruav_1.setPointComponentProperty("startLocation", new Point(7.0,-2.0,0.0));
		ruav_1.setDoubleComponentProperty("maxSpeed", 1.5);
		
			
		mission.addRobot(ruav_1);
		Robot ruav_2 = new Robot("uav_2");
		ruav_2.setPointComponentProperty("startLocation", new Point(7.0,2.0,0.0));
		ruav_2.setDoubleComponentProperty("maxSpeed", 1.5);
		
			
		mission.addRobot(ruav_2);
	
	
	
	
 
 
		
		Robot [] grp1 = {ruav_1,ruav_2}; 
		GoalParticipants gptrackDistancesAndVelocities = new StaticParticipants(grp1, mission);
		
		
		
			GoalTemporalConstraints gt1 = new GoalTemporalConstraints(0.0, 2400.0);
			
			
		
		
		
		
		
		GoalAction ga1 = new TrackDistances();
		
		
		
		   
		GoalRegion grtrackDistancesAndVelocities = new StaticGoalRegion(
			new Region(new Point(-10.0, -10.0, 0.0),
			           new Point(10.0, 10.0, 10.0)));
		
		
		Goal trackDistancesAndVelocities = new Goal("trackDistancesAndVelocities", mission, gt1, gptrackDistancesAndVelocities, Optional.of(grtrackDistancesAndVelocities), ga1);
		
		
		
		
		
		Metric met1 = new SpeedViolationsCount();
		trackDistancesAndVelocities.addMetric(met1);
		
		
		
		Metric met2 = new FuzzingTimeLength();
		trackDistancesAndVelocities.addMetric(met2);
		
		mission.addGoal("trackDistancesAndVelocities", trackDistancesAndVelocities);
 
 
		
		Robot [] grp2 = {ruav_1,ruav_2}; 
		GoalParticipants gpStayInOuterRegion = new StaticParticipants(grp2, mission);
		
		
		
			GoalTemporalConstraints gt2 = new GoalTemporalConstraints(0.0, MISSION_END_TIME);
		
		
		
		
		
		
		GoalAction ga2 = new StayInRegion(false);
		
		
		   
		GoalRegion grStayInOuterRegion = new StaticGoalRegion(
			new Region(new Point(-100.0, -50.0, 0.0),
			           new Point(12.0, 50.0, 23.0)));
		
		
		Goal StayInOuterRegion = new Goal("StayInOuterRegion", mission, gt2, gpStayInOuterRegion, Optional.of(grStayInOuterRegion), ga2);
		
		
		
		
		
		Metric met3 = new OutsideOfOuterRegionViolations();
		StayInOuterRegion.addMetric(met3);
		
		mission.addGoal("StayInOuterRegion", StayInOuterRegion);
 
 
		
		Robot [] grp3 = {ruav_1,ruav_2}; 
		GoalParticipants gpAvoidPlaneInner = new StaticParticipants(grp3, mission);
		
		
		
			GoalTemporalConstraints gt3 = new GoalTemporalConstraints(0.0, MISSION_END_TIME);
		
		
		
		
		GoalAction ga3 = new Avoid(1.5);
		
		
		
		
		
		
		Goal AvoidPlaneInner = new Goal("AvoidPlaneInner", mission, gt3, gpAvoidPlaneInner, Optional.empty(), ga3);
		
			GoalVariable gvar1 = new GoalVariable("/pymesh_distance_to_plane", "standard_msgs/Float32", true);
			AvoidPlaneInner.addVariable(gvar1);
		
		
		
		
		Metric met4 = new OutsideOfInnerRegionViolations();
		AvoidPlaneInner.addMetric(met4);
		
		mission.addGoal("AvoidPlaneInner", AvoidPlaneInner);
 
 
		
		Robot [] grp4 = {ruav_1,ruav_2}; 
		GoalParticipants gpAvoidOthers = new StaticParticipants(grp4, mission);
		
		
		
			GoalTemporalConstraints gt4 = new GoalTemporalConstraints(0.0, MISSION_END_TIME);
		
		
		GoalAction ga4 = new AvoidOthers(2.0);
		
		
		
		
		
		
		   
		GoalRegion grAvoidOthers = new StaticGoalRegion(
			new Region(new Point(-10.0, -10.0, 0.0),
			           new Point(10.0, 10.0, 10.0)));
		
		
		Goal AvoidOthers = new Goal("AvoidOthers", mission, gt4, gpAvoidOthers, Optional.of(grAvoidOthers), ga4);
		
		
		
		
		
		Metric met5 = new AvoidanceViolationsCount();
		AvoidOthers.addMetric(met5);
		
		mission.addGoal("AvoidOthers", AvoidOthers);
 
 
		
		Robot [] grp5 = {ruav_1,ruav_2}; 
		GoalParticipants gpAvoidInnerRegion = new StaticParticipants(grp5, mission);
		
		
		
			GoalTemporalConstraints gt5 = new GoalTemporalConstraints(0.0, MISSION_END_TIME);
		
		
		
		GoalAction ga5 = new MaintainDistanceFrom(1.0);
		
		
		
		
		
		
		
		Goal AvoidInnerRegion = new Goal("AvoidInnerRegion", mission, gt5, gpAvoidInnerRegion, Optional.empty(), ga5);
		
			GoalVariable gvar2 = new GoalVariable("/airframe_clearance", "std_msgs/Float64", true);
			AvoidInnerRegion.addVariable(gvar2);
		
		
		mission.addGoal("AvoidInnerRegion", AvoidInnerRegion);
	

	
	
	
	return mission;
	}
}