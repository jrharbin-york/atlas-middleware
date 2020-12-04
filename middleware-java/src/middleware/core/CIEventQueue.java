package middleware.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import activemq.portmapping.PortMappings;
import atlasdsl.*;
import atlasdsl.faults.*;
import atlassharedclasses.*;

public class CIEventQueue extends ATLASEventQueue<CIEvent> {
	private static final long serialVersionUID = 1L;
	private Mission mission;
	private ATLASCore core;
	
	private HashMap<String,ActiveMQProducer> producers = new LinkedHashMap<String,ActiveMQProducer>();
	private HashMap<String,CIActiveMQConsumer> consumers = new LinkedHashMap<String,CIActiveMQConsumer>();

	public CIEventQueue(ATLASCore core, Mission mission, int capacity) {
		super(core, capacity, '@');
		this.mission = mission;
		this.core = core;
	}
	
	public void setup() {
		// Create the producers to send out converted updates to the relevant MOOSDB's
		for (Robot r : mission.getAllRobots()) {
			String name = r.getName();
			ActiveMQProducer p = new ActiveMQProducer(PortMappings.portForMOOSDB(name), ActiveMQProducer.QueueOrTopic.TOPIC);
			CIActiveMQConsumer c = new CIActiveMQConsumer(name, PortMappings.portForMiddlewareFromCI(name), this);
			producers.put(name, p);
			p.run();
			consumers.put(name,c);
			startThread(c, false);
		}
		for (Computer cp : mission.getAllComputers()) {
			String name = cp.getName();
			ActiveMQProducer p = new ActiveMQProducer(PortMappings.portForMOOSDB(name), ActiveMQProducer.QueueOrTopic.TOPIC);
			CIActiveMQConsumer c = new CIActiveMQConsumer(name, PortMappings.portForMiddlewareFromCI(name), this);
			producers.put(name, p);
			p.run();
			consumers.put(name, c);
		}
		
		// Set these producers so they are ready for the CARS translations to use
		core.getCARSTranslationOutput().setOutputProducers(producers);
	}
	
	// This is used by active faults to inject their immediate effects
	// upon the low-level CARS simulation
	public void sendToCARS(Robot r, String key, String value) {
		core.getCARSTranslationOutput().sendCARSUpdate(r.getName(), key, value);
	}
	
	private static String pointListToPolyString(List<Point> coords) {
		String coordsJoined = coords.stream()
				.map(p -> p.toStringBareCSV())
				.collect(Collectors.joining(":"));
		return coordsJoined;
	}
	
	public void handleEvent(CIEvent event) {
		System.out.println("CIEventQueue.handleEvent - " + event.toString());
		// General procedure:
		// 1) log received event at the middleware side
		// 2) apply any relevant faults if they are registered in the middleware
		// 3) convert it into a simulator specific representation
		// 4) then send to MOOS producers to be relayed to MOOSDBs
		
		// TOOD: log the incoming event here
		
		BehaviourCommand ciCmd = event.getCommand();
		String robotName = event.getRobotName();
		
		System.out.println("CIEvent behaviour command class = " + ciCmd.getClass().toString());
		
		// Dispatch types of CI event, convert it into a low-level simulator event
		// currently handle a BehaviourEvent
		if (ciCmd instanceof ActivateBehaviour) {
			ActivateBehaviour actCmd = (ActivateBehaviour)ciCmd;
			// TODO: Check for any fault impacting the command here
			System.out.println("CIEventQueue - ActivateBehaviour received");
		}
		
		if (ciCmd instanceof StartVehicle) {
			StartVehicle startCmd = (StartVehicle)ciCmd;
			// TODO: Check for any fault impacting the command here?
			System.out.println("CIEventQueue - StartVehicle received");
			core.getCARSTranslationOutput().startRobot(robotName);
		}
		
		if (ciCmd instanceof SetCoordinates) {
			SetCoordinates setCmd = (SetCoordinates)ciCmd;
			// put the faults that impact the coordinate processing here
			List<Point> coordinates = setCmd.getCoordinates();

			// Check for faults impacting the coordinates here
			List<FaultInstance> fs = core.activeFaultsOfClass(MutateMessage.class);
			System.out.println("faults of class MutateMessage: " + fs.size());
			
			List<Point> modifiedCoords = coordinates;
			
			for (FaultInstance fi : fs) {
				Fault f = fi.getFault();
				FaultImpact fim = f.getImpact();
				System.out.println("fim class = " + fim.getClass().toString());
				if (fim instanceof MessageImpact) {
					MessageImpact mim = (MessageImpact)fim;
					System.out.println(mim.getMessage().getName());
					System.out.println(setCmd.getMessageName());
					
					// Check the fault message name matches this name
					if (mim.getMessage().getName().equals(setCmd.getMessageName())) {  
						System.out.println("before applying fault:  coords =" + modifiedCoords.toString());
						Optional<String> additionalData = fi.getExtraDataOpt();
						modifiedCoords = (List<Point>)f.applyFaultToData(modifiedCoords, additionalData);
						System.out.println("applying fault: modifiedCoords = " + modifiedCoords.toString());
					}
					// TODO: check is from shoreside? - this encodes assumption that the
					// CI runs on shoreside						
				}
			}
			
			// TODO: this contains MOOS-specific conversion here - push into the MOOS layer
			String polyUpdate = "polygon=" + pointListToPolyString(modifiedCoords) + ":label," + robotName + "_LOITER";
			System.out.println("CIEventQueue - SetCoordinates received: vehicle " + robotName + " : " + polyUpdate);
			core.getCARSTranslationOutput().sendCARSUpdate(robotName, "UP_LOITER", polyUpdate);
		}
	}
}
