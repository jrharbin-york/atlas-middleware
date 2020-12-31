package exptrunner.systematic;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.uma.jmetal.util.JMetalException;

import atlasdsl.Mission;
import atlasdsl.Robot;
import atlasdsl.faults.Fault;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import atlassharedclasses.FaultInstance;
import exptrunner.jmetal.FaultInstanceSetSolution;
import exptrunner.jmetal.Metrics;
import exptrunner.jmetal.RunExperiment;
import exptrunner.jmetal.obsolete.SingleFaultCoverageExpt;
import exptrunner.metrics.MetricsProcessing;

public class SystematicRunner {
	private static final double HIGH_RESULT_PROPORTION = 0.1;
	private static final double LOW_RESULT_PROPORTION = 0.1;
	private static final int BEST_VERIFY_COUNT = 0;
	private static final int WORST_VERIFY_COUNT = 0;
	private static double runTime = 1200.0;
	
	public static void runGeneralExpt(Mission mission, ExptParams eparams, String exptTag, boolean actuallyRun, double timeLimit) throws InterruptedException, IOException {
		while (!eparams.completed()) {
			eparams.printState();
			List<FaultInstance> fis = eparams.specificFaults();
			RunExperiment.doExperiment(mission, exptTag, fis, actuallyRun, timeLimit);
			eparams.logResults("/home/jharbin/academic/atlas/atlas-middleware/expt-working/logs");
			eparams.advance();
		}
	}
	
	public static Optional<HashMap<FaultInstanceSetSolution,Double>> runCoverage(List<Metrics> metricList, Mission mission, String faultName, double additionalDataVal) {
		try {
			Optional<Fault> f_o = mission.lookupFaultByName(faultName);
			if (f_o.isPresent()) {
				String resFileName = faultName;
				Fault f = f_o.get();
				Optional<String> speedOverride_o = Optional.of(Double.toString(additionalDataVal));
				resFileName = resFileName + "_coverage_" + additionalDataVal + ".res";
				FileWriter tempLog = new FileWriter("tempLog-" + faultName + ".res");
				MetricsProcessing mp = new MetricsProcessing(mission, metricList, tempLog);
				ExptParams ep = new SystematicSingleFaultSearch(mp, resFileName, runTime, 0.0, runTime, runTime, 50.0, 0.5, f,
						speedOverride_o, mission);
				runGeneralExpt(mission, ep, faultName + "_coverage", false, runTime);
				System.out.println("Done");
				return Optional.of(ep.returnResultsInfo());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}
	
	public static void runRepeated(List<Metrics> l, List<FaultInstanceSetSolution> res, int runCount) {
		for (FaultInstanceSetSolution fis : res) {
			// TODO: produce a name for each and tag
			// for now, just use the start of the first fault and length
			List<FaultInstance> finstances = fis.getFaultInstances();
			FaultInstance fi1 = finstances.get(0);
			String fileTag = "COVERAGE-" + Double.toString(fi1.getStartTime()) + "-" + Double.toString(fi1.getEndTime());
			RepeatedRunner.runRepeatedFaultSetFI(l, finstances, fileTag, runCount);
		}
	}
	
	public static void main(String[] args) throws JMetalException, FileNotFoundException {
		DSLLoader loader = new GeneratedDSLLoader();
		try {
			Mission mission = loader.loadMission();
			List<Metrics> l = new ArrayList<Metrics>();
			
			if (mission.getAllRobots().size() > 2) {
				l.add(Metrics.PURE_MISSED_DETECTIONS);
			} else {
				l.add(Metrics.OBSTACLE_AVOIDANCE_METRIC);
			}
			
			l.add(Metrics.TIME_TOTAL_ABSOLUTE);
		
			List<Robot> vehicles = mission.getAllRobots();
		
			double headingStep=30.0;
			
			List<Double> speeds = new ArrayList<Double>();
			speeds.add(2.0);
			speeds.add(3.0);
			speeds.add(4.0);
			speeds.add(5.0);
		
			for (Robot vehicle : vehicles) {
				for (double speed : speeds) {
					String vname = vehicle.getName().toUpperCase();
					System.out.println("Running speed experiments for " + vname + " - speed=" + speed);
					Optional<HashMap<FaultInstanceSetSolution,Double>> res_o = runCoverage(l, mission, "SPEEDFAULT-" + vname, speed);
					if (res_o.isPresent()) {
						HashMap<FaultInstanceSetSolution,Double> res = res_o.get();
						List<FaultInstanceSetSolution> best = filterBest(res, HIGH_RESULT_PROPORTION);
						List<FaultInstanceSetSolution> worst = filterWorst(res, LOW_RESULT_PROPORTION);
						runRepeated(l, best, BEST_VERIFY_COUNT);
						runRepeated(l, worst, WORST_VERIFY_COUNT);
					}
				}
			}
			
			for (Robot vehicle : vehicles) {
				for (double heading = 0; heading < 360.0; heading+=headingStep) {
					String vname = vehicle.getName().toUpperCase();
					System.out.println("Running heading experiments for " + vname + " - heading=" + heading);
					Optional<HashMap<FaultInstanceSetSolution,Double>> res_o = runCoverage(l, mission, "HEADINGFAULT-" + vname, heading);
					if (res_o.isPresent()) {
						HashMap<FaultInstanceSetSolution,Double> res = res_o.get();
						List<FaultInstanceSetSolution> best = filterBest(res, HIGH_RESULT_PROPORTION);
						List<FaultInstanceSetSolution> worst = filterWorst(res, LOW_RESULT_PROPORTION);
						runRepeated(l, best, BEST_VERIFY_COUNT);
						runRepeated(l, worst, WORST_VERIFY_COUNT);
					}
				}
			}
			
		} catch (DSLLoadFailed e) {
			e.printStackTrace();
		}
	}

	private static List<FaultInstanceSetSolution> filterBest(HashMap<FaultInstanceSetSolution, Double> res,
		double highResultProportion) {
		
		int maxCount = (int)Math.ceil(res.size() * highResultProportion);
		
		List<FaultInstanceSetSolution> topFIS = res.entrySet().stream()
				.sorted(Comparator.<Entry<FaultInstanceSetSolution, Double>>comparingDouble(Entry::getValue)
                .reversed())
				.limit(maxCount)
				.map(Entry::getKey)
				.collect(Collectors.toList());
		return topFIS;
	}

	private static List<FaultInstanceSetSolution> filterWorst(HashMap<FaultInstanceSetSolution, Double> res, 
			double lowResultProportion) {
		int maxCount = (int)Math.ceil(res.size() * lowResultProportion);
		
		List<FaultInstanceSetSolution> topFIS = res.entrySet().stream()
				.sorted(Comparator.<Entry<FaultInstanceSetSolution, Double>>comparingDouble(Entry::getValue)
                .reversed())
				.limit(maxCount)
				.map(Entry::getKey)
				.collect(Collectors.toList());
		return topFIS;
	}
}
