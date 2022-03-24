package atlasdsl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;

import atlassharedclasses.Point;
import middleware.core.ATLASCore;

public class TrackDistances extends GoalAction {
	private ATLASCore core;
	private Mission mission;
	private double completionTime;
	private GeometryFactory jtsGeoFactory = new GeometryFactory();

	private final double SPEED_VIOLATION_TIME_THRESHOLD = 1.0;
	private HashMap<String, Double> robotLastSpeedViolationTime = new HashMap<String, Double>();

	public class SpeedViolationRecord {
		private String robotName;
		private double speed;
		private double speedLimit;
		private double time;

		public SpeedViolationRecord(String robotName, double speed, double speedLimit, double time) {
			this.robotName = robotName;
			this.speed = speed;
			this.speedLimit = speedLimit;
			this.time = time;
		}

		public String getRobotName() {
			return robotName;
		}

		public double getTime() {
			return time;
		}

		public double getSpeed() {
			return speed;
		}

		public double getSpeedLimit() {
			return speedLimit;
		}
	}

	public class CollisionRecord {
		private long lastCollisionTime;
		private long timeStepMillis = 100;
		private int count = 0;

		public CollisionRecord() {
			this.lastCollisionTime = System.currentTimeMillis();
		}

		public void registerPotentialCollision() {
			long time = System.currentTimeMillis();
			if ((time - lastCollisionTime) > timeStepMillis) {
				count++;
				lastCollisionTime = time;
			}
		}

		public int getCount() {
			return count;
		}
	}

	// TODO: track the robot distances from each other too!
	protected Map<EnvironmentalObject, Double> objectDistances = new HashMap<EnvironmentalObject, Double>();
	protected Map<EnvironmentalObject, Double> sensorWorkingDistances = new HashMap<EnvironmentalObject, Double>();

	protected Map<String, Double> robotEnergy = new HashMap<String, Double>();
	protected Map<Robot, Double> finalDists = new HashMap<Robot, Double>();

	protected List<SpeedViolationRecord> speedViolations = new ArrayList<SpeedViolationRecord>();

	protected Map<String, Geometry> obstacleGeometry = new HashMap<String, Geometry>();
	protected Map<String, HashMap<String, CollisionRecord>> collisions = new HashMap<String, HashMap<String, CollisionRecord>>();

	protected Map<String, Double> interRobotDistances = new HashMap<String, Double>();
	protected Map<String, Point> positions = new HashMap<String, Point>();

	private boolean writtenYet = false;

	protected FileWriter positionTrace;

	// private WKTReader jtsReader = new WKTReader();
	// protected Map<EnvironmentalObstacle,Boolean> obstacleInside = new
	// HashMap<EnvironmentalObstacle,Double>();

	private double maxSpeedThreshold;

	private void setupObstacleGeometry() {

		for (Entry<String, EnvironmentalObstacle> eo_e : mission.getObstacles().entrySet()) {
			String label = eo_e.getKey();
			EnvironmentalObstacle eo = eo_e.getValue();
			List<Point> eoPoints = eo.getPoints();
			Coordinate[] coords = new Coordinate[eoPoints.size() + 1];
			int i = 0;
			for (Point p : eoPoints) {
				coords[i] = new Coordinate(p.getX(), p.getY(), 0.0);
				i++;
			}
			coords[eoPoints.size()] = new Coordinate(coords[0].x, coords[0].y);

			System.out.println(coords);

			Geometry p = jtsGeoFactory.createPolygon(coords);
			System.out.println("Created geometry: " + p);
			obstacleGeometry.put(label, p);
		}
	}

	private void registerCollision(String obstacleName, String robotName, double time) {
		if (!collisions.containsKey(robotName)) {
			collisions.put(robotName, new HashMap<String, CollisionRecord>());
		}

		HashMap<String, CollisionRecord> hm = collisions.get(robotName);
		if (!hm.containsKey(obstacleName)) {
			hm.put(obstacleName, new CollisionRecord());
		}

		hm.get(obstacleName).registerPotentialCollision();
	}

