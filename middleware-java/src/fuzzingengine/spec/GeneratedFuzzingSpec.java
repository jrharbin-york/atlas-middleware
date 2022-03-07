package fuzzingengine.spec;

import java.util.Optional;
import atlasdsl.Mission;

import fuzzingengine.*;
public class GeneratedFuzzingSpec {

	public static FuzzingEngine createFuzzingEngine(Mission m) {
	FuzzingEngine fe = new FuzzingEngine(m);
	FuzzingSimMapping simMapping = new FuzzingSimMapping();
	
	fe.setSimMapping(simMapping);
	fe.setupFromFuzzingFile("/home/atlas/atlas/atlas-middleware/middleware-java/moos-sim/fuzz-configs/generated-fuzz.csv", m);
	return fe;
	}
}