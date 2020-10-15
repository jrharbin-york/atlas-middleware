package carsspecific.ros.rosmapping;

import atlasdsl.*;
import atlassharedclasses.Point;

public class RobotCommunity extends MOOSCommunity {
	// TODO: Add custom behaviours in constructor - must include a Waypoint for each
	// robot to be updated by the collective intelligence
	
	public RobotCommunity(MOOSSimulation sim, Robot robot, Point startPos, double startSpeed, double maxSpeed, double maxDepth) {
		super(sim,robot.getName());
		
		addProcess(new PShareProcess(this, dbPortOffset));
		addProcess(new USimMarineProcess(this, startPos));
		addProcess(new PLoggerProcess(this, "LOG_" + communityName, false));
		addProcess(new PNodeReporterProcess(this, robot));
		addProcess(new PMarinePIDProcess(this));
		addProcess(new PHelmIvpProcess(this, startPos, robot.getName(), startSpeed, maxSpeed, maxDepth));
		addProcess(new PBasicContactMgrProcess(this));	
		addProcess(new PHostinfoProcess(this));
		addProcess(new UFldNodeBrokerProcess(this));
	}
	
	public void addSonarSensors() {
		
	}
}
