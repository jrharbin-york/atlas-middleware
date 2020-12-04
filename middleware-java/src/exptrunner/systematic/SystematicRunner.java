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
import atlasdsl.Robot;
import atlasdsl.faults.Fault;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import atlassharedclasses.FaultInstance;
import exptrunner.jmetal.Metrics;
import exptrunner.jmetal.RunExperiment;
import exptrunner.jmetal.obsolete.SingleFaultCoverageExpt;
import exptrunner.metrics.MetricsProcessing;

public class SystematicRunner {
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
	
	public static void runCoverage(List<Metrics> metricList, Mission mission, String faultName, double additionalDataVal) {
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
				runGeneralExpt(mission, ep, faultName + "_coverage", true, runTime);
				System.out.println("Done");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws JMetalException, FileNotFoundException {
		DSLLoader loader = new GeneratedDSLLoader();
		try {
			Mission mission = loader.loadMission();
			List<Metrics> l = new ArrayList<Metrics>();
			l.add(Metrics.OBSTACLE_AVOIDANCE_METRIC);
			l.add(Metrics.TIME_TOTAL_ABSOLUTE);
			// TODO: add the additional metrics here
		
			List<Robot> vehicles = mission.getAllRobots();
		
			double headingStep=30.0;
			
			List<Double> speeds = new ArrayList<Double>();
			speeds.add(2.0);
			speeds.add(3.0);
			speeds.add(4.0);
			speeds.add(5.0);
		
			for (Robot vehicle : vehicles) {
				for (double heading = 0; heading < 360.0; heading+=headingStep) {
					String vname = vehicle.getName().toUpperCase();
					System.out.println("Running heading experiments for " + vname + " - heading=" + heading);
					runCoverage(l, mission, "HEADINGFAULT-" + vname, heading);
				}
			}
			
			for (Robot vehicle : vehicles) {
				for (double speed : speeds) {
					String vname = vehicle.getName().toUpperCase();
					System.out.println("Running speed experiments for " + vname + " - speed=" + speed);
					runCoverage(l, mission, "SPEEDFAULT-" + vname, speed);
				}
			}
			
		} catch (DSLLoadFailed e) {
			e.printStackTrace();
		}
	}
}
