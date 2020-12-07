package exptrunner.metrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import atlasdsl.Mission;
import exptrunner.jmetal.FaultInstanceSetSolution;
import exptrunner.jmetal.InvalidMetrics;
import exptrunner.jmetal.Metrics;

public class MetricsProcessing {
	private Mission mission;
	private List<Metrics> metrics;
	private FileWriter tempLog;
	
	private int metricID;
	private int constraintCount;
	
	public MetricsProcessing(Mission mission, List<Metrics> metrics, FileWriter tempLog) {
		this.metrics = metrics;
		this.metricID = 0;
		this.constraintCount = 0;
		this.mission = mission;
		this.tempLog = tempLog;
	}
	
	private static final int DETECTIONS_PER_OBJECT_EXPECTED = 2;
	
	public List<Metrics> getMetrics() {
		return metrics;
	}
	
	public int readObstacleFileObsCount(File obstacleFile) {
		Scanner reader;
		int count = 0;
		try {
			reader = new Scanner(obstacleFile);
			// Find the result from the line num
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String[] fields = line.split(",");
				String robotName = fields[0];
				Integer collisionCount = Integer.valueOf(fields[1]);
				count += collisionCount;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;
	}
	
	public double detectionCompletionTime(Map<Integer, List<Double>> detectionInfo, int objectCount) {
		double missionCompletionTime = 0;
		// Find latest of the first 2 for all these
		for (int i = 0; i < objectCount; i++) {
			List<Double> res = detectionInfo.get(i);
			if (res != null) {
				Collections.sort(res);

				if (res.size() < 2) {
					// If less than 1 detection/verification per object, the mission
					// was never completed

					// TODO: mission end time (defined) instead of MAX_VALUE here?

					return Double.MAX_VALUE;
				} else {
					missionCompletionTime = Math.max(missionCompletionTime, res.get(0));
					missionCompletionTime = Math.max(missionCompletionTime, res.get(1));
				}
			} else {
				// If one object was never detected, the mission was not completed
				return Double.MAX_VALUE;
			}
		}

		return missionCompletionTime;
	}
	
	public void registerDetectionAtTime(Map<Integer, List<Double>> detectionInfo, double time, int label) {
		if (!detectionInfo.containsKey(label)) {
			detectionInfo.put(label, new ArrayList<Double>());
		}
		detectionInfo.get(label).add(time);
	}

	public int missedDetections(Map<Integer, List<Double>> detectionInfo, int objectCount) {
		int missedTotal = 0;
		int foundTotal = 0;
		for (int i = 0; i < objectCount; i++) {
			List<Double> res = detectionInfo.get(i);
			if (res != null) {
				foundTotal += detectionInfo.get(i).size();
				foundTotal = Math.min(foundTotal, DETECTIONS_PER_OBJECT_EXPECTED);
				missedTotal += DETECTIONS_PER_OBJECT_EXPECTED - foundTotal;
			} else {
				// If no result for this object, add the number of detections intended
				missedTotal += DETECTIONS_PER_OBJECT_EXPECTED;
			}
		}
		return missedTotal;
	}
	
	public void readLogFiles(String logFileDir, FaultInstanceSetSolution solution) throws InvalidMetrics {
		// Read the goal result file here - process the given goals
		// Write it out to a common result file - with the fault info
		File f = new File(logFileDir + "/goalLog.log");
		// TODO: fix path
		File pf = new File("/home/atlas/atlas/atlas-middleware/expt-working/logs/objectPositions.log");
		File robotDistFile = new File("/home/atlas/atlas/atlas-middleware/expt-working/logs/robotDistances.log");
		File obstacleFile = new File("/home/atlas/atlas/atlas-middleware/expt-working/logs/obstacleCollisions.log");
		int detections = 0;
		int missedDetections = 0;
		int avoidanceViolations = 0;
		int maxObjectNum = 0;
		int checkDetectionCount = 0;

		double firstFaultTime = Double.MAX_VALUE;

		// The map entry stores as a pair the number of detections and the latest time
		Map<Integer, List<Double>> detectionInfo = new HashMap<Integer, List<Double>>();

		Scanner reader;
		try {
			reader = new Scanner(f);
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String[] fields = line.split(",");
				String goalClass = fields[0];
				if (goalClass.equals("atlasdsl.DiscoverObjects")) {
					double time = Double.parseDouble(fields[1]);
					String robot = fields[2];
					int num = Integer.parseInt(fields[3]);
					checkDetectionCount++;

					if (num > maxObjectNum) {
						maxObjectNum = num;
					}

					registerDetectionAtTime(detectionInfo, time, num);
				}

				if (goalClass.equals("atlasdsl.AvoidOthers")) {
					avoidanceViolations += 1;
					double time = Double.parseDouble(fields[2]);
					if (time < firstFaultTime) {
						firstFaultTime = time;
					}
				}
			}

			missedDetections = Math.max(0, ((mission.getEnvironmentalObjects().size() * DETECTIONS_PER_OBJECT_EXPECTED)
					- checkDetectionCount));
			reader.close();

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// TODO: better way, get max_dist this from a region?
		double MAX_DIST = 1000.0;

		double missedDetectionFactor = 10.0;
		double avoidanceFactor = 10.0;
		int objectCount = mission.getEnvironmentalObjects().size();

		double combinedDistMetric = missedDetections;
		double avoidanceMetric = avoidanceViolations;
		double timeProp = solution.faultCostProportion();
		double timeTotal = solution.faultTimeTotal();

		try {
			reader = new Scanner(pf);
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String[] fields = line.split(",");
				String label = fields[0];
				double dist = Double.valueOf(fields[1]);
				double sensorWorkingDist = Double.valueOf(fields[2]);
				System.out.println("Robot dist to label " + label + "=" + dist + "\n");
				combinedDistMetric += dist;
			}

			System.out.println(
					"Total distance: " + combinedDistMetric + ",missedDetections = " + missedDetections + "\n");
			combinedDistMetric += (missedDetections * missedDetectionFactor);
			System.out.println("Output metric: " + combinedDistMetric);

			reader.close();

			reader = new Scanner(robotDistFile);
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String[] fields = line.split(",");
				String label = fields[0];
				double dist = Double.valueOf(fields[1]);
				avoidanceMetric += dist;
			}

			avoidanceMetric += (avoidanceViolations * avoidanceFactor);
			reader.close();

			double detectionCompletionTime = detectionCompletionTime(detectionInfo, objectCount);
			int numFaults = solution.numberOfFaults();

			// Set the output metrics
			int metricID = 0;
			int constraintID = 0;
			List<String> names = new ArrayList<String>();

			if (metrics.contains(Metrics.PURE_MISSED_DETECTIONS)) {
				solution.setObjective(metricID++, -missedDetections);
				names.add("missedDetections");
			}
			
			if (metrics.contains(Metrics.COMBINED_MISSED_DETECTION_DIST_METRIC)) {
				solution.setObjective(metricID++, -combinedDistMetric);
				names.add("combinedMissedDetectionDistMetric");
			}

			if (metrics.contains(Metrics.OBSTACLE_AVOIDANCE_METRIC)) {
				if (mission.getAllRobots().size() == 2) {
					// Check we're using the right case study!
					int obstacleCollisionCount = readObstacleFileObsCount(obstacleFile);
					if (obstacleCollisionCount == 0) {
						solution.setConstraint(constraintID++, -100);
					} else {
						solution.setConstraint(constraintID++, 0);
					}

					solution.setObjective(metricID++, -obstacleCollisionCount);
					names.add("obstacleCollisions");
				} else {
					throw new InvalidMetrics(Metrics.OBSTACLE_AVOIDANCE_METRIC, "Cannot be used on this case study");
				}
			}

			if (metrics.contains(Metrics.AVOIDANCE_METRIC)) {
				solution.setObjective(metricID++, -avoidanceMetric);
				names.add("avoidanceMetric");
			}

			if (metrics.contains(Metrics.TIME_PROP)) {
				solution.setObjective(metricID++, timeProp);
				names.add("timeProp");
			}

			if (metrics.contains(Metrics.TIME_TOTAL_ABSOLUTE)) {
				solution.setObjective(metricID++, timeTotal);
				names.add("timeTotal");
			}

			if (metrics.contains(Metrics.NUM_FAULTS)) {
				solution.setObjective(metricID++, numFaults);
				names.add("numFaults");
			}

			if (metrics.contains(Metrics.FIRST_FAULT_TIME)) {
				solution.setObjective(metricID++, firstFaultTime);
				names.add("firstFaultTime");
			}

			if (metrics.contains(Metrics.DETECTION_COMPLETION_TIME)) {
				solution.setObjective(metricID++, detectionCompletionTime);
				names.add("detectionCompletionTime");
			}

			constraintCount = constraintID;

			String info = String.join(",", names);
			String logRes = Arrays.stream(solution.getObjectives()).mapToObj(Double::toString)
					.collect(Collectors.joining(","));

			System.out.println(solution.hashCode() + ":" + solution + "\n");
			System.out.println(info + "\n");
			System.out.println(logRes + "\n");
			tempLog.write(logRes + "\n");
			tempLog.flush();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Metrics getMetricByID(int i) {
		return metrics.get(i);
	}
}
