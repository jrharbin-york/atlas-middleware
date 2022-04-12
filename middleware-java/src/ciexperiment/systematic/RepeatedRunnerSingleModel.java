package ciexperiment.systematic;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import atlasdsl.Mission;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import exptrunner.metrics.CompletedRoomsCount;
import exptrunner.metrics.Metrics;
import exptrunner.metrics.MissionCompletionTime;
import exptrunner.metrics.OfflineMetric;
import exptrunner.metrics.TotalRobotEnergyAtEnd;
import ciexperiment.runner.RunExperiment;
import ciexperiment.runner.RunExperimentROS;

public class RepeatedRunnerSingleModel {
	private static final String EMF_OUTPUT_PATH = "/home/jharbin/academic/atlas/atlas-middleware/middleware-java/src/atlasdsl/loader/GeneratedDSLLoader.java";
	private static final String HEALTHCARE_LOG_DIR = "/home/jharbin/academic/atlas/atlas-middleware/expt-working/healthcare/logs";
	
	public static ModelsTransformer modelTransformer = new ModelsTransformer();
	public static ModelEGLExecutor modelExecutor = new ModelEGLExecutor();

	public static void runCIRepeatedModel(Mission baseMission, ExptParams eparams, String exptTag, boolean actuallyRun, double timeLimit,
			List<String> ciOptions, String simulatorType) throws InterruptedException, IOException {
		// The core logic for the loop
		String modelFile = eparams.getModelFile();
		while (!eparams.completed()) {
			eparams.printState();
			// Modify the mission from the parameters - and load the modified mission file
			// here
			try {
				for (String ciOption : ciOptions) {
					Thread.sleep(1000);
					
					if (simulatorType.equals("MOOS")) {
						RunExperiment.doExperimentFromFile(exptTag, actuallyRun, timeLimit, ciOption);
					}
					
					if (simulatorType.equals("ROS")) {
						RunExperimentROS.doExperimentFromFile(exptTag, actuallyRun, timeLimit, ciOption);
					}
					
					eparams.logResults(HEALTHCARE_LOG_DIR, modelFile, ciOption, baseMission);
				}
				eparams.advance();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Thread.sleep(1000);
		}
	}

	public static void runCIExperimentSingleModel(String sourceModelFile, List<Metrics> oldMetricList, List<OfflineMetric> newMetricList, String fileTag,
			List<String> ciOptions, int count, double runTime, String simulatorType) {
		DSLLoader loader = new GeneratedDSLLoader();

		try {
			Mission baseMission = loader.loadMission();
			//double runTime = baseMission.getEndTime();
			String fileName = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
			FileWriter tempLog = new FileWriter("tempLog-" + fileName + ".res");
			
			String resFileName = "ciexpt-"+fileTag+".res";
			
			if (newMetricList.size() > 0) {
				System.out.println("Running repeated experiments for model file " + sourceModelFile + ": count " + count);
				// Mission loader - recompile it
				modelExecutor.executeEGL(sourceModelFile, EMF_OUTPUT_PATH);
				System.out.println("Recompiling loader");
				Thread.sleep(3000);
				RunExperiment.compileLoader();
				
				System.out.println("Using NEW metric infrastructure");
				System.out.println("Starting experiment set - repeated run for " + sourceModelFile);
				MetricHandler mh = new MetricHandler(newMetricList, resFileName);
				ExptParams ep = new RunSameModelNewMetrics(mh, runTime, sourceModelFile, resFileName, count);
				runCIRepeatedModel(baseMission, ep, resFileName, true, runTime, ciOptions, simulatorType);
			} else {
				//MetricsProcessing mp = new MetricsProcessing(oldMetricList, tempLog);
				//ep = new RunOnSetOfModels(mp, runTime, missionFiles, resFileName);
			}			

			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void expt_caseStudy1() {
		double runtime = 2400.0;
		List<Metrics> l = new ArrayList<Metrics>();
		l.add(Metrics.PURE_MISSED_DETECTIONS);
		// l.add(Metrics.DETECTION_COMPLETION_TIME);
		l.add(Metrics.WORST_CASE_WAYPOINT_COMPLETION_TIME_FROM_CI);
		
		List<OfflineMetric> m = new ArrayList<OfflineMetric>();
		
		String sourceModelFile = "experiment-models/casestudy1/mission-basis.model";
		String standardCI = "atlascollectiveint.expt.casestudy1.ComputerCIshoreside_standard";
		String advancedCI = "atlascollectiveint.expt.casestudy1.ComputerCIshoreside_advanced";

		ArrayList<String> ciOptions = new ArrayList<String>();
		ciOptions.add(standardCI);
		ciOptions.add(advancedCI);
		runCIExperimentSingleModel(sourceModelFile, l, m, "casestudy1", ciOptions, 1, runtime, "MOOS");
	}
	
	public static void expt_caseStudyHealthcare() {
		double runtime = 500.0;
		System.out.println("Case study runtime is " + runtime);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<OfflineMetric> newMetrics = new ArrayList<OfflineMetric>();
		List<Metrics> l = new ArrayList<Metrics>();
			
		// TODO: read this from the file
		newMetrics.add(new CompletedRoomsCount());
		newMetrics.add(new TotalRobotEnergyAtEnd());
		newMetrics.add(new MissionCompletionTime());
			
		//String sourceModelFile = "experiment-models/healthcare/missionHealthcare-basis.model-d045d342-4a3f-45e1-b7c0-c7e7c752aab2.model";
		//String sourceModelFile = "experiment-models/healthcare/missionHealthcare-basis.model-b6032518-c185-4b69-b177-35d3c8bac6f7.model";
		
		String sourceModelFile = "experiment-models/healthcare/missionHealthcare-basis.model-c933ce9d-f812-4142-bbc3-090f7f00999e.model";

		String standardCI = "atlascollectiveint.expt.healthcare.ComputerCIshoreside_healthcare";
		String dynamicCI = "atlascollectiveint.expt.healthcare.ComputerCIshoreside_healthcare_dynamicenergy";

		ArrayList<String> ciOptions = new ArrayList<String>();
		//ciOptions.add(standardCI);
		ciOptions.add(dynamicCI);
		
		runCIExperimentSingleModel(sourceModelFile, l, newMetrics, "casestudyHealthcare_repeated-c933ce9d", ciOptions, 10, runtime, "ROS");
	}

	public static void expt2_test(String modelFile, String output) {
		double runtime = 1200.0;
		List<Metrics> l = new ArrayList<Metrics>();
		l.add(Metrics.OBSTACLE_AVOIDANCE_METRIC);
		l.add(Metrics.AVOIDANCE_METRIC);
		l.add(Metrics.TOTAL_ENERGY_AT_END);
		l.add(Metrics.MEAN_ENERGY_AT_END);
		l.add(Metrics.TOTAL_FINAL_DISTANCE_AT_END);
		l.add(Metrics.MEAN_FINAL_DISTANCE_AT_END);
		l.add(Metrics.TOTAL_WAYPOINT_SWITCH_COUNT);

		int repeatCount = 30;

		String standardCI = "atlascollectiveint.expt.casestudy2.ComputerCIshoreside_standard";
		String energyTrackingCI = "atlascollectiveint.expt.casestudy2.ComputerCIshoreside_energytracking";

		ArrayList<String> ciOptions = new ArrayList<String>();
		ciOptions.add(standardCI);
		ciOptions.add(energyTrackingCI);
		//runCIExperimentSingleModel(modelFile, l, output, ciOptions, repeatCount, runtime, "MOOS");
	}
	
	public static void expt1_test() {
		List<Metrics> l = new ArrayList<Metrics>();
		l.add(Metrics.PURE_MISSED_DETECTIONS);
		l.add(Metrics.WORST_CASE_WAYPOINT_COMPLETION_TIME_FROM_CI);

		double runtime = 2400.0;
		int repeatCount = 30;
		String standardCI = "atlascollectiveint.expt.casestudy1.ComputerCIshoreside_standard"; 
		String energyTrackingCI = "atlascollectiveint.expt.casestudy1.ComputerCIshoreside_advanced"; 

		ArrayList<String> ciOptions = new ArrayList<String>();
		ciOptions.add(standardCI);
		ciOptions.add(energyTrackingCI);
		//runCIExperimentSingleModel(
		//		"/home/atlas/atlas/atlas-middleware/middleware-java/experiment-models/casestudy1/mission-basis.model-5274393d-ed7c-4f48-92f4-e4ad56cdaf22.model", l,
		//		"casestudy1-optimal-repeated", ciOptions, repeatCount, runtime, "MOOS");
	}
	
	public static void run1() {
		expt1_test();
		expt2_test("experiment-models/casestudy2/mission-basis.model-a208758b-642e-4bf7-bdb5-278890b6050f.model", "casestudy2-optimal-repeated");

	}
	
	public static void run2() {
		expt2_test("experiment-models/casestudy2/mission-basis.model-42addeeb-3bb1-4151-8053-630979f57604.model", "casestudy2-worst-repeated");
		expt1_test();
	}

	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		run2();
	}
}
