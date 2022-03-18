package atlasdsl;

import atlassharedclasses.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import atlasdsl.SimulatorVariable.VariableTag;
import atlasdsl.faults.Fault;

public class Mission {
	private Map<String,Robot> robots = new LinkedHashMap<String,Robot>();
	private Map<String,Computer> computers = new LinkedHashMap<String,Computer>();
	private Map<String,Region> regions = new LinkedHashMap<String,Region>();
	private Map<Integer,EnvironmentalObject> objects = new LinkedHashMap<Integer,EnvironmentalObject>();
	private Map<String,EnvironmentalObstacle> obstacles = new LinkedHashMap<String,EnvironmentalObstacle>();
	private Region obstacleRegion;
	private Map<String,Goal> goals = new LinkedHashMap<String,Goal>();
	private Map<String,Message> messages = new LinkedHashMap<String,Message>();
	private Map<String,Fault> faults = new LinkedHashMap<String,Fault>();
	
	// This string is the topic name
	private Map<String, SimulatorVariable> simulatorVariables = new HashMap<String, SimulatorVariable>();
	
	// The string is the behaviour name
	private Map<String, List<SimulatorVariable>> behaviourVariables = new HashMap<String, List<SimulatorVariable>>();
	
	private boolean stopOnNoEnergy;
	private double endTime;
	
	public Mission(double endTime, boolean stopOnNoEnergy) {
		this.endTime = endTime;
		this.stopOnNoEnergy = stopOnNoEnergy;
	}
	
	public List<Robot> getAllRobots() {
		return new ArrayList<Robot>(robots.values());
	}
	
	public Map<Robot,Double> getAllRobotSpeeds() throws MissingProperty {
		Map<Robot,Double> robotSpeeds = new HashMap<Robot,Double>();
		for (Robot robot : getAllRobots()) {
			double speed = robot.getDoubleComponentProperty("startSpeed");
			robotSpeeds.put(robot, speed);
		}
		return robotSpeeds;
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
		r.checkPropertiesAndSetupState();
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
		return obstacles.size() > 0;
	}
	
	public List<String> getAllRobotAndComputerNames() {
		List<String> names = new ArrayList<String>();
		for (Robot r : getAllRobots()) {
			names.add(r.getName());
		}
		
		for (Computer c : getAllComputers()) {
			names.add(c.getName());
		}		
		return names;
	}
	
	public double getEndTime() {
		return endTime;
	}

	public Goal getGoalByName(String name) {
		return goals.get(name);
	}

	public boolean hasEnvObjects() {
		return objects.size() > 0;
	}
	
	public void setObstacleRegion(Region r) {
		obstacleRegion = r;
	}
	
	public Region getObstacleRegion() {
		return obstacleRegion;
	}
	
	public void addSimulatorVariable(SimulatorVariable sv) {
		simulatorVariables.put(sv.getVarName(), sv);
	}
	
	public Optional<SimulatorVariable> getSimulatorVariableByTag(VariableTag vt) {
		return simulatorVariables
				.values()
				.stream()
				.filter(sv -> sv.getTag() == vt)
				.findFirst();
	}

	public List<String> getSimulatorVariableNames() {
		return simulatorVariables.values().stream().map(sv -> sv.getVarName()).collect(Collectors.toList());
	}
	
	public Optional<SimulatorVariable> getSimulatorVariableByName(String name) {
		if (simulatorVariables.containsKey(name)) {
			return Optional.of(simulatorVariables.get(name));
		} else {
			return Optional.empty();
		}
	}
	
	public List<SimulatorVariable> getSimulatorVariables() {
		return new ArrayList<SimulatorVariable>(simulatorVariables.values());
	}
	
	public List<String> getBehaviourVariableNames() {
		return new ArrayList<String>();
		
	}
	
	public boolean stopOnNoEnergy() {
		return stopOnNoEnergy;
	}
}
