package atlasdsl;

import atlassharedclasses.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import atlasdsl.faults.Fault;

public class Mission {
	private Map<String,Robot> robots = new LinkedHashMap<String,Robot>();
	private Map<String,Computer> computers = new LinkedHashMap<String,Computer>();
	private Map<String,Region> regions = new LinkedHashMap<String,Region>();
	private Map<Integer,EnvironmentalObject> objects = new LinkedHashMap<Integer,EnvironmentalObject>();
	private Map<String,EnvironmentalObstacle> obstacles = new LinkedHashMap<String,EnvironmentalObstacle>();
	private Map<String,Goal> goals = new LinkedHashMap<String,Goal>();
	private Map<String,Message> messages = new LinkedHashMap<String,Message>();
	private Map<String,Fault> faults = new LinkedHashMap<String,Fault>();
	
	public List<Robot> getAllRobots() {
		return new ArrayList<Robot>(robots.values());
	}
	
	public List<Goal> getAllGoals() {
		return new ArrayList<Goal>(goals.values());
	}
	
	public List<String> getAllGoalNames() {
		return new ArrayList<String>();
	}
	
	public Map<String,Goal> getGoalsAndNames() {
		return goals;
	}
	
	public Optional<EnvironmentalObject> getEnvironmentalObject(int label) {
		EnvironmentalObject eo = objects.get(label);
		if (eo == null) return Optional.empty();
		else return Optional.of(eo);
	}
	
	public void addGoal(String n, Goal g) {
		goals.put(n,g);
	}
	
	public List<Computer> getAllComputers() {
		return new ArrayList<Computer>(computers.values());
	}
	
	public Robot getRobot(String name) {
		return robots.get(name);
	}
	
	public Computer getComputer(String name) {
		return computers.get(name);
	}
	
	public boolean includesComputer() {
		return (computers.size() > 0); 
	}
	
	public void addRobot(Robot r) {
		robots.put(r.getName(), r);
	}
	
	public void addMessage(Message m) {
		messages.put(m.getName(), m);
	}
	
	//TODO: setters for the other geometric regions as well
	public void addObject(EnvironmentalObject eo) {
		objects.put(eo.getLabel(), eo);
	}
	
	public void addComputer(Computer c) {
		computers.put(c.name, c);
	}
	
	public void addFault(Fault f) {
		faults.put(f.getName(), f);
	}

	public Collection<EnvironmentalObject> getEnvironmentalObjects() {
		return objects.values();
	}
	
	public List<Message> messagesToComponent(Component c) {
        List<Message> res = messages.values().stream()
                .filter(msg -> msg.isTo(c))
                .collect(Collectors.toList());
        return res;
	}
	
	public List<Message> messagesFromComponent(Component c) {
        List<Message> res = messages.values().stream()
                .filter(msg -> msg.isFrom(c))
                .collect(Collectors.toList());
        return res;
	}
	
	public List<Message> messagesToAnyComponent(List<Component> cs) {
        List<Message> res = messages.values().stream()
                .filter(msg -> cs.contains(msg.getTo()))
                .collect(Collectors.toList());
        return res;
	}
	
	public List<Message> messagesFromAnyComponent(List<Component> cs) {
        List<Message> res = messages.values().stream()
                .filter(msg -> cs.contains(msg.getFrom()))
                .collect(Collectors.toList());
        return res;
	}

	public List<Goal> getGoals() {
		return getAllGoals();
	}
	
	public Map<SensorType,Robot> getAllSensorTypesOnVehicles() {
		Map<SensorType,Robot> res = new LinkedHashMap<SensorType,Robot>();
		for (Robot r : robots.values()) {
			for (Sensor s : r.getAllSensors()) {
				SensorType t = s.getType();
				res.put(t, r);
			}
		}
		return res;
	}

	public Message getMessage(String messageName) {
		return messages.get(messageName);
	}

	public Optional<Fault> lookupFaultByName(String name) {
		Fault f = faults.get(name); 
		if (f != null) {
			return Optional.of(f);
		} else return Optional.empty(); 
	}

	public List<Fault> getFaultsAsList() {
		return new ArrayList<Fault>(faults.values());
	}

	public void addObstacle(EnvironmentalObstacle eob) {
		obstacles.put(eob.getLabel(), eob);
	}
	
	public Map<String,EnvironmentalObstacle> getObstacles() {
		return obstacles;
	}

	public boolean hasObstacles() {
		return (obstacles.size() > 0);
	}
}
