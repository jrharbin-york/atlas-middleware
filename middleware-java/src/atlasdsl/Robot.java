package atlasdsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import atlassharedclasses.Point;

public class Robot extends Component {
	private static final boolean ROBOT_ENERGY_DEBUGGING = true;
	private String robotname;
	private VehicleType vtype;
	private List<Subcomponent> contains = new ArrayList<Subcomponent>();
	
	private double currentEnergy = 0.0;
	private double startingEnergy = 0.0;
	
	private void setInitialLocation() throws MissingProperty {
		// Need to set the initial location property for the robot from startLocation
		// Otherwise the energy updates will fail
		Point loc = getPointComponentProperty("startLocation");
		setPointComponentProperty("location", new Point(loc.getX(), loc.getY()));
	}
	
	public Robot(String robotname) {
		this.robotname = robotname;
		this.vtype = VehicleType.KAYAK;
	}
	
	public void checkPropertiesAndSetupState() {
		try {
			setInitialLocation();
		} catch (MissingProperty e) {
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return robotname;
	}
	
	public void addSubcomponent(Subcomponent s) {
		contains.add(s);
		s.setParent(this);
	}
	
	public String vehicleTypeAsString() {
		if (vtype == VehicleType.LIGHT_DRONE) return "LIGHT_DRONE";
		if (vtype == VehicleType.HEAVY_DRONE) return "HEAVY_DRONE";
		return "KAYAK";
	}

	// TODO: should use Optional for these. And the same for MotionSource etc
	public Sensor getSensor(SensorType st) {
		List<Subcomponent> sensors = contains.stream()
				.filter(sc -> sc instanceof Sensor)
				.filter(sc -> ((Sensor)sc).getType() == st)
				.collect(Collectors.toList());
		
		if (sensors.size() > 0) {
			return (Sensor) sensors.get(0);
		} else return null;
	}
	
	public List<Sensor> getAllSensors() {
		return (List<Sensor>)(contains.stream()
				.filter(sc -> sc instanceof Sensor)
				.map(sc -> (Sensor)sc)
				.collect(Collectors.toList()));
	}

	public Optional<MotionSource> getMotionSource() {
		List<Subcomponent> mss = contains.stream()
				.filter(sc -> sc instanceof MotionSource)
				.collect(Collectors.toList());
		if (mss.size() > 0) {
			return Optional.of((MotionSource)mss.get(0));
		} else return Optional.empty();
	}
	
	public List<Battery> getBatteries() {
		List<Battery> mss = contains.stream()
				.filter(sc -> sc instanceof Battery)
				.map(sc -> (Battery)sc)
				.collect(Collectors.toList());
		return mss;
	}

	public Optional<Sensor> getFirstSensor() {
		List<Sensor> sensors = getAllSensors();
		if (sensors.size() > 0) {
			return Optional.of(sensors.get(0));
		} else {
			return Optional.empty();
		}
	}

	public void setupRobotEnergy() {
		// Enumerate all the Battery devices for the robot
		for (Battery b : getBatteries()) {
			currentEnergy += b.getMaxEnergy();
			startingEnergy += b.getMaxEnergy();
			System.out.println("Robot " + getName() + " found battery of capacity " + b.getMaxEnergy());
		}
	}
	
	public double getEnergyProportionRemaining() {
		return ((double)currentEnergy) / ((double)startingEnergy);
	}
	
	public double getEnergyRemaining() {
		return currentEnergy;
	}

	/** This should only be called by the core as it does 
	 * not handle sending out an energy update or stop the robot 
	 * if energy is exhausted 
	 * **/  
	public void _depleteEnergy(double fixedEnergyLoss) {
		currentEnergy -= fixedEnergyLoss;
		currentEnergy = Math.max(currentEnergy, 0.0);
		System.out.println("depleteEnergy: robot " + getName() + " experienced energy loss of " + fixedEnergyLoss + ",currentEnergy = " + currentEnergy);
	}
	
	public void setEnergyRemaining(double newEnergy) {
		currentEnergy = newEnergy;
	}
	
	public boolean noEnergyRemaining() {
		return (currentEnergy <= 0.0);
	}
}
