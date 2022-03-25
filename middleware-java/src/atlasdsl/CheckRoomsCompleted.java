package atlasdsl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import atlasdsl.GoalResult.GoalResultStatus;
import atlassharedclasses.Point;
import middleware.core.ATLASCore;

// This goal needs to log the final rooms completed - so a metric can read the count

public class CheckRoomsCompleted extends GoalAction {

	private static final double RETURN_HOME_DIST_THRESHOLD = 0.1;

	private Mission mission;
	private double completionTime;

	private Map<String, List<Integer>> roomsPending = new HashMap<String, List<Integer>>();
	Map<String, List<RoomServicedRecord>> roomsServicedByRobots = new HashMap<String, List<RoomServicedRecord>>();

	private HashMap<String, Double> checkReturnHomeTime = new HashMap<String, Double>();

	FileWriter roomsOutput;

	private boolean writtenYet = false;

	private ATLASCore core;

	public class RoomServicedRecord {
		private String robotName;
		private Integer room;
		private double time;

		public RoomServicedRecord(String robotName, Integer room, double time) {
			this.robotName = robotName;
			this.room = room;
			this.time = time;
		}

		public String toFileLine() {
			return this.robotName + "," + this.room + "," + this.time + "\n";
		}
	}

	private void logToFile(RoomServicedRecord rsr) {
		try {
			roomsOutput.write(rsr.toFileLine());
			roomsOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void registerRoomCompleted(String robotName, Integer room, double time) {
		RoomServicedRecord rsr = new RoomServicedRecord(robotName, room, time);
		logToFile(rsr);

		List<RoomServicedRecord> servicedByRobot;
		if (roomsServicedByRobots.containsKey(robotName)) {
			servicedByRobot = roomsServicedByRobots.get(robotName);
		} else {
			servicedByRobot = new ArrayList<RoomServicedRecord>();
		}

		servicedByRobot.add(rsr);
		roomsServicedByRobots.put(robotName, servicedByRobot);

		List<Integer> roomsForRobot = roomsPending.get(robotName);
		roomsForRobot.remove(room);
		roomsPending.put(robotName, roomsForRobot);
		System.out.println("CheckRoomsCompleted: registerRoomCompleted: robot " + robotName + " completed room " + room);
		System.out.println("CheckRoomsCompleted: roomsPending for robot " + robotName + " is " + roomsPending.toString());
	}

	protected Optional<GoalResult> test(Mission mission, GoalParticipants participants) {
		double time = core.getTime();
		boolean isEmpty = true;
		for (Map.Entry<String, List<Integer>> e : roomsPending.entrySet()) {
			List<Integer> rooms = e.getValue();
			if (rooms.size() > 0) {
				isEmpty = false;
			}
		}

		// If completion time is exceeded, write the results file
		if ((time > completionTime) && !writtenYet) {
			writeResultsOut();
		}
		
		if (isEmpty) {
			return Optional.of(new GoalResult(GoalResultStatus.COMPLETED));
		} else {
			return Optional.empty();
		}
	}
	
	private void writeResultsOut() {
		writtenYet  = true;
		try {
			FileWriter output = new FileWriter("logs/finishMissionTime.log");
			for (Robot r : mission.getAllRobots()) {
				String robotName = r.getName();
				// Assume completion time is mission completion time
				double completionTimeForRobot = completionTime;
				if (checkReturnHomeTime.containsKey(robotName)) {
					completionTimeForRobot = checkReturnHomeTime.get(robotName);
				} 
				output.write(robotName + "," + completionTimeForRobot);
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void setup(ATLASCore core, Mission mission, Goal g) throws GoalActionSetupFailure {
		this.core = core;
		this.mission = mission;
		completionTime = mission.getEndTime();

		try {
			roomsOutput = new FileWriter("logs/roomsCompleted.log");
		} catch (IOException e) {
			e.printStackTrace();
			throw new GoalActionSetupFailure(this, "CheckRoomsCompleted: Log file for roomsCompleted not set up");
		}

		// TODO Encode all rooms in roomsPending from the DSL - here they are hardcoded

		List<Integer> rooms_tb0 = new ArrayList<Integer>();
		rooms_tb0.add(1);
		rooms_tb0.add(2);
		rooms_tb0.add(6);

		List<Integer> rooms_tb1 = new ArrayList<Integer>();
		rooms_tb1.add(3);
		rooms_tb1.add(5);
		rooms_tb1.add(7);

		List<Integer> rooms_tb2 = new ArrayList<Integer>();
		rooms_tb2.add(4);
		rooms_tb2.add(8);
		rooms_tb2.add(9);

		roomsPending.put("tb3_0", rooms_tb0);
		roomsPending.put("tb3_1", rooms_tb1);
		roomsPending.put("tb3_2", rooms_tb2);

		// Set up watcher that will be triggered on this simulator variable
		core.setupSimVarWatcher("/roomCompleted", (svar, robotName, val) -> {
			double time = core.getTime();
			if (val instanceof Integer) {
				registerRoomCompleted(robotName, (Integer) val, time);
			}
			if (val instanceof String) {
				System.out.println("CheckRoomsCompleted: registerRoom debugging: string is " + (String) val);
				Integer i = Integer.parseInt((String) val);
				registerRoomCompleted(robotName, i, time);
			}
		});

		core.setupPositionWatcher((gps) -> {
			if (g.isReady(core.getTime())) {
				String rname = gps.getRobotName();
				checkMissionCompletedForRobot(rname, gps.getPoint(), core.getTime());
			}
		});
	}

	private void checkMissionCompletedForRobot(String robotName, Point newLoc, double time) {
		List<Integer> roomsToDo = roomsPending.get(robotName);
		// Only if all rooms are completed
		if (roomsToDo != null && roomsToDo.size() == 0) {
			if (!checkReturnHomeTime.containsKey(robotName)) {
				try {
					Point origLoc = mission.getRobot(robotName).getPointComponentProperty("startLocation");
					if (newLoc.distanceTo(origLoc) < RETURN_HOME_DIST_THRESHOLD) {
						System.out.println("CheckRoomsCompleted: Robot " + robotName + " registered ");
						checkReturnHomeTime.put(robotName, time);
						
					}
				} catch (MissingProperty e) {
					e.printStackTrace();
				}
			}
		}
	}
}
