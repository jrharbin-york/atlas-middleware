package exptrunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import atlasdsl.loader.*;
import atlassharedclasses.FaultInstance;
import faultgen.*;
import atlasdsl.*;
import atlasdsl.faults.Fault;
import atlasdsl.faults.MotionFault;

public class RunExperiment {

	private final static String ABS_SCRIPT_PATH = "/home/jharbin/academic/atlas/atlas-middleware/bash-scripts/";
	private final static String ABS_WORKING_PATH = "/home/jharbin/academic/atlas/atlas-middleware/expt-working/";
	private final static String ABS_MIDDLEWARE_PATH = "/home/jharbin/academic/atlas/atlas-middleware/expt-working/";
	private final static String ABS_MOOS_PATH = "/home/jharbin//academic/atlas/atlas-middleware/middleware-java/moos-sim/";

	private final static String ABS_ATLAS_JAR = "/home/jharbin/academic/atlas/atlas-middleware/expt-jar/atlas.jar";
	
	private final static boolean CLEAR_MOOS_LOGS_EACH_TIME = true;

	private static void exptLog(String s) {
		System.out.println(s);
	}

	private static void waitUntilMiddlewareTime(double time) throws FileNotFoundException {
		String pathToFile = ABS_MIDDLEWARE_PATH + "/logs/atlasTime.log";
		String target = Double.toString(time);
		boolean finished = false;
		BufferedReader reader = new BufferedReader(new FileReader(pathToFile));
		try {
			while (!finished) {
				TimeUnit.MILLISECONDS.sleep(100);
				while (reader.ready()) {
					String line = reader.readLine();
					Double lineVal = Double.valueOf(line);
					exptLog(line + "-" + target);
					if (lineVal >= time) {
						finished = true;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void doExperiment(Mission mission, String exptTag, ExptParams eparams, boolean actuallyRun) {
		Process middleware;

		int faultInstanceFileNum = 0;

		while (!eparams.completed()) {
			eparams.printState();
			// TODO: generate fresh MOOS code if required - need to specify the MOOSTimeWarp
			// here?

			String faultInstanceFileName = "expt_" + exptTag + Integer.toString(faultInstanceFileNum);
			exptLog("Running experiment with fault instance file " + faultInstanceFileName);
			// Generate a fault instance file for the experiment according to the experiment
			// parameters 
			FaultFileCreator ffc = new FaultFileCreator(mission, ABS_WORKING_PATH);
			List<FaultInstance> outputFaultInstances = eparams.specificFaults();

			try {
				ffc.writeFaultDefinitionFile(ABS_WORKING_PATH + faultInstanceFileName, outputFaultInstances);

				if (actuallyRun) {
				// Launch the MOOS code, middleware and CI as separate subprocesses

				// TODO: if launching an experiment with more robots, need to ensure individual
				// launch scripts are generated in the MOOS code
				ExptHelper.startScript(ABS_MOOS_PATH, "launch_shoreside.sh");
				ExptHelper.startScript(ABS_MOOS_PATH, "launch_ella.sh");
				ExptHelper.startScript(ABS_MOOS_PATH, "launch_frank.sh");
				ExptHelper.startScript(ABS_MOOS_PATH, "launch_gilda.sh");
				ExptHelper.startScript(ABS_MOOS_PATH, "launch_brian.sh");
				ExptHelper.startScript(ABS_MOOS_PATH, "launch_linda.sh");
				ExptHelper.startScript(ABS_MOOS_PATH, "launch_henry.sh");

				exptLog("Started MOOS launch scripts");
				// Sleep until MOOS is ready
				TimeUnit.MILLISECONDS.sleep(400);

				String[] middlewareOpts = { faultInstanceFileName, "nogui" };
				middleware = ExptHelper.startNewJavaProcess("-jar", ABS_ATLAS_JAR, middlewareOpts, ABS_WORKING_PATH);

				// Sleep until the middleware is ready, then start the CI
				TimeUnit.MILLISECONDS.sleep(1000);

				// CI not starting properly as a process, call it via a script
				exptLog("Starting CI");
				// TODO: check CI - fix absolute paths when working
				ExptHelper.startScript(ABS_MIDDLEWARE_PATH, "run-ci.sh");

				// Wait until the end condition for the middleware
				waitUntilMiddlewareTime(eparams.getTimeLimit());
				exptLog("Middleware end time reached");
				exptLog("Destroying middleware processes");
				middleware.destroy();
				
				if (CLEAR_MOOS_LOGS_EACH_TIME) {
					ExptHelper.startCmd(ABS_SCRIPT_PATH, "terminate_clear_logs.sh");
				} else {
					ExptHelper.startCmd(ABS_SCRIPT_PATH, "terminate.sh");
				}
				
				exptLog("Kill MOOS / Java processes command sent");
				exptLog("Destroy commands completed");
				
				}
				// Read and process the result files from the experiment
				eparams.logResults(ABS_WORKING_PATH + "logs");

				// Modify the experiment spec data (either by using the results for mutation,
				// or according to predefined template)
				eparams.advance();
				faultInstanceFileNum++;
				
				if (actuallyRun) {
					exptLog("Waiting to restart experiment");
					// Wait 10 seconds before ending
					TimeUnit.MILLISECONDS.sleep(10000);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void runCoverage(String[] args) {
		// Read args to launch appropriate experiment
		String faultName = "SPEEDFAULT-ELLA";
		if (args.length > 0 && args[0] != null) {
			faultName = args[0];
		}

		DSLLoader loader = new GeneratedDSLLoader();
		Mission mission;
		try {
			mission = loader.loadMission();
			Optional<Fault> f_o = mission.lookupFaultByName(faultName);
			if (f_o.isPresent()) {
				String resFileName = faultName;
				Fault f = f_o.get();
				Optional<String> speedOverride_o = Optional.empty();

				// hack to change the fault speed
				if (f.getImpact() instanceof MotionFault) {
					if (args.length > 1 && args[1] != null) {
						String speedOverride_s = args[1];
						speedOverride_o = Optional.of(speedOverride_s);
						//double speedOverride = Double.valueOf(speedOverride_s);
						//MotionFault mfi = (MotionFault) f.getImpact();
						resFileName = resFileName + speedOverride_s;
						//System.out.println("Experiment overriding speed to " + speedOverride);
//						mfi._overrideSpeed(speedOverride);

						// test
//						MotionFault mfi2 = (MotionFault) f.getImpact();
						//System.out.println("test new value = " + mfi2.getNewValue());
					}
				}
				resFileName = resFileName + "_goalDiscovery.res";

				ExptParams ep = new SingleFaultCoverageExpt(resFileName, 1200.0, 0.0, 1200.0, 1200.0, 50.0, 0.5, f,
						speedOverride_o);
				doExperiment(mission, faultName + "_coverage", ep, true);
				exptLog("Done");
			}
		} catch (DSLLoadFailed e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runMultifaultExpt(String[] args) {
		DSLLoader loader = new GeneratedDSLLoader();
		Mission mission;
		try {
			mission = loader.loadMission();
			String tempFileName = "tempFaultFile.fif";
			String resFileName = "multifault.res";
			int repeatsCount = 30;
			ExptParams ep = new RandomFaultConfigs(tempFileName, 1200.0, resFileName, repeatsCount, mission);
			doExperiment(mission, "multifault", ep,true);
			exptLog("Done");
		} catch (DSLLoadFailed e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void runMutation(String[] args) {
		DSLLoader loader = new GeneratedDSLLoader();
		Mission mission;
		try {
			mission = loader.loadMission();
			String evolveFileName = "evolveProgress.log";
			String mutationFilename = "mutations.log";
			String popFileName = "population.log";
			
			double runtime = 1200.0;
			// TODO: when experiments are stable, add the seed 
			int seed = 6453232;
			int iterations = 10;
			
			ExptParams ep = new FaultMutation(null, evolveFileName, mutationFilename, popFileName, runtime, seed, iterations, mission);
			doExperiment(mission, "multifault", ep,true);
			exptLog("Done");
		} catch (DSLLoadFailed e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void testEvolveProcess() {
		try {
			DSLLoader loader = new GeneratedDSLLoader();
			Mission mission = loader.loadMission();
			Optional<Fault> f_o = mission.lookupFaultByName("SPEEDFAULT-ELLA");
			if (f_o.isPresent()) {
				Random seedGen = new Random();
				String tempFileName = "evolve.log";
				String resFileName = "multifault.res";
				//int repeatsCount = 30;
				double runtime = 1200.0;
				int seed = seedGen.nextInt();
				System.out.println("seed=" + seed);
				int iterations = 20;
				
				//Fault f = f_o.get();
				
				LoggingOperation logOp = (ep, logFileDir) -> {
					int runNoInPopulation = ep.getRunNoInPopulation();
					FaultInstanceSet currentFS = ep.getPop().get(runNoInPopulation);
					ResultInfo ri = new ResultInfo();
					int missedDetections = 0;
					
					// Count operation
					if (currentFS._hasFaultInRange(800.0,300.0, "SPEEDFAULT-ELLA")) {
						missedDetections++;
					}
					
					// Count operation
					if (currentFS._hasFaultInRange(500.0,300.0, "HEADINGFAULT-FRANK")) {
						missedDetections++;
					}
					
					ri.setField("missedDetections", missedDetections);
					ep.getPopResults().put(currentFS, ri);
					// storedResults maintains a previous results cache
					// ensure it is used rather than re-evaluating!
					ep.getStoredResults().put(currentFS, ri);
					
				};
				
				FaultMutation ep = new FaultMutation(logOp, tempFileName, "mutation.log", "pop.log", runtime, seed, iterations, mission);
				doExperiment(mission, "evolvefault", ep, false);
			}
		} catch (DSLLoadFailed e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO: something more intelligent to select all these
		//runMultifaultExpt(args);
		//runCoverage(args);
		runMutation(args);
//		testEvolveProcess();
	}
}
