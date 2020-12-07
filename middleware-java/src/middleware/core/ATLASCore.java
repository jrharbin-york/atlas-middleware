package middleware.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.locationtech.jts.io.ParseException;

import atlasdsl.*;
import atlasdsl.faults.*;
import atlassharedclasses.*;
import faultgen.FaultGenerator;
import middleware.carstranslations.CARSTranslations;
import middleware.gui.GUITest;
import middleware.logging.ATLASLog;
import middleware.missionmonitor.*;

import fuzzingengine.*;
import fuzzingengine.operations.FuzzingOperation;
import fuzzingengine.operations.NumericVariableChangeFuzzingOperation;

// This code will be combined with the simulator-specific code
// during code generation
public abstract class ATLASCore {
	protected ATLASEventQueue<?> carsIncoming;
	protected ATLASEventQueue<?> fromCI;
	
	// TODO: read this from the interface
	private double timeLimit = 1200.0;
	
	//protected ATLASEventQueue fromFaultGen;
	// for now the fault generator is installed in the middleware process itself,
	// not communicating over ActiveMQ with it
	
	protected ActiveMQProducer outputToCI;
	private final int CI_QUEUE_CAPACITY = 100;
	protected Mission mission;
	protected MissionMonitor monitor;
	protected CARSTranslations carsOutput;
	
	protected FuzzingEngine fuzzEngine;
	
	private GUITest gui;

	@SuppressWarnings("rawtypes")
	protected List<ATLASEventQueue> queues = new ArrayList<ATLASEventQueue>();
	protected List<FaultInstance> activeFaults = new ArrayList<FaultInstance>();
	
	protected List<SensorDetectionLambda> sensorWatchers = new ArrayList<SensorDetectionLambda>();
	protected List<PositionUpdateLambda> positionWatchers = new ArrayList<PositionUpdateLambda>();
	
	private FaultGenerator faultGen;
	
	private double time = 0.0;
	
	private void setupPositionLogging() {
		setupPositionWatcher((gps) -> {
			double x = gps.getX();
			double y = gps.getY();
			String rname = gps.getRobotName();
			ATLASLog.logPosition(time, rname, x, y);
		});
	}
	
	public ATLASCore(Mission mission) {
		this.mission = mission;
		this.monitor = new MissionMonitor(this, mission);
		fromCI = new CIEventQueue(this, mission, CI_QUEUE_CAPACITY);
		queues.add(fromCI);
		faultGen = new FaultGenerator(this,mission);
		fuzzEngine = new NullFuzzingEngine();
		setupPositionLogging();
	}
	
	public CARSTranslations getCARSTranslationOutput() {
		return carsOutput;
	}
	
	protected void createGUI() {
		gui = new GUITest(this,mission, faultGen);
	}
	
	public synchronized void registerFault(FaultInstance fi) {
		if (fi.getLength() > 0.0) {
			activeFaults.add(fi);
			System.out.println("Fault instance added");
			Fault f = fi.getFault();
			Optional<String> additionalData = fi.getExtraDataOpt();
			f.immediateEffects(this, additionalData);
		} else {
			System.out.println("Invalid fault of zero length rejected");
		}
	}
	
	public synchronized void completeFault(FaultInstance fi) {
		activeFaults.remove(fi);
		Fault f = fi.getFault();
		Optional<String> additionalData = fi.getExtraDataOpt();
		System.out.println("Calling completion effect");
		
		fi.getFault().completionEffects(this, additionalData);
		System.out.println("Fault instance " + fi + " completed: " + f.getClass());
	}
	
	public void clearFaults() {
		activeFaults.clear();
	}
    
    public ActiveMQProducer getCIProducer() {
    	return outputToCI;
    }
	
    public void runMiddleware()  {
		for (ATLASEventQueue<?> q : queues) {
			// Since the GUI displays global status, it
			// needs to be updated following every event on any queue
			if (gui != null) {
				q.registerAfterHook(() -> gui.updateGUI());
			}
			// Also after events, need to check for faults
			q.registerAfterHook(() -> faultGen.pollFaultsNow());
			q.registerAfterHook(() -> monitor.runStep());
			q.setup();
		}
		
		for (ATLASEventQueue<?> q : queues) {
			new Thread(q).start();
		}
    }

	public double getTime() {
		return time; 
	}
	
	public synchronized void updateTime(double time) throws CausalityException {
		//System.out.println("updateTime called with " + time);
		if ((time > this.time)) {
			this.time = time;
			if (this.time > timeLimit) {
				ATLASLog.logTime(time);
			}
		}
	}
	
	// This is used by active faults to inject their immediate effects
	// upon the low-level CARS simulation
	public void sendToCARS(Robot r, String key, String value) {
		CIEventQueue CIq = (CIEventQueue)fromCI;
		CIq.sendToCARS(r, key, value);
	}

	public List<FaultInstance> activeFaultsOfClass(Class<?> class1) {
		for (FaultInstance fi : activeFaults) {
			System.out.println("fault instance name " + fi.getFault().getImpact().getClass().getName());
		}
		
		System.out.println("class1 name: = " + class1.getName());
		System.out.println("activeFaults count: " + activeFaults.size());
		return activeFaults.stream()
				.filter(fi -> fi.getFault().getImpact().getClass() == class1)
				.collect(Collectors.toList());
	}

	// This is called by the simulator-side event queues when a low
	// level sensor detection occurs
	public void notifySensorDetection(SensorDetection d) {
		for (SensorDetectionLambda watcher : sensorWatchers) { 
			watcher.op(d);
		}
	}
	
	public void setupSensorWatcher(SensorDetectionLambda l) {
		sensorWatchers.add(l);
	}
	
	public void setFaultDefinitionFile(String filePath) {
		faultGen.setFaultDefinitionFile(filePath);
		
		if (gui != null) {
			gui.setFaultDefinitionFile(filePath);
		}		
	}

	public FuzzingEngine getFuzzingEngine() {
		return fuzzEngine;
	}
	
	public void setupPositionWatcher(PositionUpdateLambda l) {
		positionWatchers.add(l);
	}
	
	public void notifyPositionUpdate(GPSPositionReading gps) {
		for (PositionUpdateLambda watcher : positionWatchers) { 
			watcher.op(gps);
		}
	}

	public double getTimeLimit() {
		return timeLimit;
	}
}
