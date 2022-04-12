package exptrunner.metrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import exptrunner.jmetal.InvalidMetrics;

public class MetricsProcessing {
	private static final boolean DEBUG_CHECK_REALISTIC_COMPLETION_TIME = true;
	private List<Metrics> metrics;
	private FileWriter tempLog;

	public enum MetricStateKeys {
		MISSION_END_TIME, 
		BENIGN_OBJECTS_IN_MISSION, 
		MALICIOUS_OBJECTS_IN_MISSION, 
		VERIFICATIONS_PER_BENIGN_OBJECT,
		VERIFICATIONS_PER_MALICIOUS_OBJECT,
	}

	private HashMap<MetricStateKeys, Object> metricState = new HashMap<MetricStateKeys, Object>();

	public MetricsProcessing(List<Metrics> metrics, FileWriter tempLog) {
		this.metrics = metrics;
		this.tempLog = tempLog;
	}

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

	public void registerDetectionAtTime(Map<Integer, List<Double>> detectionInfo, double time, int label) {
		if (!detectionInfo.containsKey(label)) {
			detectionInfo.put(label, new ArrayList<Double>());
		}
		detectionInfo.get(label).add(time);
	}

	public Map<Metrics, Object> processMissedDetections(Map<Integer, Integer> remainingDetectionsTracker,
			double finalDetectionTime, Map<Metrics, Object> metricResults) {
		// Set PURE_MISSED_DETECTIONS if needed

		int totalMissedDetections = 0;
		for (Map.Entry<Integer, Integer> entry : remainingDetectionsTracker.entrySet()) {
			totalMissedDetections += entry.getValue();
		}

		if (metrics.contains(Metrics.PURE_MISSED_DETECTIONS)) {
			metricResults.put(Metrics.PURE_MISSED_DETECTIONS, totalMissedDetections);
		}

		if (metrics.contains(Metrics.DETECTION_COMPLETION_TIME)) {
			double endTime;
			if (totalMissedDetections == 0) {
				endTime = finalDetectionTime;
				metricResults.put(Metrics.DETECTION_COMPLETION_TIME, finalDetectionTime);
			} else {
				endTime = (double) metricState.get(MetricStateKeys.MISSION_END_TIME);
			}
			metricResults.put(Metrics.DETECTION_COMPLETION_TIME, endTime);
		}

		return metricResults;
	}

	public Map<Metrics, Object> readMetricsFromLogFiles(String logFileDir) throws InvalidMetrics {

		// Read the goal result file here - process the given goals
		// Write it out to a common result file - with the fault info
		File f = new File(logFileDir + "/goalLog.log");
		File pf = new File(logFileDir + "/objectPositions.log");
		File robotDistFile = new File(logFileDir + "/robotDistances.log");
		File obstacleFile = new File(logFileDir + "/obstacleCollisions.log");
		File energyFile = new File(logFileDir + "/robotEnergyAtEnd.log");
		File distFile = new File(logFileDir + "/robotDistancesAtEnd.log");

		int detections = 0;
		int avoidanceViolations = 0;
		int checkDetectionCount = 0;
		double finalDetectionTime = 0.0;

		int outsideRegionViolations = 0;

		double firstFaultTime = Double.MAX_VALUE;

		Map<Metrics, Object> metricResults = new HashMap<Metrics, Object>();
		Map<Integer, Integer> remainingDetectionsTracker = new HashMap<Integer, Integer>();

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
					String nature = fields[1];

					if (nature.equals("LOOKINGFOR")) {
						int objectID = Integer.parseInt(fields[2]);
						int remainingDetections = Integer.parseInt(fields[3]);
						remainingDetectionsTracker.put(objectID, remainingDetections);
					}

					if (nature.equals("FOUND")) {
						double time = Double.parseDouble(fields[2]);
						String robot = fields[3];
						int objectID = Integer.parseInt(fields[4]);
						int remainingDetections = Integer.parseInt(fields[5]);
						checkDetectionCount++;
						registerDetectionAtTime(detectionInfo, time, objectID);

						if (time > finalDetectionTime) {
							finalDetectionTime = time;
						}

						remainingDetectionsTracker.put(objectID, remainingDetections);
					}
				}

				if (goalClass.equals("atlasdsl.AvoidOthers")) {
					avoidanceViolations += 1;
					double time = Double.parseDouble(fields[2]);
					if (time < firstFaultTime) {
						firstFaultTime = time;
					}
				}

