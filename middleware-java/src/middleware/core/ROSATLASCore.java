package middleware.core;

import atlasdsl.Mission;
import carsspecific.ros.carsqueue.*;
import carsspecific.ros.translations.ROSTranslations;
import middleware.carstranslations.CARSTranslations;

public class ROSATLASCore extends ATLASCore {
	private int MOOS_QUEUE_CAPACITY = 2000;
	
	public ROSATLASCore(Mission mission) {
		super(mission, false);
		carsOutput = (CARSTranslations) new ROSTranslations();
		//outputToCI = new ActiveMQProducer(PortMappings.portForCI("shoreside"), ActiveMQProducer.QueueOrTopic.TOPIC);
		//outputToCI.run();
		carsIncoming = new ROSEventQueue_AMCL(this, mission, MOOS_QUEUE_CAPACITY, fuzzEngine);
		
		// TODO: this is temporary, just while testing the new ROS queue
		//carsIncoming.setup();
		
		queues.add(carsIncoming);
	}

	public void runMiddleware() {
		super.runMiddleware();
	}

	public void setFuzzingDefinitionFile(String filename) {
		fuzzEngine.setupFromFuzzingFile(filename, mission);
		
		if (gui != null) {
			gui.setFuzzingDefinitionFile(filename);
		}
	}
}