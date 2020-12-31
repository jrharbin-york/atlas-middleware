package exptrunner.systematic;

import java.util.HashMap;
import java.util.List;
import atlassharedclasses.FaultInstance;
import exptrunner.jmetal.FaultInstanceSetSolution;

public abstract class ExptParams {
	protected double runtime = 1200.0;
	
	protected HashMap<FaultInstanceSetSolution,Double> solutionLog = new HashMap<FaultInstanceSetSolution,Double>();

	public double getTimeLimit() {
		return runtime;
	}
	
	public ExptParams(double runtime) {
		this.runtime = runtime;
	}

	public abstract boolean completed();
	public abstract void printState();
	public abstract void advance();
	public abstract void logResults(String string);
	
	public HashMap<FaultInstanceSetSolution,Double> returnResultsInfo() {
		return solutionLog;
	}
	
	public abstract List<FaultInstance> specificFaults();
}