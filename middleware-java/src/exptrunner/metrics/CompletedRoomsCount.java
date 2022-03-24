package exptrunner.metrics;

//protected region customHeaders on begin
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import atlasdsl.Mission;
import fuzzingengine.FuzzingKeySelectionRecord;
//protected region customHeaders end

public class CompletedRoomsCount extends OfflineMetric {
//protected region customFunction on begin
//protected region customFunction end

	public Double computeFromLogs(List<FuzzingKeySelectionRecord> recs, String logDir, Mission mission)
			throws MetricComputeFailure {
		// Implement the metric here
		// protected region userCode on begin
		int completedRooms = 0;
		String filename = logDir + "/roomsCompleted.log";
		System.out.println("CompletedRoomsCount metric filename = " + filename);
		Scanner reader;
		try {
			reader = new Scanner(new File(filename));
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				completedRooms++;
			}
			reader.close();
			System.out.println("completedRooms = " + completedRooms);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return Double.valueOf(completedRooms);
		// protected region userCode end
	}

	public MetricDirection optimiseDirection() {
		// protected region userCode on begin
		return Metric.MetricDirection.HIGHEST;
		// protected region userCode end
	}
}
