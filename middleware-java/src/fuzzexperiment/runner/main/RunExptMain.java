package fuzzexperiment.runner.main;

import java.io.IOException;

import atlasdsl.Mission;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.GeneratedDSLLoader;
import fuzzexperiment.runner.*;
import fuzzexperiment.runner.metrics.*;

public class RunExptMain {
	
	public static void main(String [] args) {
		String resFileName = "fuzzexpt-set-of-solutions.res";
		String csvFile = args[0];
		System.out.println("Running on file: " + csvFile);
		
		try {
			Mission mission = new GeneratedDSLLoader().loadMission();
			MetricHandler mh = new MetricHandler(mission, resFileName);
			ExptParams ep = new RunRepeated(csvFile, 12);
			FuzzExptRunner r;
			r = new FuzzExptRunner(ep, mh);
			r.run();
		} catch (DSLLoadFailed | IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
