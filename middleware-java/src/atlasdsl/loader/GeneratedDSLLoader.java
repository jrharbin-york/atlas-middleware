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
	
		Robot rgilda = new Robot("gilda");
		rgilda.setPointComponentProperty("startLocation", new Point(200.0,-85.0,0.0));
		rgilda.setDoubleComponentProperty("maxSpeed", 5.0);
		rgilda.setDoubleComponentProperty("maxDepth", 20.0);
		rgilda.setDoubleComponentProperty("startSpeed", 1.6);
		
 
		Sensor srgilda_1 = new Sensor(SensorType.SONAR);
		srgilda_1.setParent(rgilda);
		srgilda_1.setDoubleComponentProperty("swathWidth", 10.0);
		srgilda_1.setDoubleComponentProperty("detectionProb", 0.99);
		rgilda.addSubcomponent(srgilda_1);
			
			
 
			
			MotionSource srgilda_2 = new MotionSource();
			rgilda.addSubcomponent(srgilda_2);
			
 
		Sensor srgilda_3 = new Sensor(SensorType.GPS_POSITION);
		srgilda_3.setParent(rgilda);
		rgilda.addSubcomponent(srgilda_3);
			
			
			
		mission.addRobot(rgilda);
		Robot rhenry = new Robot("henry");
		rhenry.setPointComponentProperty("startLocation", new Point(-85.0,-150.0,0.0));
		rhenry.setDoubleComponentProperty("maxSpeed", 5.0);
		rhenry.setDoubleComponentProperty("startSpeed", 1.5);
		rhenry.setDoubleComponentProperty("maxDepth", 20.0);
		
 
		Sensor srhenry_1 = new Sensor(SensorType.SONAR);
		srhenry_1.setParent(rhenry);
		srhenry_1.setDoubleComponentProperty("swathWidth", 20.0);
		srhenry_1.setDoubleComponentProperty("detectionProb", 0.99);
		rhenry.addSubcomponent(srhenry_1);
			
			
 
		Sensor srhenry_2 = new Sensor(SensorType.GPS_POSITION);
		srhenry_2.setParent(rhenry);
		rhenry.addSubcomponent(srhenry_2);
			
			
 
			
			MotionSource srhenry_3 = new MotionSource();
			rhenry.addSubcomponent(srhenry_3);
			
			
		mission.addRobot(rhenry);
	
	
	
	ArrayList<Point> eopoints0 = new ArrayList<Point>();
		eopoints0.add(new Point(107.0, -101.0, 0.0));
		eopoints0.add(new Point(112.0, -106.0, 0.0));
		eopoints0.add(new Point(112.0, -113.0, 0.0));
		eopoints0.add(new Point(107.0, -118.0, 0.0));
		eopoints0.add(new Point(100.0, -118.0, 0.0));
		eopoints0.add(new Point(95.0, -113.0, 0.0));
		eopoints0.add(new Point(95.0, -106.0, 0.0));
		eopoints0.add(new Point(100.0, -101.0, 0.0));
	EnvironmentalObstacle eob0 = new EnvironmentalObstacle("ob_0", eopoints0);
	mission.addObstacle(eob0);
	ArrayList<Point> eopoints1 = new ArrayList<Point>();
		eopoints1.add(new Point(50.0, -30.0, 0.0));
		eopoints1.add(new Point(53.0, -33.0, 0.0));
		eopoints1.add(new Point(53.0, -38.0, 0.0));
		eopoints1.add(new Point(50.0, -42.0, 0.0));
		eopoints1.add(new Point(45.0, -42.0, 0.0));
		eopoints1.add(new Point(41.0, -38.0, 0.0));
		eopoints1.add(new Point(41.0, -33.0, 0.0));
		eopoints1.add(new Point(45.0, -30.0, 0.0));
	EnvironmentalObstacle eob1 = new EnvironmentalObstacle("ob_1", eopoints1);
	mission.addObstacle(eob1);
	ArrayList<Point> eopoints2 = new ArrayList<Point>();
		eopoints2.add(new Point(82.0, -65.0, 0.0));
		eopoints2.add(new Point(87.0, -70.0, 0.0));
		eopoints2.add(new Point(87.0, -76.0, 0.0));
		eopoints2.add(new Point(82.0, -81.0, 0.0));
		eopoints2.add(new Point(76.0, -81.0, 0.0));
		eopoints2.add(new Point(71.0, -76.0, 0.0));
		eopoints2.add(new Point(71.0, -70.0, 0.0));
		eopoints2.add(new Point(76.0, -65.0, 0.0));
	EnvironmentalObstacle eob2 = new EnvironmentalObstacle("ob_2", eopoints2);
	mission.addObstacle(eob2);
	ArrayList<Point> eopoints3 = new ArrayList<Point>();
		eopoints3.add(new Point(59.0, -101.0, 0.0));
		eopoints3.add(new Point(64.0, -105.0, 0.0));
		eopoints3.add(new Point(64.0, -112.0, 0.0));
		eopoints3.add(new Point(59.0, -116.0, 0.0));
		eopoints3.add(new Point(53.0, -116.0, 0.0));
		eopoints3.add(new Point(48.0, -112.0, 0.0));
		eopoints3.add(new Point(48.0, -105.0, 0.0));
		eopoints3.add(new Point(53.0, -101.0, 0.0));
	EnvironmentalObstacle eob3 = new EnvironmentalObstacle("ob_3", eopoints3);
	mission.addObstacle(eob3);
	ArrayList<Point> eopoints4 = new ArrayList<Point>();
		eopoints4.add(new Point(107.0, -38.0, 0.0));
		eopoints4.add(new Point(112.0, -43.0, 0.0));
		eopoints4.add(new Point(112.0, -49.0, 0.0));
		eopoints4.add(new Point(107.0, -54.0, 0.0));
		eopoints4.add(new Point(100.0, -54.0, 0.0));
		eopoints4.add(new Point(96.0, -49.0, 0.0));
		eopoints4.add(new Point(96.0, -43.0, 0.0));
		eopoints4.add(new Point(100.0, -38.0, 0.0));
	EnvironmentalObstacle eob4 = new EnvironmentalObstacle("ob_4", eopoints4);
	mission.addObstacle(eob4);
	
 
 
		
		Robot [] grp1 = {rgilda,rhenry}; 
		GoalParticipants gpmutualAvoidance = new StaticParticipants(grp1, mission);
		
		
		
		GoalTemporalConstraints gt1 = new GoalTemporalConstraints(0.0, 1200.0);
		
		
		GoalAction ga1 = new AvoidOthers(4.0);
		
		
		
		GoalRegion grmutualAvoidance = new StaticGoalRegion(
			new Region(new Point(0.0, 0.0, 0.0),
			           new Point(1000.0, 1000.0, 0.0)));
		
		
		Goal mutualAvoidance = new Goal("mutualAvoidance", mission, gt1, gpmutualAvoidance, Optional.of(grmutualAvoidance), ga1);
		
		
		mission.addGoal("mutualAvoidance", mutualAvoidance);
 
 
		
		Robot [] grp2 = {rgilda,rhenry}; 
		GoalParticipants gptrackDistances = new StaticParticipants(grp2, mission);
		
		
		
		GoalTemporalConstraints gt2 = new GoalTemporalConstraints(0.0, 1201.0);
		
		
		
		GoalAction ga2 = new TrackDistances();
		
		
		GoalRegion grtrackDistances = new StaticGoalRegion(
			new Region(new Point(0.0, 0.0, 0.0),
			           new Point(0.0, 0.0, 0.0)));
		
		
		Goal trackDistances = new Goal("trackDistances", mission, gt2, gptrackDistances, Optional.of(grtrackDistances), ga2);
		
		
		mission.addGoal("trackDistances", trackDistances);
	

	
	
	 
	
	
	FaultImpact fi1;
	try {	
		fi1 = new MotionFault(srgilda_2, "speed", "5.0");
	} catch (InvalidComponentType e) {
		throw new DSLLoadFailed("MotionFault 1 is not using a MotionSource as its affected component");
	}
	
	
	
	FaultTimeProperties ft1 = new FaultTimeProperties(0.0, 1200.0, 1200.0, 3, 0.8); 
	
	Fault f1 = new Fault("SPEEDFAULT-GILDA", fi1, Optional.empty(), ft1);
	mission.addFault(f1);
	 
	
	
	FaultImpact fi2;
	try {	
		fi2 = new MotionFault(srgilda_2, "heading", "180.0");
	} catch (InvalidComponentType e) {
		throw new DSLLoadFailed("MotionFault 2 is not using a MotionSource as its affected component");
	}
	
	
	
	FaultTimeProperties ft2 = new FaultTimeProperties(0.0, 1200.0, 1200.0, 1, 0.8); 
	
	Fault f2 = new Fault("HEADINGFAULT-GILDA", fi2, Optional.empty(), ft2);
	mission.addFault(f2);
	 
	
	
	FaultImpact fi3;
	try {	
		fi3 = new MotionFault(srhenry_3, "speed", "5.0");
	} catch (InvalidComponentType e) {
		throw new DSLLoadFailed("MotionFault 3 is not using a MotionSource as its affected component");
	}
	
	
	
	FaultTimeProperties ft3 = new FaultTimeProperties(0.0, 1200.0, 1200.0, 3, 0.8); 
	
	Fault f3 = new Fault("SPEEDFAULT-HENRY", fi3, Optional.empty(), ft3);
	mission.addFault(f3);
	 
	
	
	FaultImpact fi4;
	try {	
		fi4 = new MotionFault(srhenry_3, "heading", "153.0");
	} catch (InvalidComponentType e) {
		throw new DSLLoadFailed("MotionFault 4 is not using a MotionSource as its affected component");
	}
	
	
	
	FaultTimeProperties ft4 = new FaultTimeProperties(0.0, 1200.0, 1200.0, 1, 0.8); 
	
	Fault f4 = new Fault("HEADINGFAULT-HENRY", fi4, Optional.empty(), ft4);
	mission.addFault(f4);
	
	return mission;
	}
}