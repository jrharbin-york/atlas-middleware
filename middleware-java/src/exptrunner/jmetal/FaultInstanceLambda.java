package exptrunner.jmetal;

import atlassharedclasses.FaultInstance;

public interface FaultInstanceLambda {
	boolean op(FaultInstance fi);
}
