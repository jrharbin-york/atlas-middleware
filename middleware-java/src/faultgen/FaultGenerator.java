package faultgen;

import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import atlassharedclasses.FaultInstance;
import atlasdsl.*;
import atlasdsl.faults.*;
import middleware.core.*;

public class FaultGenerator {
	// Custom comparator used in the queue which sorts faults by finish time
	private class ByFinishTime implements Comparator<FaultInstance> {
	    public int compare(FaultInstance fi1, FaultInstance fi2) {
	    	System.out.println("TIMES = " + fi1.getEndTime() + " - " + fi2.getEndTime());
	        return Double.compare(fi1.getEndTime(), fi2.getEndTime());
	    }
	}
	
	private Mission mission;
	private ATLASCore core;
	private FaultFileIO faultFileIO;
	
	// This holds the fault instances that have been read from the definition file, 
	// but not yet injected. Sorted in order of injection time
	private PriorityQueue<FaultInstance> scheduledFaults = new PriorityQueue<FaultInstance>();
	
	ByFinishTime compByFinishTime = new ByFinishTime();
	
	// This holds injected fault instances in order of their removal time
	private PriorityQueue<FaultInstance> injectedFaults = new PriorityQueue<FaultInstance>(compByFinishTime);
	
	public FaultGenerator(ATLASCore core, Mission mission) {
		this.core = core;
		this.mission = mission;
		this.faultFileIO = new FaultFileIO(mission);
	}
	
	public void injectSpeedFaultNow(double timeLength, String robotName) {
		Robot r = mission.getRobot(robotName);
		// Need to look up the MotionSource for the target robot
		Optional<MotionSource> ms_o = r.getMotionSource(); 
		if (ms_o.isPresent()) {
			MotionSource ms = ms_o.get();
			double startTime = core.getTime();
			double endTime = startTime + timeLength;
			FaultImpact fi;
			try {
				fi = new MotionFault(ms, "speed", "5.0");
				Fault f = new Fault(fi);
				FaultInstance fInstance = new FaultInstance(startTime, endTime, f, Optional.empty());
				scheduledFaults.add(fInstance);
			} catch (InvalidComponentType e) {
				System.out.println("Injecting fault failed - invalid component type not a MotionSource");
				e.printStackTrace();
			}
		} else {
			System.out.println("Injecting fault failed - no motion source for " + robotName);
		}
	}
	
	public void pollFaultsNow() {
		double time = core.getTime();
		FaultInstance nextFault = scheduledFaults.peek();
		if (nextFault != null && nextFault.isReady(time)) {
			FaultInstance next = scheduledFaults.remove();
			// If the fault is not active, it is discarded
			if (next.isActive()) {
				injectedFaults.add(next);
				core.registerFault(next);
			}
		}
		
		FaultInstance nextToFinish = injectedFaults.peek();
		if (nextToFinish != null) {
			//System.out.println("nextToFinish.isFinished(" + time + ")" + nextToFinish.isFinished(time));
			if (nextToFinish != null && nextToFinish.isFinished(time)) {
				FaultInstance next = injectedFaults.remove();
				core.completeFault(next);
			}
		}
	}
	
	public void clearFaults() {
		scheduledFaults.clear();
	}
	
	public void injectDynamicFault(Fault f, double length, Optional<String> extraData) {
		double startTime = core.getTime();
		double endTime = startTime + length;
		FaultInstance fi = new FaultInstance(startTime, endTime, f, extraData);
		scheduledFaults.add(fi);
	}

	public void setFaultDefinitionFile(String filePath) {
		try {
			List<FaultInstance> faultInstances = faultFileIO.loadFaultsFromFile(filePath);
			for (FaultInstance fi : faultInstances) {
				scheduledFaults.add(fi);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFaultFormat e) {
			e.printStackTrace();
		}
	}
}
