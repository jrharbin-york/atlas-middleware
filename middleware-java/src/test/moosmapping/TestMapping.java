package test.moosmapping;

import atlascarsgenerator.ConversionFailed;
import atlascarsgenerator.MOOSCodeGen;

import atlascollectiveintgenerator.*;

import atlasdsl.*;
import atlasdsl.loader.*;
import carsmapping.CARSSimulation;
import carsspecific.moos.moosmapping.*;

public class TestMapping {
	public static void testCodeGeneration1(String code_dir) {
		MOOSSimulation testSim = new MOOSSimulation();

		MOOSCommunity gilda = new MOOSCommunity(testSim, "gilda");
		MOOSProcess ivpHelm = new MOOSProcess("pIvpHelm", gilda);
		testSim.addCommunity(gilda);
		gilda.addProcess(ivpHelm);
		
		// Add subclasses of behaviour etc with the custom
		// properties added? (for different types of behaviour)
		
		MOOSBehaviour bhv_waypoint = new MOOSBehaviour("BHV_WAYPOINT", ivpHelm);
		bhv_waypoint.setProperty("name", "waypt_return");
		ivpHelm.addBehaviour(bhv_waypoint);
		testSim.generateCARSInterface(code_dir);
	}
	
	// Test function that represents a robot added with a sonar sensor
	public static void testCodeGeneration2(String code_dir) throws DSLLoadFailed {
		DSLLoader dslloader = new GeneratedDSLLoader();
		Mission mission = dslloader.loadMission();
		
		MOOSCodeGen gen = new MOOSCodeGen(mission);
		CollectiveIntGen javaCI = new JavaCollectiveIntGen(mission);
		
		System.out.println("Converting DSL to MOOS representation...");
		try {
			CARSSimulation moossim = gen.convertDSL(mission);
			System.out.println("DSL conversion completed");
			moossim.generateCARSInterface(code_dir);
			System.out.println("Code generation completed");
			
			//javaCI.generateCollectiveIntFiles("collectiveint-gen", CollectiveIntGenTypes.ALL_ROBOTS);
			// Need to currently generate collective intelligence for the shoreside here
			javaCI.generateCollectiveIntFiles("src/atlascollectiveint/custom", CollectiveIntGenTypes.ALL_COMPUTERS);
			
		} catch (ConversionFailed cf) {
			System.out.println("ERROR: DSL conversion to MOOS representation failed");
		}
	}
	
	public static void main(String [] args) {
		String outputCodeDir = System.getProperty("user.dir") + "/codegen-test1/";
		System.out.println("Generating code in: " + outputCodeDir);
		testCodeGeneration1(outputCodeDir);
		System.out.println("Code generation completed");
		
		outputCodeDir = System.getProperty("user.dir") + "/codegen-test2/";
		System.out.println("Generating code from DSL in: " + outputCodeDir);
		
		try {
			testCodeGeneration2(outputCodeDir);
		} catch (DSLLoadFailed e) {
			System.out.println("DSL loading configuration error");
			e.printStackTrace();
		}
	}
}
