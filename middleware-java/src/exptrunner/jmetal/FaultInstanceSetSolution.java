package exptrunner.jmetal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.uma.jmetal.solution.*;

import atlasdsl.*;
import atlasdsl.faults.Fault;
import atlassharedclasses.FaultInstance;


public class FaultInstanceSetSolution implements Solution<FaultInstance> {
	private static final long serialVersionUID = 1L;
	// Variable is a FaultInstance
	// Mutation also does the length change

	// TODO: better way of propagating this constant
	private static final double MAX_SPEED_VALUE = 5.0;
	
	private Mission mission;
	private boolean actuallyRun;
	private double exptRunTime;
	private double outputViolations;
	
	private Map<Object,Object> attributes = new HashMap<Object,Object>();
	
	private List<FaultInstance> contents = new ArrayList<FaultInstance>();
	private Object attrib;
	
	FaultInstanceSetSolution(Mission mission, String exptTag, boolean actuallyRun, double exptRunTime) {
		this.mission = mission;
		this.actuallyRun = actuallyRun;
		this.exptRunTime = exptRunTime;
	}
	
	FaultInstanceSetSolution(FaultInstanceSetSolution other) {
		this.mission = other.mission;
		this.actuallyRun = other.actuallyRun;
		this.exptRunTime = other.exptRunTime;
		this.contents = new ArrayList<FaultInstance>(contents);
	}
	
	public void setObjective(int index, double value) {
		if (index == 0) {
			outputViolations = value;
		}
	}

	public double getObjective(int index) {
		if (index == 0) {
			return outputViolations;
		} else {
			return 0.0;
		}
	}

	public double[] getObjectives() {
		double [] res =  new double [1];
		res[0] = getObjective(0);
		return res;
	}

	public FaultInstance getVariable(int index) {
		return contents.get(index);
	}

	public List<FaultInstance> getVariables() {
		return contents;
	}

	public void setVariable(int index, FaultInstance variable) {
		contents.set(index, variable);
	}

	public double[] getConstraints() {
		double [] res =  new double [1];
		res[0] = getConstraint(0);
		return res;
	}
	
	double faultInstanceIntensity(FaultInstance fi) {
		// Assume the intensity is always 1 unless otherwise
		double intensity = 1.0;
		Fault f = fi.getFault();
		Optional<String> extraData_opt = fi.getExtraDataOpt();
		if (extraData_opt.isPresent()) {
			String extraData = extraData_opt.get();
			double exValue = Double.parseDouble(extraData);
			// The speed faults intensity is relative to the max value
			if (f.getName().contains("SPEEDFAULT")) {
				intensity = exValue / MAX_SPEED_VALUE;
			}
		}
		return intensity;
	}
	
	private double totalActiveFaultTimeLengthScaledByIntensity() {
		double total = 0.0;
		for (FaultInstance fi : contents) {
			if (fi.isActive()) {
				total += faultInstanceIntensity(fi) * (fi.getEndTime() - fi.getStartTime());
			}
		}
		return total;
	}

	public double faultCostProportion() {
		return 1 - ((totalActiveFaultTimeLengthScaledByIntensity() / (contents.size() * exptRunTime)));
	}

	public double getConstraint(int index) {
		return faultCostProportion();
	}

	public void setConstraint(int index, double value) {
		
	}

	public int getNumberOfVariables() {
		return contents.size();
	}

	public int getNumberOfObjectives() {
		return 1;
	}

	public int getNumberOfConstraints() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Solution<FaultInstance> copy() {
		FaultInstanceSetSolution copy = new FaultInstanceSetSolution(this);
		return copy;
	}
	
	public List<FaultInstance> getFaultInstances() {
		return contents;
	}
	
	public void setContents(int index, FaultInstance fi) {
		contents.add(index, fi);
	}

	public int numberOfFaults() {
		return contents.size();
	}

	public void setAttribute(Object id, Object value) {
		attributes.put(id,value);
	}

	public Object getAttribute(Object id) {
		return attributes.get(id);
	}

	public boolean hasAttribute(Object id) {
		return attributes.containsKey(id);
	}

	public Map<Object, Object> getAttributes() {
		return attributes;
	}
	
	
}