package middleware.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.jms.JMSException;

import com.fasterxml.jackson.core.JsonProcessingException;

import atlasdsl.*;
import atlasdsl.faults.*;
import atlassharedclasses.*;
import faultgen.FaultGenerator;
import middleware.carstranslations.CARSTranslations;
import middleware.gui.GUITest;
import middleware.logging.ATLASLog;
import middleware.missionmonitor.*;

import fuzzingengine.*;
import fuzzingengine.spec.GeneratedFuzzingSpec;

public abstract class ATLASCore {
	private static final boolean DEBUG_PRINT_DESERIALISED_MSGS_SENT_TO_CI = false;
	protected boolean stopOnNoEnergy = false;
	
	protected ATLASEventQueue<?> carsIncoming;
	protected ATLASEventQueue<?> fromCI;
	
	private double timeLimit;
	
	protected ActiveMQProducer outputToCI;
	private final int CI_QUEUE_CAPACITY = 100;
	protected Mission mission;
	protected MissionMonitor monitor;
	protected CARSTranslations carsOutput;
	
	protected FuzzingEngine fuzzEngine;
	
	protected Map<String,Boolean> vehicleBatteryFlat = new HashMap<String,Boolean>(); 
	
	private GUITest gui;

	@SuppressWarnings("rawtypes")
	protected List<ATLASEventQueue> queues = new ArrayList<ATLASEventQueue>();
	protected HashMap<String,Object> goalVariables = new LinkedHashMap<String,Object>();
	protected List<FaultInstance> activeFaults = new ArrayList<FaultInstance>();
	protected List<SensorDetectionLambda> sensorWatchers = new ArrayList<SensorDetectionLambda>();
	protected List<PositionUpdateLambda> positionWatchers = new ArrayList<PositionUpdateLambda>();
	
	private FaultGenerator faultGen;	
	private double time = 0.0;
	
	private ATLASObjectMapper atlasOMapper = new ATLASObjectMapper();
	
	public void setupPositionPropertyUpdaters() {
		setupPositionWatcher((pos) -> {
			String rname = pos.getRobotName();
			Robot r = mission.getRobot(rname);
			r.setPointComponentProperty("location", new Point(pos.getX(), pos.getY(), pos.getZ()));
		});
	}
	
	public ATLASCore(Mission mission) {
		this.mission = mission;
		stopOnNoEnergy = mission.stopOnNoEnergy();
		this.timeLimit = mission.getEndTime();
		this.monitor = new MissionMonitor(this, mission);
		fromCI = new CIEventQueue(this, mission, CI_QUEUE_CAPACITY);
		queues.add(fromCI);
		faultGen = new FaultGenerator(this,mission);
		fuzzEngine = GeneratedFuzzingSpec.createFuzzingEngine(mission, true);
		setupEnergyOnRobots();
		setupPositionPropertyUpdaters();
	}
	
	public ATLASCore(Mission mission, boolean includeCIQueue) {
		this.mission = mission;
		stopOnNoEnergy = mission.stopOnNoEnergy();
		this.timeLimit = mission.getEndTime();
		this.monitor = new MissionMonitor(this, mission);
		if (includeCIQueue) {
			fromCI = new CIEventQueue(this, mission, CI_QUEUE_CAPACITY);
			queues.add(fromCI);
		}
		faultGen = new FaultGenerator(this,mission);
		fuzzEngine = GeneratedFuzzingSpec.createFuzzingEngine(mission, true);
		setupEnergyOnRobots();
		setupPositionPropertyUpdaters();
	}
	
	public CARSTranslations getCARSTranslator() {
		return carsOutput;
	}
	
	public void createGUI() {
		gui = new GUITest(this,mission, faultGen);
	}
	
	public synchronized void registerFault(FaultInstance fi) {
		activeFaults.add(fi);
		System.out.println("Fault instance added");
		Fault f = fi.getFault();
		Optional<String> additionalData = fi.getExtraDataOpt();
		f.immediateEffects(this, additionalData);
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
    
    public void sendMessageToCI(Object o) {
    	try {
			String msg = atlasOMapper.serialise(o);
			if (DEBUG_PRINT_DESERIALISED_MSGS_SENT_TO_CI) {
				System.out.println("sendMessageToCI: serialised message " + msg);
			}
			outputToCI.sendMessage(msg);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		} catch (JMSException e1) {
			e1.printStackTrace();
		};
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

	public void registerEnergyUsage(Robot r, Point newLocation) {
		r.registerEnergyUsage(newLocation);
		if (stopOnNoEnergy) {
			String robotName = r.getName();
			if (r.noEnergyRemaining() && !vehicleBatteryFlat.containsKey(robotName)) {
				vehicleBatteryFlat.put(robotName, true);
				getCARSTranslator().stopVehicle(r.getName());
				System.out.println("STOPPING VEHICLE " + r.getName() + " due to no energy remaining");
			}
		}
	}

	public double getRobotEnergyRemaining(Robot r) {
		return r.getEnergyRemaining();
	}
	
	public void setupEnergyOnRobots() {
		for (Robot r : mission.getAllRobots()) {
			r.setupRobotEnergy();
		}
	}
	
	public void setGoalVariable(String vehicleName, String topicName, Object val) {
		goalVariables.put(vehicleName + "-_-" + topicName, val);
	}
	
	public Object getGoalVariable(String vehicleName, String topicName) {
		return goalVariables.get(vehicleName + "-_-" + topicName);
	}
}