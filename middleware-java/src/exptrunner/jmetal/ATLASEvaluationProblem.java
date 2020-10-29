package exptrunner.jmetal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

import atlasdsl.Mission;
import atlasdsl.faults.Fault;
import atlassharedclasses.FaultInstance;

public class ATLASEvaluationProblem implements Problem<FaultInstanceSetSolution> {

	private static final long serialVersionUID = 1L;
	private static final double INITIAL_MIN_SPEED_VALUE = 1.0;
	private static final double INITIAL_MAX_SPEED_VALUE = 5.0;
	private static final int DETECTIONS_PER_OBJECT_EXPECTED = 2;

	private String fakeLogFileName = "/home/atlas/atlas/atlas-middleware/middleware-java/tempres/customLog.res";

	private int runCount = 0;
	private Random rng;

	private int realExperiment;
	private int evaluationNumber;

	private Mission mission;
	private boolean actuallyRun;
	private double exptRunTime;
	private String logFileDir;

	// This gives the weights for these different goals
	private Map<GoalsToCount, Integer> goalsToCount = new HashMap<GoalsToCount, Integer>();
	private Object algorithm;

	private FileWriter tempLog;
	private int variableFixedSize;
	private List<Metrics> metrics;

	public ATLASEvaluationProblem(Random rng, Mission mission, boolean actuallyRun, double exptRunTime,
			String logFileDir, Map<GoalsToCount, Integer> goalsToCount, List<Metrics> metrics) throws IOException {
		this.rng = rng;
		this.mission = mission;
		this.exptRunTime = exptRunTime;
		this.logFileDir = logFileDir;
		this.actuallyRun = actuallyRun;
		this.goalsToCount = goalsToCount;
		this.variableFixedSize = mission.getFaultsAsList().size();
		this.metrics = metrics;
		System.out.println(metrics.toString());
		String fileName = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		tempLog = new FileWriter("tempLog-" + fileName + ".res");
	}

	public void setFakeExperimentNum(int n) {
		realExperiment = n;
		System.out.println("realExperiment = " + realExperiment);
	}

	public int goalWeight(GoalsToCount g) {
		return goalsToCount.getOrDefault(g, 0);
	}

	public int getNumberOfVariables() {
		// TODO: this is fixed
		return variableFixedSize;
	}

	public int getNumberOfObjectives() {
		return metrics.size();
	}

	public int getNumberOfConstraints() {
		return 0;
	}

	public String getName() {
		return "ATLASEvaluationProblem";
	}

