package carsspecific.moos.moosmapping;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import atlasdsl.EnvironmentalObstacle;
import atlassharedclasses.Region;

public class UFldObstacleSim extends MOOSProcess {
	private List<EnvironmentalObstacle> obstacles = new ArrayList<EnvironmentalObstacle>();
	// Defaults from the existing file
	private double minRange = 20.0;
	private double minSize = 6;
	private double maxSize = 10;
	private Region enclosingRegion;
	
	
	private void setStandardProperties(Region enclosingRegion) {
		setProperty("AppTick", 1);
		setProperty("CommsTick", 1);
		setProperty("obstacle_file", "obstacle.txt");
		setProperty("poly_fill_color", "invisible");
		// TODO: is this needed?
		setProperty("reset_interval", 250);
		setProperty("reset_range", 10);
		this.enclosingRegion = enclosingRegion;
	}
	
	public UFldObstacleSim(MOOSCommunity parent, Region enclosingRegion) {
		super("uFldObstacleSim", parent);
		setStandardProperties(enclosingRegion);
	}
	
	public UFldObstacleSim(MOOSCommunity parent, Region enclosingRegion, double minRange, double minSize, double maxSize) {
		super("uFldObstacleSim", parent);
		setStandardProperties(enclosingRegion);
		this.minRange = minRange;
		this.maxSize = maxSize;
		this.minSize = minSize;
	}
	
	public void addDetectionObject(EnvironmentalObstacle eo) {
		obstacles.add(eo);
	}
	
	public void generateCustomCode(MOOSFiles mf) throws IOException {
		FileWriter fw = mf.getOpenFile("obstacle.txt");
		
		fw.write("region = pts={" + enclosingRegion.pointsListForObstacleFile() + "}\n");
		for (EnvironmentalObstacle eo : obstacles) {
			fw.write("min_range=" + minRange + "\n");
			fw.write("min_size =" + minSize + "\n");
			fw.write("max_size =" + maxSize + "\n");
			String pointsList = eo.pointsListForObstacleFile();
			fw.write("poly = pts={" + pointsList + "},label=" + eo.getLabel() + "\n");
		}
	}
}