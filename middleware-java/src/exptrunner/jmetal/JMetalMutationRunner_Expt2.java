package exptrunner.jmetal;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.uma.jmetal.util.JMetalException;

import atlasdsl.Mission;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import exptrunner.jmetal.test.ATLASEvaluationProblemDummy.*;

public class JMetalMutationRunner_Expt2 {
	
	static int runCount = 1;
	
	public static void expt(String tag, List<Metrics> metrics) throws JMetalException, FileNotFoundException {
		DSLLoader dslloader = new GeneratedDSLLoader();
		Mission mission;
		try {
			mission = dslloader.loadMission();
			for (int i = 0; i < runCount; i++) {
				JMetalMutationRunner.jMetalRun(tag, mission, 
						Optional.empty(), 
						Optional.of(metrics));
			}
		} catch (DSLLoadFailed e) {
			System.out.println("DSL loading failed - configuration problems");
			e.printStackTrace();
		} catch (ExptError e) {
			e.printStackTrace();
		}
	}
	
	public static void expt1() throws JMetalException, FileNotFoundException {
		List<Metrics> metrics = new ArrayList<Metrics>();	
		metrics.add(Metrics.COMBINED_DIST_METRIC);
		metrics.add(Metrics.TIME_PROP);
		expt("expt1", metrics);
	}
	
	public static void expt2() throws JMetalException, FileNotFoundException {
		List<Metrics> metrics = new ArrayList<Metrics>();
		metrics.add(Metrics.AVOIDANCE_METRIC);
		metrics.add(Metrics.NUM_FAULTS);
		expt("expt2", metrics);
	}
	
	public static void expt3() throws JMetalException, FileNotFoundException {
		List<Metrics> metrics = new ArrayList<Metrics>();
		metrics.add(Metrics.DETECTION_COMPLETION_TIME);
		metrics.add(Metrics.NUM_FAULTS);
		expt("expt3", metrics);
	}
	
	public static void main(String[] args) throws JMetalException, FileNotFoundException {
		expt1();
		expt2();
		expt3();
	}
}
