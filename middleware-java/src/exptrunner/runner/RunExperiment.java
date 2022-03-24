package exptrunner.runner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import exptrunner.jmetal.FuzzingSelectionsSolution;
import utils.ExptHelperOld;
import atlasdsl.*;

public class RunExperiment {

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

	public static double doExperimentFromFile(Mission mission, String exptTag, String fileName,
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

		exptLog("Running experiment with fuzzing CSV file " + fileName);

		if (actuallyRun) {
				// Launch the MOOS code, middleware and CI as separate subprocesses
				// Always start the robots that are featured in both case studies
				ExptHelperOld.startScript(absMOOSPATH, "launch_shoreside.sh");
				ExptHelperOld.startScript(absMOOSPATH, "launch_gilda.sh");
				ExptHelperOld.startScript(absMOOSPATH, "launch_henry.sh");

				if (startAllRobots) {
					ExptHelperOld.startScript(absMOOSPATH, "launch_ella.sh");
					ExptHelperOld.startScript(absMOOSPATH, "launch_frank.sh");
					ExptHelperOld.startScript(absMOOSPATH, "launch_brian.sh");
					ExptHelperOld.startScript(absMOOSPATH, "launch_linda.sh");
				}

				exptLog("Started MOOS launch scripts");
				// Sleep until MOOS is ready
				TimeUnit.MILLISECONDS.sleep(800);

				String[] middlewareOpts = { "nofault", "nogui" };
				middleware = ExptHelperOld.startNewJavaProcess("-jar", absATLASJAR, middlewareOpts, ABS_WORKING_PATH);

				// Sleep until the middleware is ready, then start the CI
				TimeUnit.MILLISECONDS.sleep(1000);

				// CI not starting properly as a process, so call it via a script
				exptLog("Starting CI");
				ExptHelperOld.startScript(ABS_MIDDLEWARE_PATH, ciRunner);
			
				TimeUnit.MILLISECONDS.sleep(3000);
				// Wait until the end condition for the middleware
				waitUntilMiddlewareTime(timeLimit, failsafeTimeLimit );
				exptLog("Middleware end time reached");
				exptLog("Destroying middleware processes");
				middleware.destroy();

				if (CLEAR_MOOS_LOGS_EACH_TIME) {
					ExptHelperOld.startCmd(ABS_SCRIPT_PATH, "terminate_clear_logs.sh");
				} else {
					ExptHelperOld.startCmd(ABS_SCRIPT_PATH, "terminate.sh");
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

	public static void doExperiment(Mission mission, String exptTag, FuzzingSelectionsSolution sol,
			boolean actuallyRun, double timeLimit) throws IOException, InterruptedException {
		String faultInstanceFileName = "expt_" + exptTag;
		exptLog("Running experiment with generated fuzzing CSV file " + faultInstanceFileName);
		String csvFile = ABS_WORKING_PATH + faultInstanceFileName;
		sol.generateCSVFile(ABS_WORKING_PATH + faultInstanceFileName);
		doExperimentFromFile(mission, exptTag, csvFile, actuallyRun, timeLimit);
	}
}
