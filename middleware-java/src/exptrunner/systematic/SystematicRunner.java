package exptrunner.systematic;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.uma.jmetal.util.JMetalException;

import atlasdsl.Mission;
import atlasdsl.faults.Fault;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import atlassharedclasses.FaultInstance;
import exptrunner.jmetal.Metrics;
import exptrunner.jmetal.RunExperiment;
import exptrunner.metrics.MetricsProcessing;

public class SystematicRunner {
	
	private static double runTime = 1200.0;
	
	
	private static void runGeneralExpt(Mission mission, ExptParams eparams, String exptTag, boolean actuallyRun, double timeLimit) throws InterruptedException, IOException {
		while (!eparams.completed()) {
			eparams.printState();
			List<FaultInstance> fis = eparams.specificFaults();
			RunExperiment.doExperiment(mission, exptTag, fis, actuallyRun, timeLimit);
			eparams.logResults("/home/jharbin/academic/atlas/atlas-middleware/expt-working/logs");
			eparams.advance();
		}
	}
	
	public static void runRepeatedFaultSet(List<Metrics> metricList, String faultFileName, int runCount) {
		DSLLoader loader = new GeneratedDSLLoader();
		Mission mission;

		try {
			mission = loader.loadMission();
			String fileName = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
			FileWriter tempLog = new FileWriter("tempLog-" + fileName + ".res");
			MetricsProcessing mp = new MetricsProcessing(mission, metricList, tempLog);
			String resFileName = "repeatedfaults.res";
			ExptParams ep = new RepeatSingleExpt(mp, runTime, runCount, mission, faultFileName);
			// TODO: check this file name
			runGeneralExpt(mission, ep, "repeatedfaults", true, runTime);
			System.out.println("Done");
		} catch (DSLLoadFailed e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void runCoverage(String faultName) {
		DSLLoader loader = new GeneratedDSLLoader();
		Mission mission;
		try {
			mission = loader.loadMission();
			Optional<Fault> f_o = mission.lookupFaultByName(faultName);
			if (f_o.isPresent()) {
				String resFileName = faultName;
				Fault f = f_o.get();
				Optional<String> speedOverride_o = Optional.empty();
				resFileName = resFileName + "_coverage.res";

				ExptParams ep = new SingleFaultCoverageExpt(resFileName, 1200.0, 0.0, 1200.0, 1200.0, 50.0, 0.5, f,
						speedOverride_o);
				runGeneralExpt(mission, ep, faultName + "_coverage", true, 1200.0);
				System.out.println("Done");
			}
		} catch (DSLLoadFailed e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws JMetalException, FileNotFoundException {
		List<Metrics> l = new ArrayList<Metrics>();
		l.add(Metrics.OBSTACLE_AVOIDANCE_METRIC);
		l.add(Metrics.TIME_TOTAL_ABSOLUTE);
		//runRepeatedFaultSet(l, "/home/atlas/atlas/atlas-middleware/expt-working/test-repeated-faults.fif", 20);
		runCoverage("HEADINGFAULT-HENRY");
	}

}
