package exptrunner.jmetal.testrunners;

import java.io.FileNotFoundException;
import java.util.Optional;

import org.uma.jmetal.util.JMetalException;

import atlasdsl.Mission;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import exptrunner.jmetal.ExptError;
import exptrunner.jmetal.JMetalMutationRunner;
import exptrunner.jmetal.test.ATLASEvaluationProblemDummy.*;

public class JMetalMutationDummyTest_IncreasingMetricValues {
	
	static int runCount = 1;
	
	public static void main(String[] args) throws JMetalException, FileNotFoundException {
		DSLLoader dslloader = new GeneratedDSLLoader();
		Mission mission;
		try {
			mission = dslloader.loadMission();
			for (int i = 0; i < runCount; i++) {
				JMetalMutationRunner.jMetalRun("", mission, 
						Optional.of(EvaluationProblemDummyChoices.EXPT_RUNNER_INCREASING_CUSTOM_FAULTS), 
						Optional.empty());
			}
		} catch (DSLLoadFailed e) {
			System.out.println("DSL loading failed - configuration problems");
			e.printStackTrace();
		} catch (ExptError e) {
			e.printStackTrace();
		}
	}
}
