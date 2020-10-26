package atlasdsl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import atlassharedclasses.*;

public class EnvironmentalObstacle {
	private List<Point> polygon = new ArrayList<Point>();
	private String label;
	
	public EnvironmentalObstacle(String label, List<Point> polygon) {
		this.polygon = polygon;
		this.label = label;  
	}			
	
	public String pointsListForObstacleFile() {
		return polygon.stream()
				.map(p -> p.getX() + "," + p.getY())
				.collect(Collectors.joining(":"));
	}
	
	public String pointsList() {
		return polygon.stream()
				.map(p -> p.getX() + "," + p.getY())
				.collect(Collectors.joining(":"));
	}
	
	public List<Point> getPoints() {
		return polygon;
	}
	
	public String getLabel() {
		return label;
	}
}
