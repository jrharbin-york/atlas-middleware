package exptrunner.systematic;

import java.util.List;
import atlassharedclasses.FaultInstance;

public abstract class ExptParams {
	protected double runtime = 1200.0;

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
	public abstract List<FaultInstance> specificFaults();
}