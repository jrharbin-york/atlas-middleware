package middleware.core;

import atlasdsl.SimulatorVariable;

public interface SimVarUpdateLambda {
	public boolean op(SimulatorVariable sv, String robotName, Object val);
}