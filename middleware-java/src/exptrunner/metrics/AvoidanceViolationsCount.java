package exptrunner.metrics;

//protected region customHeaders on begin
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import atlasdsl.Mission;
import fuzzingengine.FuzzingKeySelectionRecord;
//protected region customHeaders end

public class AvoidanceViolationsCount extends OfflineMetric {
//protected region customFunction on begin
//protected region customFunction end

	public Double computeFromLogs(List<FuzzingKeySelectionRecord> recs, String logDir, Mission mission)
			throws MetricComputeFailure {
		// Implement the metric here
		// protected region userCode on begin
		int avoidanceViolations = 0;
		String filename = logDir + "/goalLog.log";
		System.out.println("OutsideOfOuterRegionViolations filename = " + filename);
		Scanner reader;
		try {
			reader = new Scanner(new File(filename));
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String[] fields = line.split(",");
				String goalClass = fields[0];
				if (goalClass.equals("atlasdsl.AvoidOthers")) {
					avoidanceViolations++;
				}
			}

			reader.close();
			System.out.println("avoidanceViolations = " + avoidanceViolations);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return Double.valueOf(avoidanceViolations);
		// protected region userCode end
	}

	public MetricDirection optimiseDirection() {
		// protected region userCode on begin
		return Metric.MetricDirection.HIGHEST;
		// protected region userCode end
	}
}
