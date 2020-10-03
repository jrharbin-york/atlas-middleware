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

	public static double doExperiment(Mission mission, String exptTag, List<FaultInstance> testFaultInstances,
			boolean actuallyRun, double timeLimit) throws InterruptedException, IOException {
		Process middleware;

		double returnValue = 0;

		String faultInstanceFileName = "expt_" + exptTag;
		exptLog("Running experiment with fault instance file " + faultInstanceFileName);
		// Generate a fault instance file for the experiment according to the experiment
		// parameters
		FaultFileCreator ffc = new FaultFileCreator(mission, ABS_WORKING_PATH);

			ffc.writeFaultDefinitionFile(ABS_WORKING_PATH + faultInstanceFileName, testFaultInstances);

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
				waitUntilMiddlewareTime(timeLimit);
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
		// TODO Read and process the numerical results from the given log directory
		return 0;
	}
}
