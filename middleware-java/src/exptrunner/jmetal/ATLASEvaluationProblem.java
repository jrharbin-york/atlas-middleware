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
import exptrunner.metrics.MetricsProcessing;
import utils.Pair;

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
	private Algorithm<List<FaultInstance>> algorithm;
	private MetricsProcessing metricsProcessing;

	private FileWriter tempLog;
	private int variableFixedSize;
	private int constraintCount = 0;
	private int fakeCustomEvalNum;
	private int popSize;
	
	// Only used in the fake test experiment
	private HashMap<Integer, Pair<Double, Double>> fakeExptHashes = new HashMap<Integer, Pair<Double, Double>>();
	
	public ATLASEvaluationProblem(int popSize, Random rng, Mission mission, boolean actuallyRun, double exptRunTime,
			String logFileDir, Map<GoalsToCount, Integer> goalsToCount, List<Metrics> metrics) throws IOException {
		this.rng = rng;
		this.popSize = popSize;
		this.mission = mission;
		this.exptRunTime = exptRunTime;
		this.logFileDir = logFileDir;
		this.actuallyRun = actuallyRun;
		this.goalsToCount = goalsToCount;
		this.variableFixedSize = mission.getFaultsAsList().size();
		String fileName = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		this.tempLog = new FileWriter("tempLog-" + fileName + ".res");
		metricsProcessing = new MetricsProcessing(mission, metrics, tempLog);
		System.out.println(metrics.toString());
		
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
		return metricsProcessing.getMetrics().size();
	}

	public int getNumberOfConstraints() {
		return constraintCount;
	}

	public String getName() {
		return "ATLASEvaluationProblem";
	}

	public void performATLASExperiment(FaultInstanceSetSolution solution) throws InvalidMetrics {
		String exptTag = "exptGA-" + (runCount++);
		try {
			RunExperiment.doExperiment(mission, exptTag, solution.getFaultInstances(), actuallyRun, exptRunTime);
			metricsProcessing.readLogFiles(logFileDir, solution);
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	public void fakeExperiment(FaultInstanceSetSolution solution) {
		int missedDetections = 0;
		int avoidanceViolations = 0;
		double k = 1.0;

		double time = solution.faultTimeTotal();

		solution.setObjective(2, time);

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

		String logRes = missedDetections + "," + avoidanceViolations + "," + time;
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



	
	public void fakeExperimentCustomValues(FaultInstanceSetSolution solution) {
		
		
		System.out.println("fakeExperimentCustomValues");
		int numInPop = fakeCustomEvalNum % popSize;
		int gen = fakeCustomEvalNum / popSize;
		
		System.out.println("numInPop = " + numInPop + ",gen = " + gen);

		Pair<Double, Double> res = fakeExptHashes.get(solution.hashCode());
		if (res == null) {
			if (numInPop == 0 && gen < 3) {
				System.out.println("Overriding the metrics for numInPop = " + numInPop + ",gen=" + gen);
				solution.setObjective(0, -500 - gen);
				solution.setObjective(1, 10 + gen);
				fakeExptHashes.put(solution.hashCode(), new Pair<Double,Double>(solution.getObjective(0), solution.getObjective(1)));
			} else {
				int random1 = rng.nextInt(100);
				int random2 = rng.nextInt(100);
				solution.setObjective(0, random1);
				solution.setObjective(1, random2);	
			}
			
		} else {
			solution.setObjective(0, res.getElement0());
			solution.setObjective(1, res.getElement1());
		}
		
		System.out.println("HASH: " + solution.hashCode() + ":o1 = " + solution.getObjective(0) + ",o2 = " + solution.getObjective(1));
		fakeCustomEvalNum++;
	}


	
	public void evaluate(FaultInstanceSetSolution solution) {
	
		if (realExperiment == 0) {
			try {
				performATLASExperiment(solution);
			} catch (InvalidMetrics e) {
				e.printStackTrace();
			}
		}

		if (realExperiment == 1)
			fakeExperiment(solution);

		if (realExperiment == 2)
			fakeExperimentLogTrace(solution);
		
		if (realExperiment == 3) {
			fakeExperimentCustomValues(solution);
		}
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

	private boolean faultShouldBeUsed(Fault f) {
		// return f.getName().contains("HEADINGFAULT");
		return true;
	}

	private void setupInitialPopulation(FaultInstanceSetSolution fiss) {
		System.out.println("Setting up initial population...");
		List<Fault> allFaults = mission.getFaultsAsList();

		Collections.shuffle(allFaults, rng);

		int i = 0;
		int FAULTS_COUNT_UNIQUE = 3;
		int limit = rng.nextInt(allFaults.size() * FAULTS_COUNT_UNIQUE - 1) + 1;

		while (i < limit) {
			for (Fault f : allFaults) {
				if (faultShouldBeUsed(f)) {
					FaultInstance fi = newFaultInstance(f);
					fiss.addContents(i++, fi);
				}
			}
		}
		System.out.println("Initial chromosome = " + fiss.toString());
	}

	public FaultInstanceSetSolution createSolution() {
		int objectivesCount = metricsProcessing.getMetrics().size();
		FaultInstanceSetSolution fiss = new FaultInstanceSetSolution(mission, "TAGTEST", actuallyRun, exptRunTime);
		setupInitialPopulation(fiss);
		return fiss;
	}

	public void setAlgorithm(Algorithm<List<FaultInstance>> algorithm) {
		this.algorithm = algorithm;
	}
}