	public void performATLASExperiment(FaultInstanceSetSolution solution) throws InvalidMetrics {
		String exptTag = "exptGA-" + (runCount++);
		try {
			RunExperiment.doExperiment(mission, exptTag, solution.getFaultInstances(), actuallyRun, exptRunTime);
			readLogFiles(logFileDir, solution);
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	public void fakeExperiment(FaultInstanceSetSolution solution) {
		int missedDetections = 0;
		int avoidanceViolations = 0;

		double k = 1.0;
		double timeProp = solution.faultCostProportion();

		solution.setObjective(2, timeProp);

		List<FaultInstance> res1 = solution.testAllFaultInstances((FaultInstance fi) -> {
			return (fi.getStartTime() > 300.0) && (fi.getEndTime() < 400.0) && (fi.getLength() < 100.0);
		});
		for (FaultInstance fi : res1) {
			// missedDetections += 1;
			missedDetections += Math.pow(fi.getLength(), k);
		}
		;

		List<FaultInstance> res2 = solution.testAllFaultInstances((FaultInstance fi) -> {
			return (fi.getStartTime() > 1000.0) && (fi.getEndTime() < 1100.0) && (fi.getLength() < 100.0);
		});
		for (FaultInstance fi : res2) {
			// missedDetections += 1;
			missedDetections += Math.pow(fi.getLength(), k);
		}
		;

		List<FaultInstance> res3 = solution.testAllFaultInstances((FaultInstance fi) -> {
			return (fi.getStartTime() > 100.0) && (fi.getEndTime() < 200.0)
					&& (fi.getFault().getName().contains("SPEEDFAULT-ELLA"));
		});
		for (FaultInstance fi : res3) {
			avoidanceViolations += Math.pow(fi.getLength(), k);
		}
		;

		solution.setObjective(0, -missedDetections);
		solution.setObjective(1, -avoidanceViolations);

		String logRes = missedDetections + "," + avoidanceViolations + "," + timeProp;
		System.out.println(logRes + ":<" + solution.hashCode() + ">|" + solution);
		try {
			tempLog.write(logRes + "\n");
			tempLog.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void fakeExperimentLogTrace(FaultInstanceSetSolution solution) {
		File f = new File(fakeLogFileName);
		int lineNum = 0;

		Scanner reader;
		try {
			reader = new Scanner(f);
			// Find the result from the line num
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String[] fields = line.split(",");
				if (evaluationNumber == lineNum) {
					int detections = Integer.valueOf(fields[0]);
					int collisions = Integer.valueOf(fields[1]);
					int missedDetections = Integer.valueOf(fields[2]);

					solution.setObjective(0, -missedDetections);
					solution.setObjective(1, -collisions);
					String solutionHash = "<HASH:" + Integer.toString(solution.hashCode()) + ">";
					System.out.println(
							detections + "," + collisions + "," + missedDetections + "=" + solutionHash + solution);
				}
				lineNum++;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		evaluationNumber++;
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

			// missedDetections = missedDetections(detectionInfo, maxObjectNum);
			// TODO: for now, just compute missedDetections directly from the file
			missedDetections = Math.max(0, ((mission.getEnvironmentalObjects().size() * DETECTIONS_PER_OBJECT_EXPECTED)
					- checkDetectionCount));
			// if (missedDetections != checkMissedCount) {
			// System.out.println("WARNING: missedDetections != checkMissedCount - this may
			// be OK if there are extras recorded, otherwise a bug");
			// }
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

			if (metrics.contains(Metrics.COMBINED_DIST_METRIC)) {
				solution.setObjective(metricID++, -combinedDistMetric);
				names.add("combinedDistMetric");
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

	public void evaluate(FaultInstanceSetSolution solution) {
		if (realExperiment == 0)
			try {
				performATLASExperiment(solution);
			} catch (InvalidMetrics e) {
				e.printStackTrace();
			}

		if (realExperiment == 1)
			fakeExperiment(solution);

		if (realExperiment == 2)
			fakeExperimentLogTrace(solution);
	}

	private FaultInstance setupAdditionalInfo(FaultInstance input) {
		Fault f = input.getFault();
		FaultInstance output = input;
		if (f.getName().contains("SPEEDFAULT")) {
			double newSpeed = INITIAL_MIN_SPEED_VALUE
					+ rng.nextDouble() * (INITIAL_MAX_SPEED_VALUE - INITIAL_MIN_SPEED_VALUE);
			output.setExtraData(Double.toString(newSpeed));
		}

		if (f.getName().contains("HEADINGFAULT")) {
			double newHeading = rng.nextDouble() * 360.0;
			output.setExtraData(Double.toString(newHeading));
		}
		return output;
	}

	private FaultInstance newFaultInstance(Fault f) {
		double maxRange = f.getLatestEndTime() - f.getEarliestStartTime();
		double timeStart = f.getEarliestStartTime() + rng.nextDouble() * maxRange;

		double rangeOfEnd = f.getLatestEndTime() - timeStart;
		double timeEnd = timeStart + rng.nextDouble() * rangeOfEnd;

		FaultInstance fi = new FaultInstance(timeStart, timeEnd, f, Optional.empty());
		return setupAdditionalInfo(fi);
	}

	private void setupInitialPopulation(FaultInstanceSetSolution fiss) {
		System.out.println("Setting up initial population...");
		List<Fault> allFaults = mission.getFaultsAsList();

		Collections.shuffle(allFaults, rng);

		int i = 0;
		int limit = rng.nextInt(allFaults.size() - 1) + 1;

		for (Fault f : allFaults) {
			if (i < limit) {
				FaultInstance fi = newFaultInstance(f);
				fiss.addContents(i++, fi);
			}
		}
		System.out.println("Initial chromosome = " + fiss.toString());
	}

	public FaultInstanceSetSolution createSolution() {
		int objectivesCount = metrics.size();
		FaultInstanceSetSolution fiss = new FaultInstanceSetSolution(mission, "TAGTEST", actuallyRun, exptRunTime,
				objectivesCount);
		setupInitialPopulation(fiss);
		return fiss;
	}

	public void setAlgorithm(Algorithm<List<FaultInstance>> algorithm) {
		this.algorithm = algorithm;
	}
}
