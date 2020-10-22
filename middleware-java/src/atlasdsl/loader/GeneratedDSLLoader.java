package atlasdsl.loader;

import atlasdsl.*;
import atlasdsl.faults.*;
import atlassharedclasses.*;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

public class GeneratedDSLLoader implements DSLLoader {
	public Mission loadMission() throws DSLLoadFailed {
	
	Mission mission = new Mission();
	
	Computer c1 = new Computer("shoreside");
	mission.addComputer(c1);
	
		Robot rella = new Robot("ella");
		rella.setPointComponentProperty("startLocation", new Point(200.0,-85.0,0.0));
		rella.setDoubleComponentProperty("maxSpeed", 5.0);
		rella.setDoubleComponentProperty("maxDepth", 20.0);
		rella.setDoubleComponentProperty("startSpeed", 1.6);
		
 
		Sensor srella_1 = new Sensor(SensorType.SONAR);
		srella_1.setParent(rella);
		srella_1.setDoubleComponentProperty("swathWidth", 10.0);
		srella_1.setDoubleComponentProperty("detectionProb", 0.99);
		rella.addSubcomponent(srella_1);
			
			
 
			
			MotionSource srella_2 = new MotionSource();
			rella.addSubcomponent(srella_2);
			
 
		Sensor srella_3 = new Sensor(SensorType.GPS_POSITION);
		srella_3.setParent(rella);
		rella.addSubcomponent(srella_3);
			
			
			
		mission.addRobot(rella);
		Robot rfrank = new Robot("frank");
		rfrank.setPointComponentProperty("startLocation", new Point(-85.0,-150.0,0.0));
		rfrank.setDoubleComponentProperty("maxSpeed", 5.0);
		rfrank.setDoubleComponentProperty("startSpeed", 1.5);
		rfrank.setDoubleComponentProperty("maxDepth", 20.0);
		
 
		Sensor srfrank_1 = new Sensor(SensorType.SONAR);
		srfrank_1.setParent(rfrank);
		srfrank_1.setDoubleComponentProperty("swathWidth", 20.0);
		srfrank_1.setDoubleComponentProperty("detectionProb", 0.99);
		rfrank.addSubcomponent(srfrank_1);
			
			
 
		Sensor srfrank_2 = new Sensor(SensorType.GPS_POSITION);
		srfrank_2.setParent(rfrank);
		rfrank.addSubcomponent(srfrank_2);
			
			
 
			
			MotionSource srfrank_3 = new MotionSource();
			rfrank.addSubcomponent(srfrank_3);
			
			
		mission.addRobot(rfrank);
	
	
	
	ArrayList<Point> eopoints0 = new ArrayList<Point>();
		eopoints0.add(new Point(5.0, 10.0, 0.0));
		eopoints0.add(new Point(7.0, -2.0, 0.0));
		eopoints0.add(new Point(32.0, 1.0, 0.0));
		eopoints0.add(new Point(-6.0, -41.0, 0.0));
	EnvironmentalObstacle eob0 = new EnvironmentalObstacle("bigrock", eopoints0);
	mission.addObstacle(eob0);
	
 
 
		
		Robot [] grp1 = {rella,rfrank}; 
		GoalParticipants gpmutualAvoidance = new StaticParticipants(grp1, mission);
		
		
		
		GoalTemporalConstraints gt1 = new GoalTemporalConstraints(0.0, 1200.0);
		
		
		GoalAction ga1 = new AvoidOthers(4.0);
		
		
		
		GoalRegion grmutualAvoidance = new StaticGoalRegion(
			new Region(new Point(0.0, 0.0, 0.0),
			           new Point(1000.0, 1000.0, 0.0)));
		
		
		Goal mutualAvoidance = new Goal("mutualAvoidance", mission, gt1, gpmutualAvoidance, Optional.of(grmutualAvoidance), ga1);
		
		
		mission.addGoal("mutualAvoidance", mutualAvoidance);
 
 
		
		Robot [] grp2 = {rella,rfrank}; 
		GoalParticipants gpprimarySensorSweep = new StaticParticipants(grp2, mission);
		
		
		
		GoalTemporalConstraints gt2 = new GoalTemporalConstraints(0.0, 1200.0);
		
		GoalAction ga2 = new SensorCover(10.0, 1, SensorType.SONAR);
		
		
		
		
		GoalRegion grprimarySensorSweep = new StaticGoalRegion(
			new Region(new Point(0.0, 0.0, 0.0),
			           new Point(1000.0, 1000.0, 0.0)));
		
		
		Goal primarySensorSweep = new Goal("primarySensorSweep", mission, gt2, gpprimarySensorSweep, Optional.of(grprimarySensorSweep), ga2);
		
		
		mission.addGoal("primarySensorSweep", primarySensorSweep);
 
 
		
		Robot [] grp3 = {rella,rfrank}; 
		GoalParticipants gptrackDistances = new StaticParticipants(grp3, mission);
		
		
		
		GoalTemporalConstraints gt3 = new GoalTemporalConstraints(0.0, 1201.0);
		
		
		
		GoalAction ga3 = new TrackDistances();
		
		
		GoalRegion grtrackDistances = new StaticGoalRegion(
			new Region(new Point(0.0, 0.0, 0.0),
			           new Point(0.0, 0.0, 0.0)));
		
		
		Goal trackDistances = new Goal("trackDistances", mission, gt3, gptrackDistances, Optional.of(grtrackDistances), ga3);
		
		
		mission.addGoal("trackDistances", trackDistances);
	

	
	
	 
	
	
	FaultImpact fi1;
	try {	
		fi1 = new MotionFault(srella_2, "speed", "5.0");
	} catch (InvalidComponentType e) {
		throw new DSLLoadFailed("MotionFault 1 is not using a MotionSource as its affected component");
	}
	
	
	
	FaultTimeProperties ft1 = new FaultTimeProperties(0.0, 1200.0, 1200.0, 3, 0.8); 
	
	Fault f1 = new Fault("SPEEDFAULT-ELLA", fi1, Optional.empty(), ft1);
	mission.addFault(f1);
	 
	
	
	FaultImpact fi2;
	try {	
		fi2 = new MotionFault(srella_2, "heading", "180.0");
	} catch (InvalidComponentType e) {
		throw new DSLLoadFailed("MotionFault 2 is not using a MotionSource as its affected component");
	}
	
	
	
	FaultTimeProperties ft2 = new FaultTimeProperties(0.0, 1200.0, 1200.0, 1, 0.8); 
	
	Fault f2 = new Fault("HEADINGFAULT-ELLA", fi2, Optional.empty(), ft2);
	mission.addFault(f2);
	 
	
	
	FaultImpact fi3;
	try {	
		fi3 = new MotionFault(srfrank_3, "speed", "5.0");
	} catch (InvalidComponentType e) {
		throw new DSLLoadFailed("MotionFault 3 is not using a MotionSource as its affected component");
	}
	
	
	
	FaultTimeProperties ft3 = new FaultTimeProperties(0.0, 1200.0, 1200.0, 3, 0.8); 
	
	Fault f3 = new Fault("SPEEDFAULT-FRANK", fi3, Optional.empty(), ft3);
	mission.addFault(f3);
	 
	
	
	FaultImpact fi4;
	try {	
		fi4 = new MotionFault(srfrank_3, "heading", "153.0");
	} catch (InvalidComponentType e) {
		throw new DSLLoadFailed("MotionFault 4 is not using a MotionSource as its affected component");
	}
	
	
	
	FaultTimeProperties ft4 = new FaultTimeProperties(0.0, 1200.0, 1200.0, 1, 0.8); 
	
	Fault f4 = new Fault("HEADINGFAULT-FRANK", fi4, Optional.empty(), ft4);
	mission.addFault(f4);
	
	return mission;
	}
}