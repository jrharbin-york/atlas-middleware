package atlassharedclasses;

public class GPSPositionReading extends SensorInfo {
	private double x;
	private double y;
	private double speed;
	private String robotName;
	
	// There has to be a default constructor to allow the class to be 
	// serialised.
	public GPSPositionReading() {
		
	}
	
	public GPSPositionReading(double x, double y, double speed, String robotName) {
		this.x = x;
		this.y = y;
		this.speed = speed;
		this.robotName = robotName;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public String getRobotName() {
		return robotName;
	}

	public double getSpeed() {
		return speed;
	}
}
