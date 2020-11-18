package exptrunner.systematic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import atlasdsl.faults.*;
import atlassharedclasses.FaultInstance;


public class SingleFaultCoverageExpt extends ExptParams {
	private double minLength;
	private double maxLength;
	
	private FileWriter combinedResults;
	
	// The time range to be swept 
	private double timeStart;
	private double timeEnd;
	
	private double time;
	private double len;
	
	// The fault number to use
	private Fault fault;
	
	// The current fault ID
	private int currentFault;
	
	private Optional<String> extraFaultInstanceData;
	
	private boolean completed = false;
	private double stepFactor;
	
	public SingleFaultCoverageExpt(String filename, double runTime, double timeStart, double timeEnd, double maxLength, double minLength, double stepFactor, Fault fault, Optional<String> extraFaultInstanceData) throws IOException {
		super(runTime);
		this.timeStart = timeStart;
		this.time = timeStart;
		this.timeEnd = timeEnd;
		this.maxLength = maxLength;
		this.len = maxLength;
		this.minLength = minLength;
		this.completed = false;
		this.fault = fault;
		this.stepFactor = stepFactor;
		this.combinedResults = new FileWriter(filename);
		this.currentFault = 0;
		this.extraFaultInstanceData = extraFaultInstanceData;
	}

	public void advance() {
		time += len;
		if (time >= timeEnd) {
			time = timeStart;
			len = len * stepFactor;
		}
		currentFault++;
	}

	public List<FaultInstance> specificFaults() {
		List<FaultInstance> fs = new ArrayList<FaultInstance>();
		System.out.println("Generating fault instance at " + time + " of length " + len);
		FaultInstance fi = new FaultInstance(time, time+len, fault, extraFaultInstanceData);
		fs.add(fi);
		return fs;
	}
	
	private String specificFaultsAsString() {
		List<FaultInstance> fis = specificFaults();
		String str = fis.stream().map(f -> f.toString()).collect(Collectors.joining());
		return str;
	}

	public boolean completed() {
		return (len < minLength); 
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
					combinedResults.write(currentFault + "," + specificFaultsAsString() + "," + time + "," + robot + "," + num + "\n");
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
		System.out.println("time = " + time + ",len = " + len);
	}
}
