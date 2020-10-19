package carsspecific.moos.moosmapping;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import atlasdsl.*;

public class UFldHazardSensorProcess extends MOOSProcess {
	private List<EnvironmentalObject> objects = new ArrayList<EnvironmentalObject>(); 
	
	public UFldHazardSensorProcess(MOOSCommunity parent) {
		super("uFldHazardSensor", parent);
		setProperty("hazard_file", "hazards.txt");
		
		// TODO: factor these out into the DSL
		// These represent a curve which is used to configure detection probabilities
		setProperty("sensor_config", "width=25, exp=4, pclass=0.80");
		setProperty("sensor_config", "width=50, exp=2, pclass=0.60");
		setProperty("sensor_config", "width=10, exp=6, pclass=0.93");
		
		//TODO: Ensure it still produces detections while turning
		// in simple test case test case. This can be removed when a full sweep 
		// pattern is set up
		setProperty("max_turn_rate", 10.0);
	}
	
	public void addObject(EnvironmentalObject eo) {
		objects.add(eo);
	}
	
	public void generateCustomCode(MOOSFiles mf) throws IOException {
		FileWriter fw = mf.getOpenFile("hazards.txt");
		for (EnvironmentalObject eo : objects) {
			fw.write(eo.toString() + "\n");
		}
	}
}