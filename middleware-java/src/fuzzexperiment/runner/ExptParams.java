package fuzzexperiment.runner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import exptrunner.jmetal.FuzzingSelectionsSolution;
import fuzzexperiment.runner.metrics.Metric;
import fuzzexperiment.runner.metrics.OfflineMetric;

public abstract class ExptParams {
	
	protected HashMap<FuzzingSelectionsSolution,Double> solutionLog = new HashMap<FuzzingSelectionsSolution,Double>();

	public abstract boolean completed();
	public abstract void printState() throws IOException;
	public abstract void printStateAfter(List<OfflineMetric> ms) throws IOException;
	public abstract void advance();
	public abstract void advance(Map<Metric, Double> res);
	
	public HashMap<FuzzingSelectionsSolution,Double> returnResultsInfo() {
		return solutionLog;
	}
	
	protected abstract Optional<String> getNextFuzzingCSVFileName();
	protected abstract void printFinal(List<OfflineMetric> ms) throws IOException;
}