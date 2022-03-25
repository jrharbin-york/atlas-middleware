package ciexperiment.systematic;

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
import exptrunner.metrics.CompletedRoomsCount;
import exptrunner.metrics.MetricsProcessing.MetricStateKeys;
import exptrunner.metrics.*;

import ciexperiment.runner.RunExperiment;
import ciexperiment.runner.RunExperimentROS;
import faultgen.InvalidFaultFormat;

public class RepeatedRunner {
	private static final String EMF_OUTPUT_PATH = "/home/jharbin/academic/atlas/atlas-middleware/middleware-java/src/atlasdsl/loader/GeneratedDSLLoader.java";
	
	public static ModelsTransformer modelTransformer = new ModelsTransformer();
	public static ModelEGLExecutor modelExecutor = new ModelEGLExecutor();

	public static void runCIExptLoop(Mission baseMission, ExptParams eparams, String exptTag, boolean actuallyRun, double timeLimit, List<String> ciOptions, String simulatorType) throws InterruptedException, IOException {
		// The core logic for the loop
		while (!eparams.completed()) {
			eparams.printState();
			// Modify the mission from the parameters - and load the modified mission file here
			try {
				Optional<String> nextFile_o = eparams.getNextFileName();
				if (nextFile_o.isPresent()) {
					String file = nextFile_o.get();
					String workingDir = "";
					
					for (String ciOption : ciOptions) {
						System.out.println("Running experiments for model file " + file + " and CI class " + ciOption);
						// Mission loader - recompile it
						modelExecutor.executeEGL(file, EMF_OUTPUT_PATH);
						System.out.println("Recompiling loader");
						RunExperiment.compileLoader();
						
						// The newly recompiled loader is not used by this process, only by subprocesses
						// So they see all the changes to the mission
						Thread.sleep(3000);
						System.out.println("Loader recompilation done");
						if (simulatorType.equals("MOOS")) {
							workingDir = RunExperiment.doExperimentFromFile(exptTag, actuallyRun, timeLimit, ciOption);
						} 
						
						if (simulatorType.equals("ROS")) {
							workingDir = RunExperimentROS.doExperimentFromFile(exptTag, actuallyRun, timeLimit, ciOption);
						}
						// Currently, any metrics will be computed using the state of the 
						// parent mission - metrics cannot depend on the recompiled mission!
						String logDir = workingDir + "/logs";
						eparams.logResults(logDir, file, ciOption, baseMission);
						
					}
					
					eparams.advance();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EolModelLoadingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EglRuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			Thread.sleep(1000);
		}
	}
	
	private static Mission getCurrentMission() throws DSLLoadFailed {
		DSLLoader dl = new GeneratedDSLLoader();
		return dl.loadMission();
	}

	public static void runCIExperiment(String sourceModelFile, List<Metrics> oldMetricList, List<OfflineMetric> newMetricList, String fileTag, List<String> ciOptions, String simulatorType) {
		DSLLoader loader = new GeneratedDSLLoader();

		try {
			Mission baseMission = loader.loadMission();
			double runTime = baseMission.getEndTime();
			String fileName = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
			FileWriter tempLog = new FileWriter("tempLog-" + fileName + ".res");
			
			String resFileName = "ciexpt-"+fileTag+".res";
			System.out.println("Model generation beginning for " + sourceModelFile);
			List<String> missionFiles = modelTransformer.retriveAllModels(sourceModelFile);
			
			// TODO: add model filteration according to the EVL constraints here?
			
			System.out.println("Model generation completed successfully!");
			Thread.sleep(10000);
			System.out.println("Starting experiment set");
			
			ExptParams ep;
			if (newMetricList.size() > 0) {
				System.out.println("Using NEW metric infrastructure");
				MetricHandler mh = new MetricHandler(newMetricList, resFileName);
				ep = new RunOnSetOfModelsNewMetrics(mh, runTime, missionFiles, resFileName);
				runCIExptLoop(baseMission, ep, resFileName, true, runTime, ciOptions, simulatorType);
			} else {
				//ep = new RunOnSetOfModels(mp, runTime, missionFiles, resFileName);
			}
			
			
			
			System.out.println("Done");
		} catch (DSLLoadFailed e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvalidFaultFormat e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void expt_caseStudy1() throws EolModelLoadingException, EglRuntimeException, URISyntaxException, IOException, InterruptedException {
		List<Metrics> l = new ArrayList<Metrics>();
		l.add(Metrics.PURE_MISSED_DETECTIONS);
		l.add(Metrics.WORST_CASE_WAYPOINT_COMPLETION_FROM_CI);
		String sourceModelFile = "experiment-models/casestudy1/mission-basis.model";
		String standardCI = "atlascollectiveint.expt.casestudy1.ComputerCIshoreside_standard"; 
		String advancedCI = "atlascollectiveint.expt.casestudy1.ComputerCIshoreside_advanced"; 
		
		ArrayList<String> ciOptions = new ArrayList<String>();
		ciOptions.add(standardCI);
		ciOptions.add(advancedCI);
		
		// Need to recompile the basis models - this is just to ensure e.g. the mission completion times are set properly
		modelExecutor.executeEGL(sourceModelFile, EMF_OUTPUT_PATH);
		Thread.sleep(1000);
		// TODO: convert this to new Metrics infrastructure
		runCIExperiment(sourceModelFile, l, new ArrayList<OfflineMetric>(), "casestudy1", ciOptions, "MOOS");
	}
	
	public static void expt_caseStudy2() throws EolModelLoadingException, EglRuntimeException, URISyntaxException, IOException, InterruptedException {
		List<Metrics> l = new ArrayList<Metrics>();
		l.add(Metrics.OBSTACLE_AVOIDANCE_METRIC);
		l.add(Metrics.AVOIDANCE_METRIC);
		l.add(Metrics.TOTAL_ENERGY_AT_END);
		l.add(Metrics.MEAN_ENERGY_AT_END);
		l.add(Metrics.TOTAL_FINAL_DISTANCE_AT_END);
		l.add(Metrics.MEAN_FINAL_DISTANCE_AT_END);
		l.add(Metrics.TOTAL_WAYPOINT_SWITCH_COUNT);

		String basisModel = "experiment-models/casestudy2/mission-basis.model";
		String standardCI = "atlascollectiveint.expt.casestudy2.ComputerCIshoreside_standard";
		String energyTrackingCI = "atlascollectiveint.expt.casestudy2.ComputerCIshoreside_energytracking";
		
		ArrayList<String> ciOptions = new ArrayList<String>();
		ciOptions.add(standardCI);
		ciOptions.add(energyTrackingCI);
		
		modelExecutor.executeEGL(basisModel, EMF_OUTPUT_PATH);
		Thread.sleep(1000);
		
		// Standard is threshold of 750 mAh for return
		
		// TODO: convert this to new Metrics infrastructure
		runCIExperiment(basisModel, l, new ArrayList<OfflineMetric>(), "casestudy2-threshold", ciOptions, "MOOS");
		//runCIExperiment("experiment-models/casestudy2/mission-basis-threshold500.model", l, "casestudy2-threshold500", ciOptions);
		//runCIExperiment("experiment-models/casestudy2/mission-basis-threshold250.model", l, "casestudy2-threshold250", ciOptions);
		
	}
	
	public static void expt_caseStudyHealthcare() throws EolModelLoadingException, EglRuntimeException, URISyntaxException, IOException, InterruptedException {
		List<OfflineMetric> newMetrics = new ArrayList<OfflineMetric>();

		// TODO: add the metrics from the DSL
		newMetrics.add(new CompletedRoomsCount());
		newMetrics.add(new TotalRobotEnergyAtEnd());
		
		// TODO: need to setup metrics from the goals
				
		String basisModel = "experiment-models/healthcare/missionHealthcare-basis.model";
		String standardCI = "atlascollectiveint.expt.healthcare.ComputerCIshoreside_healthcare";
		//String randomCI = "atlascollectiveint.expt.healthcare.ComputerCIshoreside_healthcare_random";
		
		ArrayList<String> ciOptions = new ArrayList<String>();
		ciOptions.add(standardCI);
		// TODO: implement more advanced CI, add it here
		
		modelExecutor.executeEGL(basisModel, EMF_OUTPUT_PATH);
		Thread.sleep(1000);
		
		runCIExperiment(basisModel, new ArrayList<Metrics>(), newMetrics, "casestudy-healthcare", ciOptions, "ROS");
	}
}
