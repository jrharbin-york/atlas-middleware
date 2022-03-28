package exptrunner.metrics;

//protected region customHeaders on begin
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import atlasdsl.Mission;
import fuzzingengine.FuzzingKeySelectionRecord;
//protected region customHeaders end

public class MissionCompletionTime extends OfflineMetric {
//protected region customFunction on begin
//protected region customFunction end

	public Double computeFromLogs(List<FuzzingKeySelectionRecord> recs, String logDir, Mission mission)
			throws MetricComputeFailure {
		// Implement the metric here
		// protected region userCode on begin
		
		String filename = logDir + "/finishMissionTime.log";
		System.out.println("MissionCompletionTime metric filename = " + filename);
		Scanner reader;
		double finishTime = -1.0;
		try {
			reader = new Scanner(new File(filename));
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String [] fields = line.split(",");
				String robotName = fields[0];
				double finishTimeForRobot = Double.parseDouble(fields[1]);
				System.out.println("robot " + robotName + " finish time = " + finishTimeForRobot);
				finishTime = Math.max(finishTime, finishTimeForRobot);
			}
			reader.close();
			System.out.println("finishTime = " + finishTime);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return Double.valueOf(finishTime);
		// protected region userCode end
	}

	public MetricDirection optimiseDirection() {
		// protected region userCode on begin
		return Metric.MetricDirection.HIGHEST;
		// protected region userCode end
	}
}
