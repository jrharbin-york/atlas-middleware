package middleware.core;

import atlasdsl.SimulatorVariable;

public interface SimVarUpdateLambda {
	public void op(SimulatorVariable sv, String robotName, Object val);
}