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
		rella.setDoubleComponentProperty("startSpeed", 1.6);
		rella.setDoubleComponentProperty("maxDepth", 20.0);
		
 
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
		Robot rgilda = new Robot("gilda");
		rgilda.setPointComponentProperty("startLocation", new Point(190.0,-150.0,0.0));
		rgilda.setDoubleComponentProperty("maxSpeed", 5.0);
		rgilda.setDoubleComponentProperty("startSpeed", 1.5);
		rgilda.setDoubleComponentProperty("maxDepth", 20.0);
		
 
		Sensor srgilda_1 = new Sensor(SensorType.SONAR);
		srgilda_1.setParent(rgilda);
		srgilda_1.setDoubleComponentProperty("swathWidth", 30.0);
		srgilda_1.setDoubleComponentProperty("detectionProb", 0.99);
		rgilda.addSubcomponent(srgilda_1);
			
			
 
		Sensor srgilda_2 = new Sensor(SensorType.GPS_POSITION);
		srgilda_2.setParent(rgilda);
		rgilda.addSubcomponent(srgilda_2);
			
			
 
			
			MotionSource srgilda_3 = new MotionSource();
			rgilda.addSubcomponent(srgilda_3);
			
			
		mission.addRobot(rgilda);
		Robot rhenry = new Robot("henry");
		rhenry.setPointComponentProperty("startLocation", new Point(-85.0,-45.0,0.0));
		rhenry.setDoubleComponentProperty("maxSpeed", 5.0);
		rhenry.setDoubleComponentProperty("startSpeed", 1.5);
		rhenry.setDoubleComponentProperty("maxDepth", 20.0);
		
 
		Sensor srhenry_1 = new Sensor(SensorType.SONAR);
		srhenry_1.setParent(rhenry);
		srhenry_1.setDoubleComponentProperty("swathWidth", 25.0);
		srhenry_1.setDoubleComponentProperty("detectionProb", 0.99);
		rhenry.addSubcomponent(srhenry_1);
			
			
 
		Sensor srhenry_2 = new Sensor(SensorType.GPS_POSITION);
		srhenry_2.setParent(rhenry);
		rhenry.addSubcomponent(srhenry_2);
			
			
 
			
			MotionSource srhenry_3 = new MotionSource();
			rhenry.addSubcomponent(srhenry_3);
			
			
		mission.addRobot(rhenry);
		Robot rbrian = new Robot("brian");
		rbrian.setPointComponentProperty("startLocation", new Point(150.0,55.0,0.0));
		rbrian.setDoubleComponentProperty("maxSpeed", 0.75);
		rbrian.setDoubleComponentProperty("startSpeed", 0.75);
		rbrian.setDoubleComponentProperty("maxDepth", 20.0);
		
 
		Sensor srbrian_1 = new Sensor(SensorType.CAMERA);
		srbrian_1.setParent(rbrian);
		srbrian_1.setDoubleComponentProperty("imagingRange", 3.0);
		srbrian_1.setDoubleComponentProperty("detectionProb", 0.99);
		rbrian.addSubcomponent(srbrian_1);
			
			
 
		Sensor srbrian_2 = new Sensor(SensorType.GPS_POSITION);
		srbrian_2.setParent(rbrian);
		rbrian.addSubcomponent(srbrian_2);
			
			
 
			
			MotionSource srbrian_3 = new MotionSource();
			rbrian.addSubcomponent(srbrian_3);
			
			
		mission.addRobot(rbrian);
		Robot rlinda = new Robot("linda");
		rlinda.setPointComponentProperty("startLocation", new Point(80.0,55.0,0.0));
		rlinda.setDoubleComponentProperty("maxSpeed", 0.75);
		rlinda.setDoubleComponentProperty("startSpeed", 0.75);
		rlinda.setDoubleComponentProperty("maxDepth", 20.0);
		
 
		Sensor srlinda_1 = new Sensor(SensorType.CAMERA);
		srlinda_1.setParent(rlinda);
		srlinda_1.setDoubleComponentProperty("imagingRange", 3.0);
		srlinda_1.setDoubleComponentProperty("detectionProb", 0.99);
		rlinda.addSubcomponent(srlinda_1);
			
			
 
		Sensor srlinda_2 = new Sensor(SensorType.GPS_POSITION);
		srlinda_2.setParent(rlinda);
		rlinda.addSubcomponent(srlinda_2);
			
			
 
			
			MotionSource srlinda_3 = new MotionSource();
			rlinda.addSubcomponent(srlinda_3);
			
			
		mission.addRobot(rlinda);
	
	
	EnvironmentalObject eo1 = new EnvironmentalObject(1, new Point(0.0,-55.0,0.0), false);
	mission.addObject(eo1);
	EnvironmentalObject eo2 = new EnvironmentalObject(2, new Point(185.0,-45.0,0.0), true);
	mission.addObject(eo2);
	EnvironmentalObject eo3 = new EnvironmentalObject(3, new Point(100.0,-150.0,0.0), false);
	mission.addObject(eo3);
	
 
 
 
 
		
		Robot [] grp1 = {rella,rfrank,rgilda,rhenry}; 
		GoalParticipants gpmutualAvoidance = new StaticParticipants(grp1, mission);
		
		
		
		GoalTemporalConstraints gt1 = new GoalTemporalConstraints(0.0, 1200.0);
		
		
		GoalAction ga1 = new AvoidOthers(4.0);
		
		
		
		GoalRegion grmutualAvoidance = new StaticGoalRegion(
			new Region(new Point(0.0, 0.0, 0.0),
			           new Point(1000.0, 1000.0, 0.0)));
		
		
		Goal mutualAvoidance = new Goal("mutualAvoidance", mission, gt1, gpmutualAvoidance, Optional.of(grmutualAvoidance), ga1);
		
		
		mission.addGoal("mutualAvoidance", mutualAvoidance);
 
 
 
 
		
		Robot [] grp2 = {rella,rfrank,rgilda,rhenry}; 
		GoalParticipants gpprimarySensorSweep = new StaticParticipants(grp2, mission);
		
		
		
		GoalTemporalConstraints gt2 = new GoalTemporalConstraints(0.0, 1200.0);
		
		GoalAction ga2 = new SensorCover(10.0, 1, SensorType.SONAR);
		
		
		
		
		GoalRegion grprimarySensorSweep = new StaticGoalRegion(
			new Region(new Point(0.0, 0.0, 0.0),
			           new Point(1000.0, 1000.0, 0.0)));
		
		
		Goal primarySensorSweep = new Goal("primarySensorSweep", mission, gt2, gpprimarySensorSweep, Optional.of(grprimarySensorSweep), ga2);
		
		
		mission.addGoal("primarySensorSweep", primarySensorSweep);
		
	
		GoalParticipants gpverifySensorDetections = new RelativeParticipants(primarySensorSweep, (StaticParticipants)gpprimarySensorSweep, "UUV_NAME", RelativeParticipants.LogicOps.SUBTRACT, 1);
		
		
		GoalTemporalConstraints gt3 = new GoalTemporalConstraints(0.0, 1200.0);
		
		GoalAction ga3 = new SensorCover(5.0, 1, SensorType.SONAR);
		
		
		
		
		
		GoalRegion grverifySensorDetections = new DynamicGoalRegion(primarySensorSweep, "UUV_DETECTION_COORD", 30.0);
		
		Goal verifySensorDetections = new Goal("verifySensorDetections", mission, gt3, gpverifySensorDetections, Optional.of(grverifySensorDetections), ga3);
		
		try {
			verifySensorDetections.setDependencyOn(primarySensorSweep);
		} catch (SelfDependencyError e) {
			throw new DSLLoadFailed("Goal verifySensorDetections depends on itself");
		}
		
		mission.addGoal("verifySensorDetections", verifySensorDetections);
 
 
 
 
		
		Robot [] grp4 = {rella,rfrank,rgilda,rhenry}; 
		GoalParticipants gpfindTestObjects = new StaticParticipants(grp4, mission);
		
		
		
		GoalTemporalConstraints gt4 = new GoalTemporalConstraints(0.0, 1200.0);
		
		
		
		
		List<EnvironmentalObject> ga4Objs = new ArrayList<EnvironmentalObject>();
		ga4Objs.add(eo1);
		ga4Objs.add(eo3);
		ga4Objs.add(eo2);
		GoalAction ga4 = new DiscoverObjects(ga4Objs, 2);
		
		
		GoalRegion grfindTestObjects = new StaticGoalRegion(
			new Region(new Point(0.0, 0.0, 0.0),
			           new Point(0.0, 0.0, 0.0)));
		
		
		Goal findTestObjects = new Goal("findTestObjects", mission, gt4, gpfindTestObjects, Optional.of(grfindTestObjects), ga4);
		
		
		mission.addGoal("findTestObjects", findTestObjects);
 
 
 
 
 
 
		
		Robot [] grp5 = {rbrian,rella,rfrank,rgilda,rhenry,rlinda}; 
		GoalParticipants gptrackDistances = new StaticParticipants(grp5, mission);
		
		
		
		GoalTemporalConstraints gt5 = new GoalTemporalConstraints(0.0, 1201.0);
		
		
		
		GoalAction ga5 = new TrackDistances();
		
		
		GoalRegion grtrackDistances = new StaticGoalRegion(
			new Region(new Point(0.0, 0.0, 0.0),
			           new Point(0.0, 0.0, 0.0)));
		
		
		Goal trackDistances = new Goal("trackDistances", mission, gt5, gptrackDistances, Optional.of(grtrackDistances), ga5);
		
		
		mission.addGoal("trackDistances", trackDistances);
	

	
 
	Message msgDETECTION_ELLA = new Message("DETECTION_ELLA", rella, c1);
	mission.addMessage(msgDETECTION_ELLA); 
 
	Message msgDETECTION_FRANK = new Message("DETECTION_FRANK", rfrank, c1);
	mission.addMessage(msgDETECTION_FRANK); 
 
	Message msgDETECTION_GILDA = new Message("DETECTION_GILDA", rgilda, c1);
	mission.addMessage(msgDETECTION_GILDA); 
 
	Message msgDETECTION_HENRY = new Message("DETECTION_HENRY", rhenry, c1);
	mission.addMessage(msgDETECTION_HENRY); 
 
	Message msgUUV_COORDINATE_UPDATE_INIITAL_ELLA = new Message("UUV_COORDINATE_UPDATE_INIITAL_ELLA", c1, rella);
	mission.addMessage(msgUUV_COORDINATE_UPDATE_INIITAL_ELLA); 
	
	 
	
	
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
		fi4 = new MotionFault(srgilda_3, "speed", "5.0");
	} catch (InvalidComponentType e) {
		throw new DSLLoadFailed("MotionFault 4 is not using a MotionSource as its affected component");
	}
	
	
	
	FaultTimeProperties ft4 = new FaultTimeProperties(0.0, 1200.0, 1200.0, 3, 0.8); 
	
	Fault f4 = new Fault("SPEEDFAULT-GILDA", fi4, Optional.empty(), ft4);
	mission.addFault(f4);
	 
	
	
	FaultImpact fi5;
	try {	
		fi5 = new MotionFault(srhenry_3, "speed", "5.0");
	} catch (InvalidComponentType e) {
		throw new DSLLoadFailed("MotionFault 5 is not using a MotionSource as its affected component");
	}
	
	
	
	FaultTimeProperties ft5 = new FaultTimeProperties(0.0, 1200.0, 1200.0, 3, 0.8); 
	
	Fault f5 = new Fault("SPEEDFAULT-HENRY", fi5, Optional.empty(), ft5);
	mission.addFault(f5);
	 
	
	
	FaultImpact fi6;
	try {	
		fi6 = new MotionFault(srfrank_3, "heading", "153.0");
	} catch (InvalidComponentType e) {
		throw new DSLLoadFailed("MotionFault 6 is not using a MotionSource as its affected component");
	}
	
	
	
	FaultTimeProperties ft6 = new FaultTimeProperties(0.0, 1200.0, 1200.0, 1, 0.8); 
	
	Fault f6 = new Fault("HEADINGFAULT-FRANK", fi6, Optional.empty(), ft6);
	mission.addFault(f6);
	 
	
	
	FaultImpact fi7;
	try {	
		fi7 = new MotionFault(srgilda_3, "heading", "350.0");
	} catch (InvalidComponentType e) {
		throw new DSLLoadFailed("MotionFault 7 is not using a MotionSource as its affected component");
	}
	
	
	
	FaultTimeProperties ft7 = new FaultTimeProperties(0.0, 1200.0, 1200.0, 1, 0.8); 
	
	Fault f7 = new Fault("HEADINGFAULT-GILDA", fi7, Optional.empty(), ft7);
	mission.addFault(f7);
	 
	
	
	FaultImpact fi8;
	try {	
		fi8 = new MotionFault(srhenry_3, "heading", "153.0");
	} catch (InvalidComponentType e) {
		throw new DSLLoadFailed("MotionFault 8 is not using a MotionSource as its affected component");
	}
	
	
	
	FaultTimeProperties ft8 = new FaultTimeProperties(0.0, 1200.0, 1200.0, 1, 0.8); 
	
	Fault f8 = new Fault("HEADINGFAULT-HENRY", fi8, Optional.empty(), ft8);
	mission.addFault(f8);
	 
	
	SubFieldSpec sf9 = new SubFieldSpec(0, 1, false);
	PointMessageChange ps9 = PointMessageChange.forAbsolute(new Point(10.0, 10.0)); 
	FaultImpact fi9 = new MutateMessage(msgUUV_COORDINATE_UPDATE_INIITAL_ELLA, Optional.of(sf9), ps9);
	
	
	
	
	FaultTimeProperties ft9 = new FaultTimeProperties(0.0, 1200.0, 300.0, 1, 0.8); 
	
	Fault f9 = new Fault("COORDINATE-ELLA", fi9, Optional.empty(), ft9);
	mission.addFault(f9);
	
	return mission;
	}
}