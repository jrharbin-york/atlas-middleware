package atlasdsl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import middleware.core.ATLASCore;

public class TrackDistances extends GoalAction {
	private ATLASCore core;
	private Mission mission;
	private double completionTime;

	protected Map<EnvironmentalObject, Double> objectDistances = new HashMap<EnvironmentalObject, Double>();
	private boolean writtenYet = false;
	//protected Map<EnvironmentalObstacle,Boolean> obstacleInside = new HashMap<EnvironmentalObstacle,Double>();

	public TrackDistances() {	

	}

	private void writeResultsOut() {
		writtenYet = true;
		try {
			FileWriter output = new FileWriter("logs/positions.log");
			for (Map.Entry<EnvironmentalObject, Double> eo_d : objectDistances.entrySet()) {
				EnvironmentalObject eo = eo_d.getKey();
				double dist = eo_d.getValue();
				output.write(eo.getLabel() + "," + dist + "\n");
			}
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected Optional<GoalResult> test(Mission mission, GoalParticipants participants) {
		// If completion time is exceeded, write the results file
		double time = core.getTime();
		if (time > completionTime && !writtenYet) {
			writeResultsOut();
			return Optional.empty();
		}
		return Optional.empty();
	}

	protected void checkDistanceToObjects(double x, double y) {
		// Check the distance to the nearest robot from all objects - update it
		System.out.println("checkDistanceToObjects");
		for (EnvironmentalObject eo : objectDistances.keySet()) {
			double dist = eo.distanceTo(x, y);
			if (dist < objectDistances.get(eo)) {
				objectDistances.put(eo, dist);
			}
		}
	}

	protected void setup(ATLASCore core, Mission mission, Goal g) throws GoalActionSetupFailure {
		this.core = core;
		this.completionTime = core.getTimeLimit();
		this.mission = mission;
		
		for (EnvironmentalObject eo : mission.getEnvironmentalObjects()) {
			objectDistances.put(eo, Double.MAX_VALUE);
		}
		
		core.setupPositionWatcher((gps) -> {
			double x = gps.getX();
			double y = gps.getY();
			checkDistanceToObjects(x, y);
		});
	}
}
