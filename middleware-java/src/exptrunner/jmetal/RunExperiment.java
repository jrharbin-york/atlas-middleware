package exptrunner.jmetal;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import java.util.concurrent.TimeUnit;

import atlassharedclasses.FaultInstance;
import exptrunner.ExptHelper;
import faultgen.*;
import atlasdsl.*;

public class RunExperiment {

	private final static String ABS_SCRIPT_PATH = "/home/jharbin/academic/atlas/atlas-middleware/bash-scripts/";
	private final static String ABS_WORKING_PATH = "/home/jharbin/academic/atlas/atlas-middleware/expt-working/";
	public final static String ABS_MIDDLEWARE_PATH = "/home/jharbin/academic/atlas/atlas-middleware/expt-working/";
	private final static String ABS_MOOS_PATH_BASE = "/home/jharbin//academic/atlas/atlas-middleware/middleware-java/moos-sim/";

	private final static boolean CLEAR_MOOS_LOGS_EACH_TIME = true;
	
	// This is an emergency time cutout if the failsafe is not operating normally
	private static double failsafeTimeLimit = 400;

	private static void exptLog(String s) {
		System.out.println(s);
	}

	private static void waitUntilMiddlewareTime(double time, double wallClockTimeOutSeconds) throws FileNotFoundException {
		String pathToFile = ABS_MIDDLEWARE_PATH + "/logs/atlasTime.log";
		String target = Double.toString(time);
		boolean finished = false;
		long timeStart = System.currentTimeMillis();
		
		BufferedReader reader = new BufferedReader(new FileReader(pathToFile));
		try {
			while (!finished) {
				TimeUnit.MILLISECONDS.sleep(100);
				if (((System.currentTimeMillis() - timeStart)/1000) > wallClockTimeOutSeconds) {
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

	public static double doExperiment(Mission mission, String exptTag, List<FaultInstance> testFaultInstances,
			boolean actuallyRun, double timeLimit) throws InterruptedException, IOException {
		Process middleware;

		double returnValue = 0;
		String absATLASJAR;
		String absMOOSPATH;
		Boolean startAllRobots = true;
		String ciRunner;
		
		if (mission.getAllRobots().size() > 2) {
			exptLog("Using 6-robot search case study");
			absATLASJAR = "/home/jharbin/academic/atlas/atlas-middleware/expt-jar/atlas-6robot-case.jar";
			absMOOSPATH = ABS_MOOS_PATH_BASE;
			ciRunner = "run-ci-6robot.sh";
		} else {
			exptLog("Using bo-alpha 2-robot case study");
			absATLASJAR = "/home/jharbin/academic/atlas/atlas-middleware/expt-jar/atlas-bo-alpha-case.jar";
			absMOOSPATH = ABS_MOOS_PATH_BASE + "/bo-alpha-case/";
			startAllRobots = false;
			ciRunner = "run-ci-bo-alpha.sh";
		}

		String faultInstanceFileName = "expt_" + exptTag;
		exptLog("Running experiment with fault instance file " + faultInstanceFileName);
		// Generate a fault instance file for the experiment according to the experiment
		// parameters
		FaultFileCreator ffc = new FaultFileCreator(mission, ABS_WORKING_PATH);

			ffc.writeFaultDefinitionFile(ABS_WORKING_PATH + faultInstanceFileName, testFaultInstances);

			if (actuallyRun) {
				// Launch the MOOS code, middleware and CI as separate subprocesses
				// Always start the robots that are featured in both case studies
				ExptHelper.startScript(absMOOSPATH, "launch_shoreside.sh");
				ExptHelper.startScript(absMOOSPATH, "launch_gilda.sh");
				ExptHelper.startScript(absMOOSPATH, "launch_henry.sh");

				if (startAllRobots) {
					ExptHelper.startScript(absMOOSPATH, "launch_ella.sh");
					ExptHelper.startScript(absMOOSPATH, "launch_frank.sh");
					ExptHelper.startScript(absMOOSPATH, "launch_brian.sh");
					ExptHelper.startScript(absMOOSPATH, "launch_linda.sh");
				}

				exptLog("Started MOOS launch scripts");
				// Sleep until MOOS is ready
				TimeUnit.MILLISECONDS.sleep(400);

				String[] middlewareOpts = { faultInstanceFileName, "nogui" };
				middleware = ExptHelper.startNewJavaProcess("-jar", absATLASJAR, middlewareOpts, ABS_WORKING_PATH);

				// Sleep until the middleware is ready, then start the CI
				TimeUnit.MILLISECONDS.sleep(1000);

				// CI not starting properly as a process, so call it via a script
				exptLog("Starting CI");
				
				ExptHelper.startScript(ABS_MIDDLEWARE_PATH, ciRunner);


				// Wait until the end condition for the middleware
				waitUntilMiddlewareTime(timeLimit, failsafeTimeLimit );
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
			returnValue = extractResults(ABS_WORKING_PATH + "logs");

			if (actuallyRun) {
				exptLog("Waiting to restart experiment");
				// Wait 10 seconds before ending
				TimeUnit.MILLISECONDS.sleep(10000);
			}
			
			return returnValue;
	}

	private static double extractResults(String string) {
		return 0;
	}
}
