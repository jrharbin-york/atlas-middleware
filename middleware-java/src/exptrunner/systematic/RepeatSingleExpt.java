package exptrunner.systematic;

import java.io.FileNotFoundException;
import java.util.List;

import atlasdsl.Mission;
import atlassharedclasses.FaultInstance;
import faultgen.FaultFileIO;

public class RepeatSingleExpt extends ExptParams {
	int runCountLimit;
	int runCount = 0;
	List<FaultInstance> fixedFaultInstances;
	
	public RepeatSingleExpt(double runtime, int runCountLimit, List<FaultInstance> fixedFaultInstances) {
		super(runtime);
		this.runCountLimit = runCountLimit;
		this.runCount = 0;
	}
	
	public RepeatSingleExpt(double runtime, int runCountLimit, Mission mission, String faultFileName) throws FileNotFoundException {
		super(runtime);
		this.runCountLimit = runCountLimit;
		this.runCount = 0;
		FaultFileIO io = new FaultFileIO(mission);
		this.fixedFaultInstances = io.loadFaultsFromFile(faultFileName);
	}


	public boolean completed() {
		return (runCount > runCountLimit);
	}

	public void printState() {
		System.out.println("runCount = " + runCount);		
	}

	public void advance() {
		runCount++;
	}

	public void logResults(String string) {
		// TODO: how to read the metrics in a standardised way? 
		// Factor out the metric reading code into a common package?
	}

	public List<FaultInstance> specificFaults() {
		return fixedFaultInstances;
	}	
}
