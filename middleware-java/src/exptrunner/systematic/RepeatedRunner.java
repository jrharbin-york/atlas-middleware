package exptrunner.systematic;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.uma.jmetal.util.JMetalException;

import atlasdsl.Mission;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import exptrunner.jmetal.Metrics;
import exptrunner.metrics.MetricsProcessing;

public class RepeatedRunner {
	private static double runTime = 1200.0;
	
	public static void runRepeatedFaultSet(List<Metrics> metricList, String faultFileName, String fileTag, int runCount) {
		DSLLoader loader = new GeneratedDSLLoader();
		Mission mission;

		try {
			mission = loader.loadMission();
			String fileName = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
			FileWriter tempLog = new FileWriter("tempLog-" + fileName + ".res");
			MetricsProcessing mp = new MetricsProcessing(mission, metricList, tempLog);
			String resFileName = "repeatedfaults-" + fileTag + ".res";
			ExptParams ep = new RepeatSingleExpt(mp, runTime, runCount, mission, faultFileName, fileTag);
			// TODO: check this file name
			SystematicRunner.runGeneralExpt(mission, ep, resFileName, true, runTime);
			System.out.println("Done");
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
		runRepeatedFaultSet(l, "/home/atlas/atlas/atlas-middleware/bash-scripts/jmetal-expts/res-keep/test-fif/11.fif", "11", 60);
		runRepeatedFaultSet(l, "/home/atlas/atlas/atlas-middleware/bash-scripts/jmetal-expts/res-keep/test-fif/22.fif", "22", 60);
	}
}
