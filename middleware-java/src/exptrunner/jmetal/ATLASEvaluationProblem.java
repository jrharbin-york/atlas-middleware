package exptrunner.jmetal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

import org.uma.jmetal.problem.Problem;

import atlasdsl.Mission;
import atlasdsl.faults.Fault;
import atlassharedclasses.FaultInstance;
import exptrunner.ExptParams;
import exptrunner.FaultInstanceSet;

public class ATLASEvaluationProblem implements Problem<FaultInstanceSetSolution> {

	private static final long serialVersionUID = 1L;
	private static final double INITIAL_MIN_SPEED_VALUE = 1.0;
	private static final double INITIAL_MAX_SPEED_VALUE = 5.0;
	private int INITIAL_VARAIBLE_SIZE = 10;
	private int runCount = 0;
	private Random rng;
	
	private int initialFaultCount;
	private Mission mission;
	private boolean actuallyRun;
	private double exptRunTime;
	private String logFileDir;
	
	public ATLASEvaluationProblem(Random rng, Mission mission, int initialFaultCount, boolean actuallyRun, double exptRunTime, String logFileDir) {
		this.rng = rng;
		this.initialFaultCount = initialFaultCount;
		this.mission = mission;
		this.exptRunTime = exptRunTime;
		this.logFileDir = logFileDir;
	}

	public int getNumberOfVariables() {
		// TODO: this is fixed
		return INITIAL_VARAIBLE_SIZE;
	}

	public int getNumberOfObjectives() {
		return 1;
	}

	public int getNumberOfConstraints() {
		return 1;
	}

	public String getName() {
		return "ATLASEvaluationProblem";
	}

	public void performATLASExperiment(FaultInstanceSetSolution solution) {
		// Generate the fault instance file corresponding to this
		// Call RunExperiment.doExperiment with
		String exptTag = "exptGA-" + (runCount++);
		// TODO: change exptrunner to use the fault instance set directly - not via file
		// TODO: exptrunner has to return its double value 
		try {
			RunExperiment.doExperiment(mission, exptTag, solution.getFaultInstances(), actuallyRun, exptRunTime);
			readLogFiles(logFileDir, solution);
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	public void readLogFiles(String logFileDir, FaultInstanceSetSolution solution) {
		// Read the goal result file here - process the given goals
		// Write it out to a common result file - with the fault info
		File f = new File(logFileDir + "/goalLog.log");
		int count = 0;
		Scanner reader;
		try {
			reader = new Scanner(f);
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String[] fields = line.split(",");
				String goalClass = fields[0];
				String time = fields[1];
				String robot = fields[2];
				String num = fields[3];
				if (goalClass.equals("atlasdsl.DiscoverObjects")) {
					count++;
				}
			}
			reader.close();
			solution.setObjective(0, count);
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void evaluate(FaultInstanceSetSolution solution) {
		performATLASExperiment(solution);
		// TODO: why is evaulate void?
	}
	
	private FaultInstance setupAdditionalInfo(FaultInstance input) {
		Fault f = input.getFault();
		FaultInstance output = input;
		if (f.getName().contains("SPEEDFAULT")) {
			double newSpeed = INITIAL_MIN_SPEED_VALUE + rng.nextDouble() * (INITIAL_MAX_SPEED_VALUE - INITIAL_MIN_SPEED_VALUE);
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
		int i = 0;
		
		for (Fault f : allFaults) {
			FaultInstance fi = newFaultInstance(f);
			fiss.setContents(i++, fi);
		}
	}

	public FaultInstanceSetSolution createSolution() {
		FaultInstanceSetSolution fiss = new FaultInstanceSetSolution(mission, "TAGTEST", actuallyRun, exptRunTime);
		setupInitialPopulation(fiss);
		return fiss;
	}
}
