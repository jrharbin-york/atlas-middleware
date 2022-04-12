package atlasdsl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import atlassharedclasses.Point;
import middleware.core.ATLASCore;

// This goal needs to log the final energy remaining

public class TrackEnergyConsumption extends GoalAction {
	
	private static final boolean ROBOT_ENERGY_DEBUGGING = false;
	
	private ATLASCore core;
	private Mission mission;
	private double previousTime = 0.0;
	
	private double energyPerDistance;
	private double energyPerTime;

	
	private boolean writtenYet = false;

	private double completionTime;
	
	public TrackEnergyConsumption(double energyPerDistance, double energyPerTime) {
		this.energyPerDistance = energyPerDistance;
		this.energyPerTime = energyPerTime;
		// energyPerRoom is now in the CheckRoomsCompleted Goal
	}

	protected Optional<GoalResult> test(Mission mission, GoalParticipants participants) {
		double time = core.getTime();
		double timeGap = time - previousTime;
		previousTime = time;
		double energyDrop = timeGap * energyPerTime;
		for (Robot r : mission.getAllRobots()) {
			core.depleteEnergyOnRobot(r.getName(), energyDrop);
		}
		
		// If completion time is exceeded, write the results file
		if ((time > completionTime) && !writtenYet) {
			writeResultsOut();
		}
		
		return Optional.empty();
	}
	
	private void writeResultsOut() {
		writtenYet = true;
		try {
			FileWriter output = new FileWriter("logs/robotEnergyAtEnd.log");
			for (Robot r : mission.getAllRobots()) {
				output.write(r.getName() + "," + r.getEnergyRemaining() + "," + r.getEnergyProportionRemaining() + "\n");
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void linearEnergyModel(Robot r, Point currentLocation, Point newLocation) {
		double distanceTravelled = currentLocation.distanceTo(newLocation);
		double energyConsumed = energyPerDistance * distanceTravelled;
		if (ROBOT_ENERGY_DEBUGGING) {
			double newEnergy = r.getEnergyRemaining();
			double energyPercent = r.getEnergyProportionRemaining();
			System.out.println("Robot " + r.getName() + " distanceTravelled = " + distanceTravelled +",energyConsumed = " + energyConsumed + ",newEnergy = " + newEnergy + "[" + (energyPercent * 100) + "%]");
		}
		core.depleteEnergyOnRobot(r.getName(), energyConsumed);
	}

	protected void setup(ATLASCore core, Mission mission, Goal g) throws GoalActionSetupFailure {
		this.core = core;
		this.previousTime = core.getTime();
		this.completionTime = core.getTimeLimit();
		this.mission = mission;

		core.setupPositionWatcher((gps) -> {
			if (g.isReady(core.getTime())) {
				double x = gps.getX();
				double y = gps.getY();
				double z = gps.getZ();

				String rname = gps.getRobotName();
				Point newLocation = new Point(x, y, z);
				Robot r = mission.getRobot(rname);
				try {
					Point currentLocation = r.getPointComponentProperty("location");
					linearEnergyModel(r,currentLocation, newLocation);
				} catch (MissingProperty e) {
					e.printStackTrace();
				}
		}});
		
		// Now done in CheckRoomsCompleted
		
//		core.setupSimVarWatcher("/roomCompleted", (svar, robotName, val) -> {
//			if (g.isReady(core.getTime())) {
//				core.depleteEnergyOnRobot(robotName, energyPerRoom);
//			}
//		});
	}
}