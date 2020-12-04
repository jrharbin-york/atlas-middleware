package middleware.core;

import atlassharedclasses.*;

public interface PositionUpdateLambda {
	public void op(GPSPositionReading p);
}