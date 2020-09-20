package exptrunner.jmetal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// This is for jmetal 5
import org.uma.jmetal.solution.*;

import atlasdsl.*;
import atlassharedclasses.FaultInstance;
import exptrunner.ExptParams;

public class FaultInstanceSetSolution implements Solution<FaultInstance> {
	private static final long serialVersionUID = 1L;
	// Variable is a FaultInstance
	// Mutation also does the length change
	
	private Mission mission;
	private ExptParams eparams;
	private boolean actuallyRun;
	
	private double exptRunTime = 1200.0;
	
	private List<FaultInstance> contents = new ArrayList<FaultInstance>();
	
	private int runCount = 0;
	
	FaultInstanceSetSolution(Mission mission, String exptTag, ExptParams eparams, boolean actuallyRun, double exptRunTime) {
		this.mission = mission;
		this.eparams = eparams;
		this.actuallyRun = actuallyRun;
		this.exptRunTime = exptRunTime;
	}
	
	FaultInstanceSetSolution(FaultInstanceSetSolution other) {
		this.mission = other.mission;
		this.eparams = other.eparams;
		this.actuallyRun = other.actuallyRun;
		this.exptRunTime = other.exptRunTime;
		this.contents = new ArrayList<FaultInstance>(contents);
	}
	
	public void setObjective(int index, double value) {
		// TODO Auto-generated method stub
	}

	public double performATLASExperiment() {
		// Generate the fault instance file corresponding to this
		// Call RunExperiment.doExperiment with
		String exptTag = "exptGA-" + (runCount++);
		// TODO: change exptrunner to use the fault instance set directly - not via file
		// TODO: exptrunner has to return its double value 
		try {
			return RunExperiment.doExperiment(mission, exptTag, contents , actuallyRun, exptRunTime);
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
			// TODO: better way to handle failure during experiment
			return 0.0;
		}
	}
	
	// TODO: What are objectives/attributes?
	public double getObjective(int index) {
		// invoke the code to 
		if (index == 0) {
			return performATLASExperiment();
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
		// TODO Auto-generated method stub
		return null;
	}

	public double getConstraint(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setConstraint(int index, double value) {
		// TODO Auto-generated method stub
		
	}

	public int getNumberOfVariables() {
		return contents.size();
	}

	public int getNumberOfObjectives() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumberOfConstraints() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Solution<FaultInstance> copy() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setAttribute(Object id, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getAttribute(Object id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasAttribute(Object id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

}