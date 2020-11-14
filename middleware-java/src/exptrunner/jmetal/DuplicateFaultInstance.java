package exptrunner.jmetal;

import atlassharedclasses.FaultInstance;

public class DuplicateFaultInstance extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FaultInstance fi;
	public DuplicateFaultInstance(FaultInstance fi) {
		this.fi = fi;
	}
	
	public FaultInstance getDuplicateFaultInstance() {
		return fi;
	}
}
