package ciexperiment.systematic;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.eclipse.epsilon.egl.exceptions.EglRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;

import atlasdsl.Mission;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import exptrunner.metrics.Metrics;
import exptrunner.metrics.MetricsProcessing;
import exptrunner.metrics.MetricsProcessing.MetricStateKeys;
import ciexperiment.runner.RunExperiment;
import faultgen.InvalidFaultFormat;

public class RepeatedRunnerSingleModel {
	private static final String EMF_OUTPUT_PATH = "/home/atlas/atlas/atlas-middleware/middleware-java/src/atlasdsl/loader/GeneratedDSLLoader.java";

	public static ModelsTransformer modelTransformer = new ModelsTransformer();
	public static ModelEGLExecutor modelExecutor = new ModelEGLExecutor();

	public static void runCIRepeatedModel(Mission baseMission, RunSameModel eparams, String exptTag, boolean actuallyRun, double timeLimit,
			List<String> ciOptions) throws InterruptedException, IOException {
		// The core logic for the loop
		String modelFile = eparams.getModelFile();
		while (!eparams.completed()) {
			eparams.printState();
			// Modify the mission from the parameters - and load the modified mission file
			// here
			try {
				for (String ciOption : ciOptions) {
					Thread.sleep(1000);
					RunExperiment.doExperimentFromFile(exptTag, actuallyRun, timeLimit, ciOption);
					eparams.logResults("/home/jharbin/academic/atlas/atlas-middleware/expt-working/logs", modelFile,
							ciOption, baseMission);
				}
				eparams.advance();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Thread.sleep(1000);
		}
	}

	public static void runCIExperimentSingleModel(String sourceModelFile, List<Metrics> metricList, String fileTag,
			List<String> ciOptions, int count, double runTime) {
		DSLLoader loader = new GeneratedDSLLoader();

		try {
			Mission baseMission = loader.loadMission();
			//double runTime = baseMission.getEndTime();
			String fileName = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
			FileWriter tempLog = new FileWriter("tempLog-" + fileName + ".res");
			MetricsProcessing mp = new MetricsProcessing(metricList, tempLog);
			mp.setMetricState(MetricStateKeys.MISSION_END_TIME, runTime);
			String resFileName = "ciexpt-" + fileTag + ".res";
			System.out.println("Starting experiment set - repeated run for " + sourceModelFile);
			RunSameModel ep = new RunSameModel(mp, runTime, sourceModelFile, resFileName, count);
			
			System.out.println("Running repeated experiments for model file " + sourceModelFile + ": count " + count);
			// Mission loader - recompile it
			modelExecutor.executeEGL(sourceModelFile, EMF_OUTPUT_PATH);
			System.out.println("Recompiling loader");
			Thread.sleep(3000);
			RunExperiment.compileLoader();
			
			runCIRepeatedModel(baseMission, ep, resFileName, true, runTime, ciOptions);

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
		l.add(Metrics.WORST_CASE_WAYPOINT_COMPLETION_FROM_CI);
		String sourceModelFile = "experiment-models/casestudy1/mission-basis.model";
		String standardCI = "atlascollectiveint.expt.casestudy1.ComputerCIshoreside_standard";
		String advancedCI = "atlascollectiveint.expt.casestudy1.ComputerCIshoreside_advanced";

		ArrayList<String> ciOptions = new ArrayList<String>();
		ciOptions.add(standardCI);
		ciOptions.add(advancedCI);
		runCIExperimentSingleModel(sourceModelFile, l, "casestudy1", ciOptions, 1, runtime);
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
		runCIExperimentSingleModel(modelFile, l, output, ciOptions, repeatCount, runtime);
	}
	
	public static void expt1_test() {
		List<Metrics> l = new ArrayList<Metrics>();
		l.add(Metrics.PURE_MISSED_DETECTIONS);
		l.add(Metrics.WORST_CASE_WAYPOINT_COMPLETION_FROM_CI);

		double runtime = 2400.0;
		int repeatCount = 30;
		String standardCI = "atlascollectiveint.expt.casestudy1.ComputerCIshoreside_standard"; 
		String energyTrackingCI = "atlascollectiveint.expt.casestudy1.ComputerCIshoreside_advanced"; 

		ArrayList<String> ciOptions = new ArrayList<String>();
		ciOptions.add(standardCI);
		ciOptions.add(energyTrackingCI);
		runCIExperimentSingleModel(
				"/home/atlas/atlas/atlas-middleware/middleware-java/experiment-models/casestudy1/mission-basis.model-5274393d-ed7c-4f48-92f4-e4ad56cdaf22.model", l,
				"casestudy1-optimal-repeated", ciOptions, repeatCount, runtime);
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
