package fuzzexperiment.runner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import atlasdsl.Mission;
import carsspecific.ros.codegen.ROSCodeGen;
import fuzzingengine.FuzzingEngine;
import fuzzingengine.spec.GeneratedFuzzingSpec;
import middleware.atlascarsgenerator.ConversionFailed;
import utils.ExptHelper;

public class StartFuzzingProcesses {

	// TODO: no more fixed paths
	//private String ABS_SCRIPT_PATH = "/home/jharbin/academic/atlas/atlas-middleware/bash-scripts/";
	//private String ABS_WORKING_PATH = "/home/jharbin/academic/atlas/atlas-middleware/expt-working/ros/";
	//public String ABS_MIDDLEWARE_PATH = "/home/jharbin/academic/atlas/atlas-middleware/expt-working/ros/";
	private String ABS_SCRIPT_PATH;
	private String ABS_WORKING_PATH;
	public String ABS_MIDDLEWARE_PATH;

	private final static boolean CLEAR_ROS_LOGS_EACH_TIME = true;

	// This is an emergency time cutout if the failsafe is not operating normally
	private static double failsafeTimeLimit = 1000;

	private static void exptLog(String s) {
		System.out.println(s);
	}
	
	public StartFuzzingProcesses(String bashScriptPath, String workingPath, String middlewarePath) {
		ABS_SCRIPT_PATH = bashScriptPath;
		ABS_WORKING_PATH = workingPath;
		ABS_MIDDLEWARE_PATH = middlewarePath;
	}

	private void waitUntilMiddlewareTime(double time, double wallClockTimeOutSeconds)
			throws FileNotFoundException {
		String pathToFile = ABS_MIDDLEWARE_PATH + "/logs/atlasTime.log";
		String target = Double.toString(time);
		boolean finished = false;
		long timeStart = System.currentTimeMillis();

		BufferedReader reader = new BufferedReader(new FileReader(pathToFile));
		try {
			while (!finished) {
				TimeUnit.MILLISECONDS.sleep(100);
				if (((System.currentTimeMillis() - timeStart) / 1000) > wallClockTimeOutSeconds) {
					finished = true;
				}
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
	
	public void waitWallClockTime(double waitTimeSeconds) throws InterruptedException {
		long timeMillis = (long)(waitTimeSeconds * 1000.0);
		System.out.println("Waiting for " + timeMillis + " ms");
		Thread.sleep(timeMillis);
	}
	
	public void codeGenerationROSFuzzing(Mission mission, String filename) {
		FuzzingEngine fe = GeneratedFuzzingSpec.createFuzzingEngine(mission, false);
		try {
			ROSCodeGen rgen = new ROSCodeGen(mission, Optional.of(fe));
			// Load the CSV file to produce fuzzing key selection records
			fe.setupFromFuzzingFile(filename, mission);
			rgen.convertDSL();
			System.out.println("Code generation completed");
		} catch (ConversionFailed cf) {
			System.out.println("ERROR: DSL conversion to MOOS representation failed: reason " + cf.getReason());
		}
	}
	
	public double doExperimentFromFile(String exptTag, boolean actuallyRun, double timeLimit, String fuzzFilePath)
			throws InterruptedException, IOException {
		Process middleware;

		double returnValue = 0;
		
		if (actuallyRun) {	
			exptLog("Starting ROS/SAFEMUV launch scripts"); 
			ExptHelper.startScript(ABS_WORKING_PATH, "auto_launch_safemuv.sh");
			// TODO: check custom delays - Sleep until MOOS is ready
			TimeUnit.MILLISECONDS.sleep(20000);
			ExptHelper.runScriptNew(ABS_WORKING_PATH, "./start_middleware.sh", fuzzFilePath);

			String[] middlewareOpts = { "nofault", "nogui" };

			// Wait until the end condition for the middleware
			waitUntilMiddlewareTime(timeLimit, failsafeTimeLimit);
			//waitWallClockTime(timeLimit);
			exptLog("Middleware end time reached");

			// TODO: ensure simulation/SAFEMUV state is properly cleared
			if (CLEAR_ROS_LOGS_EACH_TIME) {
				ExptHelper.startCmd(ABS_WORKING_PATH, "terminate_clear_logs.sh");
			} else {
				ExptHelper.startCmd(ABS_WORKING_PATH, "terminate.sh");
			}
			exptLog("Kill MOOS / Java processes command sent");
			exptLog("Destroy commands completed");
		}

		returnValue = 0;

		if (actuallyRun) {
			exptLog("Waiting to restart experiment");
			// Wait 10 seconds before ending
			TimeUnit.MILLISECONDS.sleep(10000);
		}

		return returnValue;
	}

	public void compileLoader() throws IOException {
		ExptHelper.startScript(ABS_SCRIPT_PATH, "compile_dsl_loader.sh");
	}

	public void cleanRun(Mission baseMission, String file) throws IOException {
		// Originally call Argentina's launch script here... for now, just call
		// the full script
		ExptHelper.startCmd(ABS_WORKING_PATH, "temp_clean_config_files.sh");
	}
}
