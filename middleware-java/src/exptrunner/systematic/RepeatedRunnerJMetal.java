package exptrunner.systematic;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.uma.jmetal.util.JMetalException;

import atlasdsl.Mission;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import exptrunner.jmetal.Metrics;
import exptrunner.metrics.MetricsProcessing;

import faultgen.InvalidFaultFormat;

public class RepeatedRunnerJMetal {
	private static double runTime = 1200.0;
	
	public static void runRepeatedFaultSet(List<Metrics> metricList, String faultFileName, String fileTag, int runCount, int faultNum) {
		DSLLoader loader = new GeneratedDSLLoader();
		Mission mission;

		try {
			mission = loader.loadMission();
			String fileName = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
			FileWriter tempLog = new FileWriter("tempLog-" + fileName + ".res");
			MetricsProcessing mp = new MetricsProcessing(mission, metricList, tempLog);
			String resFileName = "repeatedfaults-"+fileTag+".res";
			ExptParams ep = new RepeatSingleExptJMetal(mp, runTime, runCount, mission, faultFileName, faultNum, resFileName);
			// TODO: check this file name
			SystematicRunner.runGeneralExpt(mission, ep, "repeatedfaults.fif", true, runTime);
			System.out.println("Done");
		} catch (DSLLoadFailed e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvalidFaultFormat e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws JMetalException, FileNotFoundException, InterruptedException {
		List<Metrics> l = new ArrayList<Metrics>();
		l.add(Metrics.PURE_MISSED_DETECTIONS);
		l.add(Metrics.TIME_TOTAL_ABSOLUTE);
		
		int runCount = 40;
		String fileName = "/home/atlas/atlas/atlas-middleware/bash-scripts/jmetal-expts/res-keep/2020-12-12-6robot/VARpf-gen-expt1-13.csv";
		
		// This refers to the numbers in the PF file list
		List<Integer> nums = new ArrayList<Integer>();
		nums.add(0);
		nums.add(1);
		nums.add(2);
		nums.add(3);
		
		for (Integer i : nums) {
			runRepeatedFaultSet(l, fileName, "6robot-line" + Integer.toString(i) + ".fif", runCount, i);
		}
	}
}
