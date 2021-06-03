package fuzzexperiment.runner;

import java.util.Optional;

import atlasdsl.Mission;
import fuzzingengine.exptgenerator.FuzzingExperimentGenerator;

public class RunRandomlyGeneratedExperiments extends ExptParams {
	private String resFileName;
	int count = 0;
	int countLimit;
	private Mission mission;
	private String fuzzCSVBaseName;
	private	FuzzingExperimentGenerator g;
	
	private String getCurrentFilename() {
		return fuzzCSVBaseName + "-" + count + ".csv";
	}
	
	private void newGeneratedFile() {
		g.generateExperiment(Optional.of(getCurrentFilename()));
	}
	
	public RunRandomlyGeneratedExperiments(String resFileName, Mission mission, String fuzzCSVBaseName, int countLimit) {
		this.resFileName = resFileName;
		this.mission = mission;
		this.countLimit = countLimit;
		this.fuzzCSVBaseName = fuzzCSVBaseName;
		g = new FuzzingExperimentGenerator(mission);
		newGeneratedFile();
	}

	public boolean completed() {
		return (count >= countLimit);
	}

	public void printState() {
		System.out.println("Evaluating entry " + getCurrentFilename());
	}

	public void advance() {
		count++;
		newGeneratedFile();
	}

//	public void logResults(String string, String modelFile) {
//
//	}

	public Optional<String> getNextFuzzingCSVFileName() {
		return Optional.of(getCurrentFilename());
	}
}