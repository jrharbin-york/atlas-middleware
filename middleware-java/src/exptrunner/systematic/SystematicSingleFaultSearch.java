package exptrunner.systematic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.uma.jmetal.util.JMetalException;

import atlasdsl.Mission;
import atlasdsl.faults.Fault;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import atlassharedclasses.FaultInstance;

import exptrunner.jmetal.RunExperiment;

public class SystematicSingleFaultSearch {
	
	private static double runTime = 1200.0;
	
	private static void runGeneralExpt(Mission mission, ExptParams eparams, String exptTag, boolean actuallyRun, double timeLimit) throws InterruptedException, IOException {
		while (!eparams.completed()) {
			eparams.printState();
			List<FaultInstance> fis = eparams.specificFaults();
			RunExperiment.doExperiment(mission, exptTag, fis, actuallyRun, timeLimit);
			eparams.advance();
		}
	}
	
	public static void runRepeatedFaultSet(String faultFileName, int runCount) {
		DSLLoader loader = new GeneratedDSLLoader();
		Mission mission;		

		try {
			mission = loader.loadMission();
			String resFileName = "repeatedfaults.res";
			ExptParams ep = new RepeatSingleExpt(runTime, runCount, mission, faultFileName);
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
				resFileName = resFileName + "coverage.res";

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
		runRepeatedFaultSet("/tmp/testfile.fif", 10);
	}
}

