package atlassharedclasses;

public class Point {
	private double x;
	private double y;
	private double z = 0;
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	// Empty constructor since Point may be used in SonarDetection
	// and things serialised by Jackson need it
	public Point() {
		
	}
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public String toString() {
		return "x=" + x + ",y=" + y;
	}
	
	public String toStringBareCSV() {
		return x + "," + y;
	}
	
	public double distanceSqrTo(Point other) {
		return Math.pow((other.x - this.x),2.0) + Math.pow((other.y - this.y),2.0); 
	}
	
	public double distanceTo(Point other) {
		return Math.sqrt(distanceSqrTo(other));
	}

	public Point add(Point dp) {
		return new Point(this.x + dp.x, this.y + dp.y, this.z + dp.z);
	}

	public Point sub(Point dp) {
		return new Point(this.x - dp.x, this.y - dp.y, this.z - dp.z);
	}
	
	public double distanceSqrTo(double otherx, double othery) {
		return Math.pow((otherx - this.x),2.0) + Math.pow((othery - this.y),2.0); 
	}
	
	public double distanceTo(double otherx, double othery) {
		return Math.sqrt(distanceSqrTo(otherx, othery));
	}
}
