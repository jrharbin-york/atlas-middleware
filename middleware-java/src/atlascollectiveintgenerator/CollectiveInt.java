package atlascollectiveintgenerator;

import atlasdsl.loader.*;
import atlascollectiveint.api.*;
import atlassharedclasses.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map.Entry;
import java.util.Optional;

import activemq.portmapping.PortMappings;
import atlasdsl.*;

public class CollectiveInt {
	private Mission mission;
	protected Class<?> ciClass;
	//protected Object ciInstance;
	
	protected static double timeNow = 0.0;
	
	public static double getTimeNow() {
		return timeNow;
	}
	
	private CollectiveIntActiveMQConsumer consumer;
	protected HashMap<String, CollectiveIntActiveMQProducer> moosProducers = new LinkedHashMap<String, CollectiveIntActiveMQProducer>();
	protected HashMap<String, Timer> timers = new LinkedHashMap<String,Timer>();
	
	public CollectiveInt(String cIClassName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		ciClass = Class.forName(cIClassName);
		System.out.println("ciClass = " + ciClass);
		//ciInstance = ciClass.getDeclaredConstructor().newInstance();
	}
	
	protected void handleMessage(ATLASSharedResult a) {
		if (a.getContentsClass() == ATLASTimeUpdate.class) {
			Optional<ATLASTimeUpdate> a_o = a.getATLASTimeUpdate();
			if (a_o.isPresent()) {
				double newTime = a_o.get().getTime();
				updateTime(newTime);
			}
		}
	}
	
	public void checkTimers() {
		// Use iterator instead of for as we may to change the collection
		// while iterating over it
		Iterator<Entry<String, Timer>> it = timers.entrySet().iterator();
	    while (it.hasNext()) {
	        Timer t = it.next().getValue();
	        //System.out.println("timeNow = " + timeNow);
			if (t.isReady(timeNow)) {
				t.performAction();
			}
	        
			if (t.shouldRemove(timeNow)) {
				it.remove();
			}
	    }
	}
	
	public void startCI() {
		System.out.print("startCI beginning...\n");
		consumer.run();
	}
	
	protected void updateTime(double newTime) {
		if (timeNow < newTime) {
			timeNow = newTime;
		}
	}

	public void init() {
		System.out.println();
		DSLLoader l = new GeneratedDSLLoader();
		try {
			mission = l.loadMission();
			API.setCIReference(this);
			
			List<Computer> cs = mission.getAllComputers();
			if (cs.size() > 0) {
				Computer c = cs.get(0);
				String ciCompName = c.getName();
				consumer = new CollectiveIntActiveMQConsumer(PortMappings.portForCI(ciCompName), mission, this);
			}
			
			List<String> producerNames = new ArrayList<String>();
			
			for (Robot r : mission.getAllRobots()) {
				producerNames.add(r.getName());
			}
			
			for (Computer c : mission.getAllComputers()) {
				producerNames.add(c.getName());
			}
			
			for (String name : producerNames) {
				CollectiveIntActiveMQProducer p = new CollectiveIntActiveMQProducer(PortMappings.portForMiddlewareFromCI(name), mission);
				moosProducers.put(name, p);
				API.registerNewProducer(name, p);
				p.setupConnection();
			}
		} catch (DSLLoadFailed e) {
			System.out.println("CollectiveInt.init - loadMission failed - DSL configuration error");
			e.printStackTrace();
		}
	}
	
	public void registerTimer(String timerName, Timer t) {
		timers.put(timerName, t);
	}
}