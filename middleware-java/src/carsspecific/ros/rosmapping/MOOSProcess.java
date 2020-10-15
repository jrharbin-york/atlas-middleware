package carsspecific.ros.rosmapping;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MOOSProcess extends MOOSElement {
	private List<MOOSBehaviour> behaviours = new ArrayList<MOOSBehaviour>();
	// Used to generate the initialise lines at start of behaviour file
	protected Map<String,Object> moosBehaviourInitVals = new LinkedHashMap<String,Object>();
	
	protected Map<String,MOOSSetModeDetails> setModeProperties = new LinkedHashMap<String,MOOSSetModeDetails>();

	private static int defaultCommsTick = 2;
	private static int defaultAppTick = 2;
	
	public MOOSProcess(String processName, MOOSCommunity parent) {
		this.parent = parent;
		this.processName = processName;
		this.setProperty("AppTick", defaultAppTick);
		this.setProperty("CommsTick", defaultCommsTick);
	}
	
	MOOSCommunity parent;
	String processName;
	
	public void generateConfigBlock(FileWriter missionFile) throws IOException {
		missionFile.write("ProcessConfig = " + processName + "\n{\n");
		writePropertiesDefault(missionFile);
		missionFile.write("}\n\n");
	}
	
	// If the process has any extra code to for example, set properties based on its
	// parent community, they can be placed here in subclasses
	public void prepareAdditionalProperties() {
		
	}
	
	// If the process has any extra code to for example, generate custom files, 
	// this is the place in which to put it. Normally, it is a empty, but subclasses
	// such as UFldHazardSensorProcess may use it to generate hazards.txt 
	public void generateCustomCode(MOOSFiles mf) throws IOException {
		
	}
	
	private void generateBehaviourInitSection(FileWriter bhvFile) throws IOException {
		// Generate the initialisation values
		for (Map.Entry<String,Object> entry : moosBehaviourInitVals.entrySet()) {
			String name = entry.getKey();
			Object value = entry.getValue();
			bhvFile.write("initialize " + name + " = " + value + "\n");
		}
		
		for (Map.Entry<String, MOOSSetModeDetails> setModeEntry : setModeProperties.entrySet()) {
			setModeEntry.getValue().generate(bhvFile);
		}
	}
	
	public void generateBehavioursForProcess(FileWriter behaviourFile) throws IOException {	
		generateBehaviourInitSection(behaviourFile);
		for (MOOSBehaviour b : behaviours)
			b.generateCode(behaviourFile);
	}
	
	public void addBehaviour(MOOSBehaviour b) {
		behaviours.add(b);
	}
}
