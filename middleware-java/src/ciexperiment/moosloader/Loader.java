package ciexperiment.moosloader;

import java.io.IOException;

import atlasdsl.*;
import atlasdsl.loader.*;
import utils.ExptHelperOld;

public class Loader {
	private final static String ABS_MOOS_PATH_BASE = "/home/jharbin//academic/atlas/atlas-middleware/middleware-java/moos-sim/";

	public static void main(String [] args) {

		try {
			DSLLoader dl = new GeneratedDSLLoader();
			Mission mission = dl.loadMission();

			for (Computer c : mission.getAllComputers()) {
				String launchScriptName = "launch_" + c.getName() + ".sh";
				ExptHelperOld.startScript(ABS_MOOS_PATH_BASE, launchScriptName);
			}

			for (Robot r : mission.getAllRobots()) {
				String launchScriptName = "launch_" + r.getName() + ".sh";
				ExptHelperOld.startScript(ABS_MOOS_PATH_BASE, launchScriptName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DSLLoadFailed e) {
			e.printStackTrace();
		}
	}
}
