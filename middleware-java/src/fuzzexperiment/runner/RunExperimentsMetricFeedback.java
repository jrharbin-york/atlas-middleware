package fuzzexperiment.runner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import atlasdsl.Mission;
import fuzzexperiment.runner.metrics.Metric;
import fuzzexperiment.runner.metrics.OfflineMetric;
import fuzzingengine.FuzzingSelectionRecord;
import fuzzingengine.exptgenerator.FuzzingExperimentModifier;

public class RunExperimentsMetricFeedback extends ExptParams {

	private String fuzzCSVBaseName;
	private String resFileName;
	int count = 1;
	int countLimit;
	private Mission mission;
	List<Metric> metrics;
	
	private int populationLimit;
	private FileWriter populationLog;
	private FileWriter finalPopulationLog;
	private FileWriter finalPopulationLogNonDom;

	private FuzzingExperimentModifier g;

	public FuzzingPopulation pop;
	private List<FuzzingSelectionRecord> currentFuzzingSels;

	private String getCurrentFilename() {
		return fuzzCSVBaseName + "-" + String.format("%03d", count) + ".csv";
	}

	private void newRandomFile() {
		boolean found = false;
		int tries = 0;
		// Keep trying until we ensure we have an experiment with at least 1 entry
		// or, if we don't get any entries after 100 tries, ensure we stop to avoid infinite loop
		while (!found && (tries < 100)) {
			currentFuzzingSels = g.generateExperiment(Optional.of(getCurrentFilename()));
			tries++;
			// Stop if our experiment has at least one entry!
			if (currentFuzzingSels.size() > 0) {
				found = true;
			}
		}
	}

	public RunExperimentsMetricFeedback(String resFileName, Mission mission, String fuzzCSVBaseName, int countLimit, int populationLimit) throws IOException {
		this.resFileName = resFileName;
		this.populationLimit = populationLimit;
		this.mission = mission;
		this.countLimit = countLimit;
		this.fuzzCSVBaseName = fuzzCSVBaseName;
		this.populationLog = new FileWriter("population.log");
		this.finalPopulationLog = new FileWriter("finalPopulation.res");
		this.finalPopulationLogNonDom = new FileWriter("finalPopulationNonDom.res");
		this.pop = new FuzzingPopulation(populationLimit);
		g = new FuzzingExperimentModifier(mission);
		newRandomFile();
		
		metrics = new ArrayList<Metric>(mission.getAllMetrics());
	}

	public boolean completed() {
		return (count >= countLimit);
	}
	
	public void logPopulation() throws IOException {
		pop.logPopulation(populationLog);
	}
	
	public void printState() throws IOException {
		System.out.println("RunExperimentsMetricFeedback: Evaluating entry " + getCurrentFilename());
		populationLog.write("STARTING ITERATION " + count + ": population state\n");
	}

	public void printStateAfter(List<OfflineMetric> ms) throws IOException {
		logPopulation();
		pop.logFinalPopulation(finalPopulationLog, ms, false);
		pop.logFinalPopulation(finalPopulationLogNonDom, ms, true);
		populationLog.flush();
		finalPopulationLog.flush();
		finalPopulationLogNonDom.flush();
	}

	public void advance() {
		count++;
		newRandomFile();
	}

	public Optional<String> getNextFuzzingCSVFileName() {
		return Optional.of(getCurrentFilename());
	}

	public void advance(Map<Metric, Double> res) {
		pop.pushToPopulation(new FuzzingExptResult(currentFuzzingSels, getCurrentFilename(), res));
		count++;
		System.out.print("Population size = " + pop.currentSize() + "(limit " + populationLimit + ")");

		if (pop.currentSize() < populationLimit) {
			System.out.println("- Generating new fuzzing experiment");
			newRandomFile();
		} else {
			Optional<FuzzingExptResult> startingPoint_o = pop.pickPopulationElementToExplore();
			if (startingPoint_o.isPresent()) {
				FuzzingExptResult startingPoint = startingPoint_o.get();
				System.out.println("- Generating mutated experiment based upon " + startingPoint);
				List<FuzzingSelectionRecord> sels = startingPoint.getFuzzingSpec();
				System.out.println("startingPoint = " + startingPoint);
				System.out.println("\nSelected fuzzing:\n" + sels);
				currentFuzzingSels = g.generateExperimentBasedUpon(getCurrentFilename(), sels, res);
			}
		}
	}

	protected void printFinal(List<OfflineMetric> ms) throws IOException {
		pop.logFinalPopulation(finalPopulationLog, ms, false);
		pop.logFinalPopulation(finalPopulationLogNonDom, ms, true);
		finalPopulationLog.close();
		finalPopulationLogNonDom.close();
	}
}
