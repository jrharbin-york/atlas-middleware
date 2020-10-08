package atlasdsl;

import atlassharedclasses.Point;

// This should be part of the Region layer
public class EnvironmentalObject extends Point {
	private static int autoLabelCounter = 1;
	
	private int label;
	private int width = 5;
	private boolean isHazard;
	private String color;
	
	public EnvironmentalObject(Point location, boolean isHazard) {
		super(location.getX(), location.getY());
		this.label = autoLabelCounter++;
		this.isHazard = isHazard;
		
		if (isHazard) {
			color = "red";
		} else {
			color = "green";
		}
	}
	
	public EnvironmentalObject(int label, Point location, boolean isHazard) {
		super(location.getX(), location.getY());
		this.label = label;
		this.isHazard = isHazard;
		
		if (isHazard) {
			color = "red";
		} else {
			color = "green";
		}
	}
	
	public int getLabel() {
		return label;
	}
	
	public String toString() {
		String typeStr = "benign";
		if (isHazard) typeStr = "hazard";
		return "hazard=" + super.toString() +  ",label=" + label + ",type=" + typeStr + ",color=" + color + ",width=" + width;			
	}
	

}
