package exptrunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import atlasdsl.faults.Fault;
import atlassharedclasses.FaultInstance;

public class RepeatSameFaultConfig extends ExptParams {

	private FileWriter combinedResults;
	
	// The time range for the fixed fault
	private double timeStart;
	private double timeEnd;
	private Fault fault;
	private int repeats;
	private int repeatsCount;
	
	private boolean completed = false;
	
	public RepeatSameFaultConfig(String filename, double runTime, double timeStart, double timeEnd, int repeatsCount, Fault fault) throws IOException {
		super(runTime);
		this.timeStart = timeStart;
		this.timeEnd = timeEnd;
		this.completed = false;
		this.fault = fault;
		this.combinedResults = new FileWriter(filename);
		this.repeats = 0;
		this.repeatsCount = repeatsCount;
	}

	public void advance() {
		repeats++;
	}

	public List<FaultInstance> specificFaults() {
		List<FaultInstance> fs = new ArrayList<FaultInstance>();
		System.out.println("Generating fault instance at " + timeStart + " to " + timeEnd);
		FaultInstance fi = new FaultInstance(timeStart, timeEnd, fault, Optional.empty());
		fs.add(fi);
		return fs;
	}
	
	private String specificFaultsAsString() {
		List<FaultInstance> fis = specificFaults();
		String str = fis.stream().map(f -> f.toString()).collect(Collectors.joining());
		return str;
	}

	public boolean completed() {
		return (repeats >= repeatsCount); 
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
					combinedResults.write(repeats + "," + specificFaultsAsString() + "," + time + "," + robot + "," + num + "\n");
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
		System.out.println("repeats = " + repeats);
		
	}
}
