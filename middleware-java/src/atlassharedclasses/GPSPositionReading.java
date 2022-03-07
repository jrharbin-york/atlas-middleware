package atlassharedclasses;

public class GPSPositionReading extends SensorInfo {
	private double x;
	private double y;
	private double z;
	//private double speed;
	//Speed readings now in SpeedReading
	private String robotName;
	
	// There has to be a default constructor to allow the class to be 
	// serialised.
	public GPSPositionReading() {
		
	}
	
	public GPSPositionReading(double x, double y, String robotName) {
		this.x = x;
		this.y = y;
		//this.speed = speed;
		this.robotName = robotName;
	}
	
	public GPSPositionReading(double x, double y, double z, String robotName) {
		this.x = x;
		this.y = y;
		this.z = z;
		//this.speed = speed;
		this.robotName = robotName;
	}
	
	public GPSPositionReading(Point pos, String robotName) {
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
		//this.speed = speed;
		this.robotName = robotName;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	public String getRobotName() {
		return robotName;
	}

	public double getSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}
}
