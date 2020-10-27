package atlasdsl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import atlassharedclasses.Point;
import middleware.core.ATLASCore;

public class TrackDistances extends GoalAction {
	private ATLASCore core;
	private Mission mission;
	private double completionTime;
	private PrecisionModel jtsPrecisionModel = new PrecisionModel(); 
	private GeometryFactory jtsGeoFactory = new GeometryFactory();

	// TODO: track the robot distances from each other too!
	protected Map<EnvironmentalObject, Double> objectDistances = new HashMap<EnvironmentalObject, Double>();
	protected Map<EnvironmentalObject, Double> sensorWorkingDistances = new HashMap<EnvironmentalObject, Double>();
	
	protected Map<String, Geometry> obstacleGeometry = new HashMap<String, Geometry>();
	protected Map<String, Double> collisions  = new HashMap<String,Double>();
	
	protected Map<String, Double> interRobotDistances = new HashMap<String, Double>();
	protected Map<String, Point> positions = new HashMap<String, Point>();
	
	private boolean writtenYet = false;
	
	//private WKTReader jtsReader = new WKTReader();
	//protected Map<EnvironmentalObstacle,Boolean> obstacleInside = new HashMap<EnvironmentalObstacle,Double>();

	private double maxSpeedThreshold;
	
	private void setupObstacleGeometry() {
		
		for (Entry<String, EnvironmentalObstacle> eo_e : mission.getObstacles().entrySet()) {
			String label = eo_e.getKey();
			EnvironmentalObstacle eo = eo_e.getValue();
			List<Point> eoPoints = eo.getPoints();
			Coordinate [] coords = new Coordinate[eoPoints.size()+1];
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
	
	@SuppressWarnings("deprecation")
	private void checkPointIntersection(Point p) throws ParseException {
		// TODO: check the bounding box first to reduce computation?
		Coordinate c = new Coordinate(p.getX(),p.getY());
		Geometry jtsP = jtsGeoFactory.createPoint(c);
		
		for (Entry<String, Geometry> eo_e : obstacleGeometry.entrySet()) {
			
			Geometry g = eo_e.getValue();
			String name = eo_e.getKey();
//			System.out.println(g);
//			System.out.println(jtsP);
			if (!g.contains(jtsP)) {
			} else {
				Double time = core.getTime();
				// TODO: also put the label there?
				collisions.put(name, time);
				System.out.println("COLLISION");
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
						
				Double thisDist = otherPos.distanceTo(x,y);
				if (thisDist < bestDist) {
					interRobotDistances.put(name, thisDist);
				}
			}
		}
	}

	protected void setup(ATLASCore core, Mission mission, Goal g) throws GoalActionSetupFailure {
		this.core = core;
		this.completionTime = core.getTimeLimit();
		this.mission = mission;
		
		setupObstacleGeometry();
		
		for (EnvironmentalObject eo : mission.getEnvironmentalObjects()) {
			objectDistances.put(eo, Double.MAX_VALUE);
			sensorWorkingDistances.put(eo, Double.MAX_VALUE);
		}
		
		core.setupPositionWatcher((gps) -> {
			double x = gps.getX();
			double y = gps.getY();
			double speed = gps.getSpeed();
			String name = gps.getRobotName();
			positions.put(name, new Point(x,y));
			
			checkDistanceToObjects(name, x, y, speed);
			try {
				checkPointIntersection(new Point(x,y));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		});
	}
}