	@SuppressWarnings("deprecation")
	private void checkPointIntersection(String robotName, Point p) throws ParseException {
		// TODO: check the bounding box first to reduce computation?
		// System.out.println("checkPointIntersection - point = " + p);
		Coordinate c = new Coordinate(p.getX(), p.getY());
		Geometry jtsP = jtsGeoFactory.createPoint(c);

		for (Entry<String, Geometry> eo_e : obstacleGeometry.entrySet()) {

			Geometry g = eo_e.getValue();
			String geometryName = eo_e.getKey();
			if (g.contains(jtsP)) {
				Double time = core.getTime();
				registerCollision(geometryName, robotName, time);
				System.out.println("checkPointIntersection - found collision with " + geometryName + " at point " + p);
			}
		}
	}

	public TrackDistances() {
		// TODO: setting maxSpeedThreshold to a default
		this.maxSpeedThreshold = 2.0;
	}

	private void writeResultsOut() {
		writtenYet = true;
		try {
			FileWriter output = new FileWriter("logs/robotDistancesAtEnd.log");
			for (Map.Entry<Robot, Double> eo_d : finalDists.entrySet()) {
				Robot r = eo_d.getKey();
				double dist = eo_d.getValue();
				output.write(r.getName() + "," + dist + "\n");
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			FileWriter output = new FileWriter("logs/robotEnergyAtEnd.log");
			for (Map.Entry<String, Double> eo_d : robotEnergy.entrySet()) {
				String robotName = eo_d.getKey();
				double energy = eo_d.getValue();
				output.write(robotName + "," + energy + "\n");
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			FileWriter output = new FileWriter("logs/objectPositions.log");
			for (Map.Entry<EnvironmentalObject, Double> eo_d : objectDistances.entrySet()) {
				EnvironmentalObject eo = eo_d.getKey();
				double dist = eo_d.getValue();

				Double sensorWorkingDist = sensorWorkingDistances.get(eo_d.getKey());
				if (sensorWorkingDist == null) {
					sensorWorkingDist = Double.MAX_VALUE;
				}

				output.write(eo.getLabel() + "," + dist + "," + sensorWorkingDist + "\n");
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			FileWriter output = new FileWriter("logs/robotDistances.log");
			for (Map.Entry<String, Double> eo_d : interRobotDistances.entrySet()) {
				String name = eo_d.getKey();
				double minDist = eo_d.getValue();
				output.write(name + "," + minDist + "\n");
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			FileWriter output = new FileWriter("logs/speedViolations.log");
			for (SpeedViolationRecord svr : speedViolations) {
				output.write(svr.getRobotName() + "," + svr.getSpeed() + "," + svr.getSpeedLimit() + "," + svr.getTime()
						+ "\n");
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			FileWriter output = new FileWriter("logs/obstacleCollisions.log");
			for (Map.Entry<String, HashMap<String, CollisionRecord>> eo_d : collisions.entrySet()) {
				String robotName = eo_d.getKey();
				int countForRobots = 0;
				HashMap<String, CollisionRecord> hm = eo_d.getValue();
				for (Map.Entry<String, CollisionRecord> entry_d : hm.entrySet()) {
					countForRobots += entry_d.getValue().getCount();
				}
				output.write(robotName + "," + countForRobots + "\n");
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getFinalDistFromStart() {
		for (Robot r : mission.getAllRobots()) {
			try {
				Point currentLocation = r.getPointComponentProperty("location");
				double finalDist = r.getPointComponentProperty("startLocation").distanceTo(currentLocation);
				finalDists.put(r, finalDist);
			} catch (MissingProperty m) {
				System.out.println(
						"WARNING: TrackDistances couldn't report final distance due to missing properties for robot "
								+ r.getName());
			}
		}
	}

	protected Optional<GoalResult> test(Mission mission, GoalParticipants participants) {
		double time = core.getTime();

		for (Robot r : mission.getAllRobots()) {
			robotEnergy.put(r.getName(), r.getEnergyRemaining());
		}

		// If completion time is exceeded, write the results file
		if ((time > completionTime) && !writtenYet) {
			getFinalDistFromStart();
			writeResultsOut();
			return Optional.empty();
		}
		return Optional.empty();
	}

	protected void checkDistanceToObjects(String name, double x, double y, double speed) {
		boolean sensorWorking = speed < maxSpeedThreshold;

		for (EnvironmentalObject eo : objectDistances.keySet()) {
			double dist = eo.distanceTo(x, y);

			if (dist < objectDistances.get(eo)) {
				objectDistances.put(eo, dist);
			}

			if (sensorWorking && (dist < sensorWorkingDistances.get(eo))) {
				sensorWorkingDistances.put(eo, dist);
			}
		}
	}

	protected void checkDistanceToOthers(String name, double x, double y) {
		for (Map.Entry<String, Point> p_d : positions.entrySet()) {
			String otherName = p_d.getKey();
			Point otherPos = p_d.getValue();

			if (!otherName.equals(name)) {
				Double bestDist = interRobotDistances.get(name);
				if (bestDist == null) {
					bestDist = Double.MAX_VALUE;
				}

				Double thisDist = otherPos.distanceTo(x, y);
				if (thisDist < bestDist) {
					interRobotDistances.put(name, thisDist);
				}
			}
		}
	}

	public boolean speedViolationReadyToLog(String robotName, double currentTime) {
		if (!robotLastSpeedViolationTime.containsKey(robotName)) {
			robotLastSpeedViolationTime.put(robotName, Double.MIN_VALUE);
		}

		double lastTime = robotLastSpeedViolationTime.get(robotName);
		if ((currentTime - lastTime) >= SPEED_VIOLATION_TIME_THRESHOLD) {
			robotLastSpeedViolationTime.put(robotName, currentTime);
			return true;
		} else {
			return false;
		}
	}

	protected void checkSpeed(String robotName, double currentSpeed) {
		Robot r = mission.getRobot(robotName);
		try {
			double maxSpeed = r.getDoubleComponentProperty("maxSpeed");
			double time = core.getTime();
			if (currentSpeed > maxSpeed && speedViolationReadyToLog(robotName, time)) {
				System.out.println("**** SPEED VIOLATION");
				SpeedViolationRecord svr = new SpeedViolationRecord(robotName, currentSpeed, maxSpeed, time);
				speedViolations.add(svr);
			}

		} catch (MissingProperty e) {
			System.out.println("Property maxSpeed missign for robot " + robotName + " in TrackDistances.java");
		}

	}

	protected void setup(ATLASCore core, Mission mission, Goal g) throws GoalActionSetupFailure {
		this.core = core;
		this.completionTime = core.getTimeLimit();
		this.mission = mission;

		try {
			String randomRunID = UUID.randomUUID().toString();
			positionTrace = new FileWriter("logs/positionTrace-" + randomRunID + ".log");

			core.setupPositionWatcher((gps) -> {
				if (g.isReady(core.getTime())) {
					double x = gps.getX();
					double y = gps.getY();
					double z = gps.getZ();
					// double speed = gps.getSpeed();
					String rname = gps.getRobotName();
					Point p = new Point(x, y, z);
					positions.put(rname, p);

					try {
						recordPositionTrace(rname, p);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					// TODO: check distance to objects has been removed from
					// the implementation as it requires a simultaneous speed
					// checkDistanceToObjects(rname, x, y, speed);

					try {
						checkPointIntersection(rname, p);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			});

			core.setupSpeedWatcher((speedReading) -> {
				if (g.isReady(core.getTime())) {
					String rname = speedReading.getRobotName();
					double speed = speedReading.getSpeed();
					System.out.println("++++ speed at " + core.getTime() + " is " + speed);
					checkSpeed(rname, speed);
				}
			});

		} catch (IOException e2) {
			e2.printStackTrace();
		}

		setupObstacleGeometry();

		for (EnvironmentalObject eo : mission.getEnvironmentalObjects()) {
			objectDistances.put(eo, Double.MAX_VALUE);
			sensorWorkingDistances.put(eo, Double.MAX_VALUE);
		}
	}

	private void recordPositionTrace(String rname, Point p) throws IOException {
		double time = core.getTime();
		positionTrace.write(rname + "," + time + "," + p.getX() + "," + p.getY() + "," + p.getZ() + "\n");

	}
}