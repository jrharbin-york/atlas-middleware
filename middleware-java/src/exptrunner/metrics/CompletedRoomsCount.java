package exptrunner.metrics;

//protected region customHeaders on begin
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		
		// The key is the room, the value the first robot to examine it
		Map<Integer,String> roomsRobot = new HashMap<Integer,String>();
		int completedRooms = -1;
		
		String filename = logDir + "/roomsCompleted.log";
		System.out.println("CompletedRoomsCount metric filename = " + filename);
		Scanner reader;
		try {
			reader = new Scanner(new File(filename));
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String [] fields = line.split(",");
				String robotName = fields[0];
				Integer room = Integer.parseInt(fields[1]);
				Double time = Double.parseDouble(fields[2]);
				
				if (!roomsRobot.containsKey(room) && time < mission.getEndTime()) {
					roomsRobot.put(room, robotName);
				}
			}
			reader.close();
			
			completedRooms = roomsRobot.size();
			System.out.println("completedRooms = " + completedRooms);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return (double)completedRooms;
		// protected region userCode end
	}

	public MetricDirection optimiseDirection() {
		// protected region userCode on begin
		return Metric.MetricDirection.HIGHEST;
		// protected region userCode end
	}
}
