package ciexperiment.runner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import utils.ExptHelperOld;

public class RunExperiment {

	// TODO: no more fixed paths
	private final static String ABS_SCRIPT_PATH = "/home/jharbin/academic/atlas/atlas-middleware/bash-scripts/";
	private final static String ABS_WORKING_PATH = "/home/jharbin/academic/atlas/atlas-middleware/expt-working/";
	public final static String ABS_MIDDLEWARE_PATH = "/home/jharbin/academic/atlas/atlas-middleware/expt-working/";
	private final static String ABS_MOOS_PATH_BASE = "/home/jharbin//academic/atlas/atlas-middleware/middleware-java/moos-sim/";

	private final static boolean CLEAR_MOOS_LOGS_EACH_TIME = true;

	// This is an emergency time cutout if the failsafe is not operating normally
	private static double failsafeTimeLimit = 300;

	private static void exptLog(String s) {
		System.out.println(s);
	}

	private static void waitUntilMiddlewareTime(double time, double wallClockTimeOutSeconds)
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

	public static String doExperimentFromFile(String exptTag, boolean actuallyRun, double timeLimit, String ciClass)
			throws InterruptedException, IOException {
		Process middleware;

		String absATLASJAR;
		String absMOOSPATH;
		String ciRunner;

		exptLog("Using custom generated mission");
		absATLASJAR = "/home/jharbin/academic/atlas/atlas-middleware/expt-jar/atlas-6robot-case.jar";
		absMOOSPATH = ABS_MOOS_PATH_BASE;
		ciRunner = "run-ci-6robot.sh";

		if (actuallyRun) {
			exptLog("Generating MOOS code");
			ExptHelperOld.startScript(ABS_WORKING_PATH, "build_moos_files.sh");
			// Add a wait until it it ready
			Thread.sleep(3000);
			exptLog("Starting MOOS launch scripts");
			ExptHelperOld.startScript(ABS_WORKING_PATH, "launch_moos_sim.sh");
			// Sleep until MOOS is ready
			TimeUnit.MILLISECONDS.sleep(1000);

			String[] middlewareOpts = { "nofault", "nogui" };

			ExptHelperOld.startScript(ABS_WORKING_PATH, "start_middleware.sh");
			// middleware = ExptHelper.startNewJavaProcess("-jar", absATLASJAR,
			// middlewareOpts, ABS_WORKING_PATH);

			// Sleep until the middleware is ready, then start the CI
			TimeUnit.MILLISECONDS.sleep(1000);

			// CI not starting properly as a process, so call it via a script
			exptLog("Starting CI");
			ExptHelperOld.startScript(ABS_WORKING_PATH, "start_ci.sh " + ciClass);

			TimeUnit.MILLISECONDS.sleep(3000);
			// Wait until the end condition for the middleware
			waitUntilMiddlewareTime(timeLimit, failsafeTimeLimit);
			exptLog("Middleware end time reached");

			if (CLEAR_MOOS_LOGS_EACH_TIME) {
				ExptHelperOld.startCmd(ABS_SCRIPT_PATH, "terminate_clear_logs.sh");
			} else {
				ExptHelperOld.startCmd(ABS_SCRIPT_PATH, "terminate.sh");
			}

			exptLog("Kill MOOS / Java processes command sent");
			exptLog("Destroy commands completed");
		}

		if (actuallyRun) {
			exptLog("Waiting to restart experiment");
			// Wait 10 seconds before ending
			TimeUnit.MILLISECONDS.sleep(10000);
		}

		return ABS_WORKING_PATH;
	}

	private static double extractResults(String string) {
		return 0;
	}

	public static void compileLoader() throws IOException {
		ExptHelperOld.startScript(ABS_SCRIPT_PATH, "compile_dsl_loader.sh");
	}
}
