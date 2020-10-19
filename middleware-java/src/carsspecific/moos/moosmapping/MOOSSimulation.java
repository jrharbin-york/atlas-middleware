package carsspecific.moos.moosmapping;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import carsmapping.*;

public class MOOSSimulation implements CARSSimulation {
	private int portOffsetCounter = 0;
	
	private List<MOOSCommunity> communities = new ArrayList<MOOSCommunity>();
	
	private void generateLaunchScript(MOOSFiles mf) throws IOException {
		String launchSeperatelyStr = " &";
		
		FileWriter missionFile = mf.getOpenFile("launch.sh");
		FileWriter missionFileExtLogs = mf.getOpenFile("launch-showconsole.sh");
		
		missionFile.write("#!/bin/sh\n\n");
		missionFile.write(". ./set_paths.sh\n");
		for (MOOSCommunity c : communities) {
			String logAppendStr = " > console-logs/" + c.getCommunityName() + "-out 2> console-logs/" + c.getCommunityName() + "-err " ;
			missionFile.write("pAntler " + c.getMissionFileName() + logAppendStr + launchSeperatelyStr + "\n");
			missionFileExtLogs.write("pAntler " + c.getMissionFileName() + launchSeperatelyStr + "\n");
		}
	}
	
	public int nextPortOffsetCounter() {
		return portOffsetCounter++;
	}
	
	public void generateCARSInterface(String baseDirectory) {
		MOOSFiles mf = new MOOSFiles(baseDirectory);
		
		try {
			generateLaunchScript(mf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (MOOSCommunity c : communities) { 
			c.generateCode(mf);
		}
		
		mf.closeAllFiles();
	}
	
	public MOOSSimulation() {
		
	}
	
	public void addCommunity(MOOSCommunity c) {
		communities.add(c);
	}

	public List<MOOSCommunity> getAllCommunities() {
		return communities;
	}
}
