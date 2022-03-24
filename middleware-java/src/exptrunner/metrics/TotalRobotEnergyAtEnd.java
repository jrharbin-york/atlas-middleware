package exptrunner.metrics;

//protected region customHeaders on begin
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import atlasdsl.Mission;
import fuzzingengine.FuzzingKeySelectionRecord;
//protected region customHeaders end

public class TotalRobotEnergyAtEnd extends OfflineMetric {
//protected region customFunction on begin
//protected region customFunction end

	public Double computeFromLogs(List<FuzzingKeySelectionRecord> recs, String logDir, Mission mission)
			throws MetricComputeFailure {
		// Implement the metric here
		// protected region userCode on begin
		double energyAtEnd = 0;
		String filename = logDir + "/robotEnergyAtEnd.log";
		System.out.println("TotalRobotEnergyAtEnd metric filename = " + filename);
		Scanner reader;
		try {
			// Since it is total energy at end, need to sum the energy
			// of individual robots
			reader = new Scanner(new File(filename));
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String [] fields = line.split(",");
				double robotEnergy = Double.parseDouble(fields[1]);
				energyAtEnd = energyAtEnd + robotEnergy;
			}
			reader.close();
			System.out.println("energyAtEnd = " + energyAtEnd);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return Double.valueOf(energyAtEnd);
		// protected region userCode end
	}

	public MetricDirection optimiseDirection() {
		// protected region userCode on begin
		return Metric.MetricDirection.HIGHEST;
		// protected region userCode end
	}
}
