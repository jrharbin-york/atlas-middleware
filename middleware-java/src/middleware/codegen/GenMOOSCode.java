package middleware.codegen;

import java.util.Optional;

import atlascollectiveintgenerator.*;

import atlasdsl.*;
import atlasdsl.loader.*;
import carsspecific.moos.codegen.MOOSCodeGen;
import fuzzingengine.FuzzingEngine;
import fuzzingengine.spec.GeneratedFuzzingSpec;
import middleware.atlascarsgenerator.ConversionFailed;
import middleware.atlascarsgenerator.carsmapping.CARSSimulation;

public class GenMOOSCode {
	private final static boolean GENERATE_NEW_CI_CODE = false;
	
	public static void testCodeGenerationMOOS(String code_dir) throws DSLLoadFailed {
		DSLLoader dslloader = new GeneratedDSLLoader();
		Mission mission;
		mission = dslloader.loadMission();
		
		FuzzingEngine fe = GeneratedFuzzingSpec.createFuzzingEngine(mission, true);
		MOOSCodeGen gen = new MOOSCodeGen(mission, Optional.of(fe));
		
		CollectiveIntGen javaCI = new JavaCollectiveIntGen(mission);		
		System.out.println("Converting DSL to MOOS representation...");
		try {
			CARSSimulation moossim = gen.convertDSL();
			System.out.println("DSL conversion completed");
			moossim.generateCARSInterface(code_dir);
			System.out.println("Code generation completed");
			if (GENERATE_NEW_CI_CODE) {
				javaCI.generateCollectiveIntFiles("src/atlascollectiveint/custom", CollectiveIntGenTypes.ALL_COMPUTERS);
			}
		} catch (ConversionFailed cf) {
			System.out.println("ERROR: DSL conversion to MOOS representation failed: reason " + cf.getReason());
		}
	}
	
	public static void main(String [] args) {
		String outputCodeDir = System.getProperty("user.dir") + "/moos-sim/";
		System.out.println("Generating code from DSL in: " + outputCodeDir);
		try {
			testCodeGenerationMOOS(outputCodeDir);
		}
		catch (DSLLoadFailed e) {
			System.out.println("DSL loading configuration error");
			e.printStackTrace();
			System.exit(1);
		}
	}
}

