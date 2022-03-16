package middleware.core;

import activemq.portmapping.PortMappings;
import atlasdsl.Mission;
import carsspecific.ros.carsqueue.*;
import carsspecific.ros.translations.CARSTransationSetupFailed;
import carsspecific.ros.translations.ROSTranslations;
import middleware.carstranslations.CARSTranslations;

public class ROSATLASCore extends ATLASCore {
	private int MOOS_QUEUE_CAPACITY = 2000;
	
	public ROSATLASCore(Mission mission) {
		super(mission, true);
		try {
			outputToCI = new ActiveMQProducer(PortMappings.portForCI("controller"), ActiveMQProducer.QueueOrTopic.TOPIC);
			outputToCI.run();
			ROSEventQueue_AMCL carsIncoming = new ROSEventQueue_AMCL(this, mission, MOOS_QUEUE_CAPACITY, fuzzEngine);
			carsIncoming.setup();
			carsOutput = (CARSTranslations) new ROSTranslations(carsIncoming.getROS());
			queues.add(carsIncoming);
		} catch (CARSTransationSetupFailed e) {
			e.printStackTrace();
		}
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