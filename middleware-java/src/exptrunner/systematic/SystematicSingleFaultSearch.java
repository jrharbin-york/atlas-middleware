package exptrunner.systematic;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import atlasdsl.Mission;
import atlasdsl.faults.Fault;
import atlassharedclasses.FaultInstance;
import exptrunner.jmetal.FaultInstanceSetSolution;
import exptrunner.jmetal.InvalidMetrics;

import exptrunner.metrics.MetricsProcessing;

public class SystematicSingleFaultSearch extends ExptParams {
	private double minLength;
	private double maxLength;
	
	private MetricsProcessing metricsProcessing;
	private FileWriter combinedResults;
	
	// The time range to be swept 
	private double timeStart;
	private double timeEnd;
	
	private double time;
	private double len;
	
	// The fault number to use
	private Fault fault;
	
	// The current fault ID
	private int currentFault;
	
	private Optional<String> extraFaultInstanceData;
	
	private boolean completed = false;
	private double stepFactor;
	private Mission mission;
	
	public SystematicSingleFaultSearch(MetricsProcessing mp, String resFileName, double runTime, double timeStart, double timeEnd, double maxLength, double minLength, double stepFactor, Fault fault, Optional<String> extraFaultInstanceData, Mission mission) throws IOException {
		super(runTime);
		this.metricsProcessing = mp;
		this.timeStart = timeStart;
		this.time = timeStart;
		this.timeEnd = timeEnd;
		this.maxLength = maxLength;
		this.len = maxLength;
		this.minLength = minLength;
		this.completed = false;
		this.fault = fault;
		this.stepFactor = stepFactor;
		this.combinedResults = new FileWriter(resFileName);
		this.currentFault = 0;
		this.extraFaultInstanceData = extraFaultInstanceData;
		this.mission = mission;
	}


	public boolean completed() {
		return completed;
	}

	public void printState() {
		System.out.println("time = " + time + ",len = " + len);
	}
	
	public void advance() {
		time += len;
		if (time >= timeEnd) {
			time = timeStart;
			len = len * stepFactor;
		}
		currentFault++;
		
		if (len < minLength) {
			completed = true;
		}
	}

	public List<FaultInstance> specificFaults() {
		List<FaultInstance> fs = new ArrayList<FaultInstance>();
		System.out.println("Generating fault instance at " + time + " of length " + len);
		FaultInstance fi = new FaultInstance(time, time+len, fault, extraFaultInstanceData);
		fs.add(fi);
		return fs;
	}

	public void logResults(String string) {
		// The FaultInstanceSetSolution parameters
		FaultInstanceSetSolution s = new FaultInstanceSetSolution(mission, "", true, 0.0);
		s.setAllContents(specificFaults());
		try {
			metricsProcessing.readLogFiles(string, s);
			combinedResults.write(time + "," + len + ",");
			for (int i = 0; i < s.getNumberOfObjectives(); i++) {
				double m = s.getObjective(i);	
				combinedResults.write(m + ",");
				// Need to plot the fault instance additional data here
				System.out.println(metricsProcessing.getMetricByID(i) + "=" + m);
			}
			combinedResults.write("\n");
			combinedResults.flush();
		} catch (InvalidMetrics e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

