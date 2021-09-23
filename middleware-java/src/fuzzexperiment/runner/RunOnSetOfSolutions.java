package fuzzexperiment.runner;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fuzzexperiment.runner.metrics.Metric;
import fuzzexperiment.runner.metrics.OfflineMetric;

public class RunOnSetOfSolutions extends ExptParams {
	private String resFileName;
	List<String> fuzzingFilenames;
	int activeElt = 0;
	
	public RunOnSetOfSolutions(String resFileName, List<String> fuzzingFilenames) {
		this.resFileName = resFileName;
		this.fuzzingFilenames = fuzzingFilenames;
	}

	public boolean completed() {
		return (activeElt >= fuzzingFilenames.size());
	}

	public void printState() {
		System.out.println("Evaluating entry " + activeElt + ":" + fuzzingFilenames.get(activeElt));
	}

	public void advance() {
		activeElt++;
	}

	public void logResults(String string, String modelFile) {

	}

	public Optional<String> getNextFuzzingCSVFileName() {
		String filename = fuzzingFilenames.get(activeElt);
		if (filename != null) {
			return Optional.of(filename);
		} else {
			return Optional.empty();
		}
	}
	
	public void advance(Map<Metric, Double> res) {
		activeElt++;	
	}

	@Override
	public void printStateAfter(List<OfflineMetric> ms) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void printFinal(List<OfflineMetric> ms) {
		// TODO Auto-generated method stub
		
	}
}
