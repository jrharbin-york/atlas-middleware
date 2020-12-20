package exptrunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import atlasdsl.Mission;
import atlassharedclasses.FaultInstance;
import faultgen.FaultFileCreator;
import faultgen.FaultFileIO;
import faultgen.InvalidFaultFormat;

public class RandomFaultConfigs extends ExptParams {

	private FileWriter combinedResults;
	
	// The time range for the fixed fault
	private int run;
	private int repeatsCount;
	private Mission mission;
	private FaultFileIO fio;
	private String resFileName;
	private String tempFaultFileName;
	
	private String path = "/home/jharbin/academic/atlas/atlas-middleware/expt-working/";
	
	public RandomFaultConfigs(String tempFaultFilename, double runTime, String resFileName, int repeatsCount, Mission mission) throws IOException {
		super(runTime);
		this.combinedResults = new FileWriter(resFileName);
		this.run = 0;
		this.repeatsCount = repeatsCount;
		this.mission = mission;
		this.tempFaultFileName = tempFaultFilename;
		fio = new FaultFileIO(mission);
		genNewFaults();
	}
	
	private void genNewFaults() {
		FaultFileCreator ffc = new FaultFileCreator(mission, path);
		ffc.generateFaultListFromScratch(tempFaultFileName);
	}
	
	public void advance() {
		run++;
	}

	public List<FaultInstance> specificFaults() {
		try {
			List<FaultInstance> fs = fio.loadFaultsFromFile(path + tempFaultFileName);
			// Limit the number of faults loaded by repeats
			List<FaultInstance> selected = fs.stream().limit(run).collect(Collectors.toList());
			return selected;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return new ArrayList<FaultInstance>();
		} catch (InvalidFaultFormat e) {
			e.printStackTrace();
			return new ArrayList<FaultInstance>();
		} 
	}
	
	private String specificFaultsAsString() {
		List<FaultInstance> fis = specificFaults();
		String str = fis.stream().map(f -> f.toString()).collect(Collectors.joining());
		return str;
	}

	public boolean completed() {
		System.out.println("repeats = " + run + ",repeatsCount = " + repeatsCount);
		return (run >= repeatsCount); 
	}

	public void logResults(String logFileDir) {
		// Read the goal result file here - process the given goals
		// Write it out to a common result file - with the fault info
		File f = new File(logFileDir + "/goalLog.log");
		Scanner reader;
		try {
			reader = new Scanner(f);
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String[] fields = line.split(",");
				String goalClass = fields[0];
				String time = fields[1];
				String robot = fields[2];
				String num = fields[3];
				if (goalClass.equals("atlasdsl.DiscoverObjects")) {
					combinedResults.write(run + "," + specificFaultsAsString() + "," + time + "," + robot + "," + num + "\n");
					combinedResults.flush();
				}
			}
			reader.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	public void printState() {
		System.out.println("repeats = " + run);
		
	}
}
