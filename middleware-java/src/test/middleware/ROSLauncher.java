package test.middleware;

import atlasdsl.Mission;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import middleware.core.ROSATLASCore;
import middleware.logging.ATLASLog;

public class ROSLauncher {
	public static void main(String [] args) {
		// On initialisation, read the DSL concrete syntax file and construct the appropriate ATLAS objects here
		DSLLoader dslloader = new GeneratedDSLLoader();
		Mission mission;
		try {
			mission = dslloader.loadMission();
			ROSATLASCore core = new ROSATLASCore(mission);
			System.out.println("ROSATLASCore created");
			// Create the GUI first, since we set its window title from the
			// fault definition file
			if (args.length > 1 && args[1] != null && (args[1].equals("nogui") == false)) {
				System.out.println("args[1] = " + args[1]);
				core.createGUI();
				ATLASLog.logMiddlewareOptions("GUI_SPEC", args[1]);
			}
			
			if (args.length > 0 && args[0] != null && (args[0].equals("nofault") == false)) {
				System.out.println("args[0] = " + args[0]);
				core.setFuzzingDefinitionFile(args[0]);
				ATLASLog.logMiddlewareOptions("FAULT_FILE", args[0]);
			}
			
			core.runMiddleware();
			
		} catch (DSLLoadFailed e) {
			System.out.println("DSL loading failed - configuration problems");
			e.printStackTrace();
		}
	}
}
