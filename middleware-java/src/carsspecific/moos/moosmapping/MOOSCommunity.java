package carsspecific.moos.moosmapping;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MOOSCommunity {
	protected String communityName;
	private static int dbPortBase = 9000;
	protected int dbPortOffset;
	private int moosTimeWarp = 10;
	
	private Map<String,MOOSProcess> processes = new LinkedHashMap<String,MOOSProcess>();
	private List<String> sharedVars = new ArrayList<String>();
	
	private void genBasicMissionParams(FileWriter missionFile) throws IOException {
		missionFile.write("ServerHost = localhost\n");
		missionFile.write("ServerPort = " + (dbPortBase + dbPortOffset) + "\n");
		missionFile.write("Simulator = true\n");
		missionFile.write("community = " + communityName + "\n");
		
		missionFile.write("MOOSTimeWarp = " + moosTimeWarp + "\n");
		
		missionFile.write("LatOrigin = 43.825300\n");
		missionFile.write("LongOrigin = -70.330400\n");
	}
	
	private void genANTLERBlock(FileWriter missionFile) throws IOException {
		missionFile.write("\nProcessconfig = ANTLER\n{\n");
		missionFile.write("MSBetweenLaunches = 100\n");
		String consoleStatus = " @ NewConsole = false";
		for (MOOSProcess p : processes.values()) {
			missionFile.write("Run = " + p.processName + consoleStatus + "\n");
		}
		missionFile.write("}\n\n");
	}
	
	private void generateMissionFile(FileWriter missionFile) throws IOException {
		genBasicMissionParams(missionFile);
		genANTLERBlock(missionFile);
		for (MOOSProcess p : processes.values()) {
			p.generateConfigBlock(missionFile);
		}
	}
	
	private void generateBehaviourFile(FileWriter behaviourFile) throws IOException {
		for (MOOSProcess p : processes.values()) {
			p.generateBehavioursForProcess(behaviourFile);
		}
	}
	
	private void generateCustomCode(MOOSFiles mf) throws IOException {
		for (MOOSProcess p : processes.values()) {
			p.generateCustomCode(mf);
		}
	}
	
	private void prepareAdditionalProperties() {
		for (MOOSProcess p : processes.values()) {
			p.prepareAdditionalProperties();
		}
	}
	
	public String getMissionFileName() {
		return "targ_" + communityName + ".moos";
	}
	
	public String getBehaviourFileName() {
		return "targ_" + communityName + ".bhv";
	}
	
	public void generateCode(MOOSFiles mf) {
		// Generate mission and behaviour files
		try {
			prepareAdditionalProperties();
			FileWriter missionFile = mf.getOpenFile(getMissionFileName());
			generateMissionFile(missionFile);
			FileWriter behaviourFile = mf.getOpenFile(getBehaviourFileName());
			generateBehaviourFile(behaviourFile);
			generateCustomCode(mf);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getDBPort() {
		return dbPortBase + dbPortOffset;
	}
	
	public String getCommunityName() {
		return communityName;
	}
	
	private void addProcessWatch() {
		addProcess(new UProcessWatch(this));
	}
	
	private void addMOOSDB() {
		addProcess(new MOOSDBProcess(this));
	}
	
	public MOOSCommunity(MOOSSimulation sim, String communityName) {
		this.communityName = communityName;
		this.dbPortOffset = sim.nextPortOffsetCounter();
		addMOOSDB();
		addProcessWatch();
	}
	
	public MOOSCommunity(MOOSSimulation sim, String communityName, int dbPortOffset) {
		this.communityName = communityName;
		this.dbPortOffset = dbPortOffset;
		addMOOSDB();
		addProcessWatch();
	}
	
	public MOOSCommunity(MOOSSimulation sim, String communityName, int dbPortOffset, boolean watchProcess) {
		this.communityName = communityName;
		this.dbPortOffset = dbPortOffset;
		addMOOSDB();
		if (watchProcess) {
			addProcessWatch();
		}
	}
	
	public void addProcess(MOOSProcess p) {
		processes.put(p.processName, p);
	}
	
	public void registerSharedVar(String varName) {
		sharedVars.add(varName);
	}
	
	public List<String> getSharedVars() {
		return sharedVars;
	}
	
	public void registerSharedVars(List<String> varNames) {
		for (String v : varNames) 
			registerSharedVar(v);
	}
	
	public MOOSProcess getProcess(String name) {
		return processes.get(name);
	}
}
