package fuzzexperiment.runner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import atlasdsl.Mission;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import carsspecific.ros.codegen.ROSCodeGen;
import fuzzexperiment.runner.metrics.*;
import fuzzingengine.FuzzingEngine;
import fuzzingengine.spec.GeneratedFuzzingSpec;
import middleware.atlascarsgenerator.ConversionFailed;

public class FuzzExptRunner {
	private boolean actuallyRun = true;
	private double timeLimit;
	private MetricHandler mh;
	private ExptParams eparams;
	private Mission baseMission;
	private String logPath;
	private StartFuzzingProcesses runner;
	
	private void readProperties() {
		Properties prop = new Properties();
		String fileName = "fuzzingexpt.config";
		InputStream is = null;
		try {
		    is = new FileInputStream(fileName);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		try {
		    prop.load(is);
		    logPath = prop.getProperty("paths.ros.log");
		    String bashPath = prop.getProperty("paths.bash_script");
		    String workingPath = prop.getProperty("paths.working");
		    String middlewarePath = prop.getProperty("paths.middleware");
		    runner = new StartFuzzingProcesses(bashPath, workingPath, middlewarePath);
		    
		    is.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void setup() throws DSLLoadFailed, IOException {
		DSLLoader loader = new GeneratedDSLLoader();
		baseMission = loader.loadMission();
		timeLimit = baseMission.getEndTime();
		FileWriter tempLog = new FileWriter("fuzzexpt-templog.log");
		readProperties();
	}
	
	public FuzzExptRunner(ExptParams ep, MetricHandler mh) throws DSLLoadFailed, IOException {
		this.eparams = ep;
		this.mh = mh;
		setup();
	}
	
	public void usefakeRun() {
		actuallyRun = false;
	}
	
	public void writeToFile(Map<Metric,Object> results, String metricResFile) {
		// 
	}
	
	public void run() throws InterruptedException, IOException {
		// The core logic for the loop
		
		// Read fuzzing specification?
		// Read the experiment specification as a model - or use the generated CSV creator...
		// In the loop...
		
		while (!eparams.completed()) {
			Map<Metric, Object> metricResults;
			
			eparams.printState();
			// Get the CSV file consisting of a fuzzing experiment... internally either generate it 
			// in response to the metrics, or it comes from a constant list
			Optional<String> nextFile_o = eparams.getNextFuzzingCSVFileName();
			if (nextFile_o.isPresent()) {
				String file = nextFile_o.get();
				String exptTag = "exptcsv-" + file;
				System.out.println("Running fuzzing experiments for CSV file name " + file);
				
				// Generate the ROS configuration files, e.g. modified launch scripts, YAML
				// config files etc for this CSV definition experimental run
				runner.codeGenerationROSFuzzing(baseMission, file);
				
				// Invoke the middleware (with the correct mission model and fuzzing spec!)
				// Invoke the CARS / call ROS launch scripts
				runner.doExperimentFromFile(exptTag, actuallyRun, timeLimit, file);
				
				// Assess the metrics (which the user defined using filled-in templates)
				// This should be done by the metrics handler now
				try {
					metricResults = mh.computeAllOffline(logPath);
					mh.printMetricsToOutputFile(file, metricResults);
					eparams.advance(metricResults);
					
				} catch (MetricComputeFailure e) {
					// If we get metric computation failure, ignore the whole line
					// TODO: log the failure
					e.printStackTrace();
					eparams.advance();
				}	
			}	
		}
		System.out.println("Run completed");
	}
}
