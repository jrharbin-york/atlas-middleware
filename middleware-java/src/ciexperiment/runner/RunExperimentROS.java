package ciexperiment.runner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import utils.ExptHelper;
import utils.ExptHelperOld;

public class RunExperimentROS {

	// TODO: no more fixed paths
	private final static String ABS_SCRIPT_PATH = "/home/jharbin/academic/atlas/atlas-middleware/bash-scripts/";
	private final static String ABS_WORKING_PATH = "/home/jharbin/academic/atlas/atlas-middleware/expt-working/healthcare";
	public final static String ABS_MIDDLEWARE_PATH = ABS_WORKING_PATH;
	private final static String ABS_MOOS_PATH_BASE = "/home/jharbin//academic/atlas/atlas-middleware/middleware-java/moos-sim/";

	private final static boolean CLEAR_MOOS_LOGS_EACH_TIME = true;
	private static final boolean CLEAR_ROS_LOGS_EACH_TIME = false;

	// This is an emergency time cutout if the failsafe is not operating normally
	private static double failsafeTimeLimit = 1000;

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
	
	public static void sleepHandlingInterruption(long timeMillisecs) {
		try {
			TimeUnit.MILLISECONDS.sleep(timeMillisecs);
		} catch (InterruptedException e) {
			System.out.println("Cancelling sleep after interruption");
		}
	}

	public static String doExperimentFromFile(String exptTag, boolean actuallyRun, double timeLimit, String ciClass) throws InterruptedException, IOException {
		
		if (actuallyRun) {	
			exptLog("Starting ROS/SAFEMUV launch scripts");
			String launchBashScript = "./auto_launch_healthcare.sh";
			ExptHelperOld.startScript(ABS_WORKING_PATH, launchBashScript);
			
			sleepHandlingInterruption(10000);
			
			System.out.println("Running middleware");
			ExptHelper.runScriptNew(ABS_WORKING_PATH, "./start_middleware.sh", "nofault");
			String[] middlewareOpts = { "nofault", "nogui" };

			// This assumes that the mission time is at least 30 seconds, and gives time for
			// the middleware to start
			sleepHandlingInterruption(5000);
			ExptHelper.runScriptNew(ABS_WORKING_PATH, "./start_ci.sh", ciClass);
			sleepHandlingInterruption(1000);
			
			// Wait until the end condition for the middleware
			waitUntilMiddlewareTime(timeLimit, failsafeTimeLimit);
			exptLog("Middleware end time reached");
			
			// TODO: ensure simulation/SAFEMUV state is properly cleared
			if (CLEAR_ROS_LOGS_EACH_TIME) {
				ExptHelperOld.startCmd(ABS_WORKING_PATH, "terminate_clear_logs.sh");
			} else {
				ExptHelperOld.startCmd(ABS_WORKING_PATH, "terminate.sh");
			}
			exptLog("Kill MOOS / Java processes command sent");
			sleepHandlingInterruption(10000);
			exptLog("Destroy commands completed");
		}

		if (actuallyRun) {
			exptLog("Waiting to restart experiment");
			// Wait 10 seconds before ending
			try {
				TimeUnit.MILLISECONDS.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