				if (goalClass.equals("atlasdsl.StayInRegion")) {
					int count = Integer.parseInt(fields[1]);
					outsideRegionViolations = Math.max(count, outsideRegionViolations);
				}
			}

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// TODO: better way, get max_dist this from a region?
		double MAX_DIST = 1000.0;

		double missedDetectionFactor = 10.0;
		double avoidanceFactor = 10.0;

		double avoidanceMetric = avoidanceViolations;

		try {
			reader = new Scanner(pf);
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String[] fields = line.split(",");
				String label = fields[0];
				double dist = Double.valueOf(fields[1]);
				double sensorWorkingDist = Double.valueOf(fields[2]);
				System.out.println("Robot dist to label " + label + "=" + dist + "\n");
			}

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

			List<String> names = new ArrayList<String>();

			if (metrics.contains(Metrics.WORST_CASE_WAYPOINT_COMPLETION_TIME_FROM_CI)) {
				Scanner swReader;
				swReader = new Scanner(new File("/tmp/completeTimings.log"));
				double worstCase = 0.0;

				while (swReader.hasNextLine()) {
					String line = swReader.nextLine();
					System.out.println("line = " + line);
					String[] fields = line.split(",");
					String name = fields[0];
					double time = Double.parseDouble(fields[1]);
					
					if (DEBUG_CHECK_REALISTIC_COMPLETION_TIME && (time < 100.0)) {
						System.out.println("Stopping run due to unrealistic individual robot completion time - robot " + name + " has " + time);
						System.exit(1);
					}
					
					System.out.println("Robot " + name + " finished at time " + time);
					if (time > worstCase) {
						worstCase = time;
					}
					
					
					
				}

				if (metrics.contains(Metrics.WORST_CASE_WAYPOINT_COMPLETION_TIME_FROM_CI)) {
					metricResults.put(Metrics.WORST_CASE_WAYPOINT_COMPLETION_TIME_FROM_CI, worstCase);
				}
				
				if (DEBUG_CHECK_REALISTIC_COMPLETION_TIME && worstCase < 100.0) {
					System.out.println("Stopping due to unrealistic worst case completion time");
					System.exit(2);
				}
				
				swReader.close();
			}
			
			if (metrics.contains(Metrics.TOTAL_WAYPOINT_SWITCH_COUNT)) {
				Scanner swReader;
				swReader = new Scanner(new File("/tmp/waypointCount.log"));
				double count = 0;

				while (swReader.hasNextLine()) {
					String line = swReader.nextLine();
					System.out.println("line = " + line);
					String[] fields = line.split(",");
					String name = fields[0];
					int waypointCount = Integer.parseInt(fields[1]);
					System.out.println("Robot " + name + " has waypoint count " + waypointCount);
					count += waypointCount;
				}

				if (metrics.contains(Metrics.TOTAL_WAYPOINT_SWITCH_COUNT)) {
					metricResults.put(Metrics.TOTAL_WAYPOINT_SWITCH_COUNT, count);
				}
				swReader.close();
			}

			if (metrics.contains(Metrics.TOTAL_FINAL_DISTANCE_AT_END)
					|| metrics.contains(Metrics.MEAN_FINAL_DISTANCE_AT_END)) {
				Scanner distReader;
				try {
					distReader = new Scanner(distFile);
					double distTotal = 0.0;
					double count = 0;

					while (distReader.hasNextLine()) {
						String line = distReader.nextLine();
						String[] fields = line.split(",");
						String name = fields[0];
						Double distForRobot = Double.parseDouble(fields[1]);
						System.out.println("Robot " + name + " has distance at end " + distForRobot);
						distTotal += distForRobot;
						count++;
					}

					if (metrics.contains(Metrics.TOTAL_FINAL_DISTANCE_AT_END)) {
						metricResults.put(Metrics.TOTAL_FINAL_DISTANCE_AT_END, distTotal);
					}

					if (metrics.contains(Metrics.MEAN_FINAL_DISTANCE_AT_END)) {
						double meanEnergy = distTotal / count;
						metricResults.put(Metrics.MEAN_FINAL_DISTANCE_AT_END, meanEnergy);
					}
					distReader.close();

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

			if (metrics.contains(Metrics.TOTAL_ENERGY_AT_END) || metrics.contains(Metrics.MEAN_ENERGY_AT_END)) {
				Scanner energyReader;
				try {
					energyReader = new Scanner(energyFile);
					double energyTotal = 0.0;
					double count = 0;

					while (energyReader.hasNextLine()) {
						String line = energyReader.nextLine();
						String[] fields = line.split(",");
						String name = fields[0];
						Double energyOnRobot = Double.parseDouble(fields[1]);
						System.out.println("Robot " + name + " has energy " + energyOnRobot);
						energyTotal += energyOnRobot;
						count++;
					}

					if (metrics.contains(Metrics.TOTAL_ENERGY_AT_END)) {
						metricResults.put(Metrics.TOTAL_ENERGY_AT_END, energyTotal);
					}

					if (metrics.contains(Metrics.MEAN_ENERGY_AT_END)) {
						double meanEnergy = energyTotal / count;
						metricResults.put(Metrics.MEAN_ENERGY_AT_END, meanEnergy);
					}
					energyReader.close();

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} finally {

				}
			}

			metricResults = processMissedDetections(remainingDetectionsTracker, finalDetectionTime, metricResults);

			if (metrics.contains(Metrics.OUTSIDE_REGION_COUNT)) {
				metricResults.put(Metrics.OUTSIDE_REGION_COUNT, outsideRegionViolations);
			}

			if (metrics.contains(Metrics.OBSTACLE_AVOIDANCE_METRIC)) {
				int obstacleCollisionCount = readObstacleFileObsCount(obstacleFile);
				metricResults.put(Metrics.OBSTACLE_AVOIDANCE_METRIC, obstacleCollisionCount);
			}

			if (metrics.contains(Metrics.AVOIDANCE_METRIC)) {
				metricResults.put(Metrics.AVOIDANCE_METRIC, avoidanceMetric);
			}

			String info = String.join(",", names);

			String logRes = metricResults.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
					.collect(Collectors.joining(","));

			System.out.println(info + "\n");
			System.out.println(logRes + "\n");
			tempLog.write(logRes + "\n");
			tempLog.flush();

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return metricResults;
	}

	public Metrics getMetricByID(int i) {
		return metrics.get(i);
	}
	
	public List<Metrics> getAllMetrics() {
		return metrics;
	}

	public void setMetricState(MetricStateKeys key, Object o) {
		metricState.put(key, o);
	}

	public void clearMetricState() {
		metricState.clear();
	}
}
