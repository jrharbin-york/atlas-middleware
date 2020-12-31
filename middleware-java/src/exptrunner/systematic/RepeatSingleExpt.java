package exptrunner.systematic;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import atlasdsl.Mission;
import atlassharedclasses.FaultInstance;
import exptrunner.jmetal.FaultInstanceSetSolution;
import exptrunner.jmetal.InvalidMetrics;
import exptrunner.metrics.MetricsProcessing;
import faultgen.FaultFileIO;
import faultgen.InvalidFaultFormat;

public class RepeatSingleExpt extends ExptParams {
	private int runCountLimit;
	private int runCount = 0;
	private List<FaultInstance> fixedFaultInstances;
	private MetricsProcessing metricsProcessing;
	private Mission mission;
	
	private FileWriter resFile;
	
	private void setupResFile() {
		try {
			this.resFile = new FileWriter("repeatedLog.res");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public RepeatSingleExpt(MetricsProcessing mp, double runtime, int runCountLimit, Mission mission, List<FaultInstance> fixedFaultInstances) {
		super(runtime);
		this.runCountLimit = runCountLimit;
		this.runCount = 0;
		this.metricsProcessing = mp;
		this.mission = mission;
		this.fixedFaultInstances = fixedFaultInstances;
		setupResFile();
	}
	
	public RepeatSingleExpt(MetricsProcessing mp, double runtime, int runCountLimit, Mission mission, String faultFileName) throws FileNotFoundException, InvalidFaultFormat {
		super(runtime);
		this.runCountLimit = runCountLimit;
		this.runCount = 0;
		this.metricsProcessing = mp;
		this.mission = mission;
		FaultFileIO io = new FaultFileIO(mission);
		this.fixedFaultInstances = io.loadFaultsFromFile(faultFileName);
		setupResFile();
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
		// The FaultInstanceSetSolution parameters
		FaultInstanceSetSolution s = new FaultInstanceSetSolution(mission, "", true, 0.0);
		s.setAllContents(fixedFaultInstances);
		try {
			metricsProcessing.readLogFiles(string, s);
			for (int i = 0; i < s.getNumberOfObjectives(); i++) {
				double m = s.getObjective(i);	
				resFile.write(m + ",");
				System.out.println(metricsProcessing.getMetricByID(i) + "=" + m);
			}
			resFile.write("\n");
			resFile.flush();
		} catch (InvalidMetrics e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<FaultInstance> specificFaults() {
		return fixedFaultInstances;
	}	
}
