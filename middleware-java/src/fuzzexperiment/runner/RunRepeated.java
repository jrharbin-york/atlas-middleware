package fuzzexperiment.runner;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import fuzzexperiment.runner.metrics.Metric;
import fuzzexperiment.runner.metrics.OfflineMetric;

public class RunRepeated extends ExptParams {
	private int count = 0;
	private int limitCount;
	private String fixedFilename;
	
	public RunRepeated(String fixedFilename, int limitCount) {
		this.count = 0;
		this.limitCount = limitCount;
		this.fixedFilename = fixedFilename;
	}

	public boolean completed() {
		return (count >= limitCount);
	}

	public void printState() {
		System.out.println("Run count = " + count);
	}

	public void advance() {
		count++;
	}

	public Optional<String> getNextFuzzingCSVFileName() {
		return Optional.of(fixedFilename);
	}

	public void advance(Map<Metric, Double> res) {
		count++;
		
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
