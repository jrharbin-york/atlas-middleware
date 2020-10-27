package exptrunner.jmetal.test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import atlasdsl.Mission;
import exptrunner.jmetal.*;

// Dummy experiment; optimises for fault cost proportion
public class ATLASEvaluationProblemDummy extends ATLASEvaluationProblem {

	public enum EvaluationProblemDummyChoices {
		MINIMAL_FAULT_INSTANCE_COUNT,
		MAXIMAL_FAULT_INSTANCE_COUNT,
		MINIMAL_FAULT_TOTAL_TIME,
		EXPT_RUNNER_FAKE_FAULTS,
		EXPT_RUNNER_LOG_FAULTS
	}
	
	private static final long serialVersionUID = 1L;
	private FileWriter fcpValues;
	private int evalCount = 0;	
	private EvaluationProblemDummyChoices testChoice;
	
	public ATLASEvaluationProblemDummy(Random rng, Mission mission, boolean actuallyRun,
			double exptRunTime, String logFileDir, EvaluationProblemDummyChoices testChoice) throws IOException {
		
		super(rng, mission, actuallyRun, exptRunTime, logFileDir, new HashMap<GoalsToCount,Integer>(), new ArrayList<Metrics>());
		try {	
			fcpValues = new FileWriter("fcp.test");
			this.testChoice = testChoice;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void dummyExpt1(FaultInstanceSetSolution solution) {
		// Generate the fault instance file corresponding to this
		// Call RunExperiment.doExperiment with
		double fcp = solution.faultCostProportion();
		System.out.println("fcp = " + fcp);
		try {
			fcpValues.write("evalCount=" + evalCount + ",fcp = " + fcp + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		solution.setObjective(0, fcp);
		solution.setAttribute(0, 1.0);
	}
	
	public void dummyExpt2(FaultInstanceSetSolution solution) {
		// Generate the fault instance file corresponding to this
		// Call RunExperiment.doExperiment with
		int size = solution.getNumberOfVariables();
		System.out.println("size = " + size);
		try {
			fcpValues.write("evalCount=" + evalCount + ",size = " + size + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		solution.setObjective(0, size);
		solution.setAttribute(0, 1.0);
	}
	
	public void dummyExpt3(FaultInstanceSetSolution solution) {
		// Generate the fault instance file corresponding to this
		// Call RunExperiment.doExperiment with
		int size = solution.getNumberOfVariables();
		System.out.println("size = " + size);
		try {
			fcpValues.write("evalCount=" + evalCount + ",size = " + size + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		solution.setObjective(0, -size);
		solution.setAttribute(0, 1.0);
	}
	
	public void evaluate(FaultInstanceSetSolution solution) {
		evalCount++;
		if (testChoice == EvaluationProblemDummyChoices.MINIMAL_FAULT_INSTANCE_COUNT) {
			dummyExpt1(solution);
		}
		
		if (testChoice == EvaluationProblemDummyChoices.MAXIMAL_FAULT_INSTANCE_COUNT) {
			dummyExpt2(solution);
		}
		
		if (testChoice == EvaluationProblemDummyChoices.MINIMAL_FAULT_TOTAL_TIME) {
			dummyExpt3(solution);
		}
	}
}
