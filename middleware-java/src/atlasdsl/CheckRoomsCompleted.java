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

	private static final double RETURN_HOME_DIST_THRESHOLD = 0.5;

	private Mission mission;
	private double completionTime;

	private Map<String, List<Integer>> roomsPending = new HashMap<String, List<Integer>>();
	Map<String, List<RoomServicedRecord>> roomsServicedByRobots = new HashMap<String, List<RoomServicedRecord>>();

	private HashMap<String, Double> workAssignedTime = new HashMap<String, Double>();
	private HashMap<String, Double> checkReturnHomeTime = new HashMap<String, Double>();

	FileWriter roomsOutput;

	private boolean writtenYet = false;
	
	private double energyPerRoom;

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
	
	public CheckRoomsCompleted(double energyPerRoom) {
		this.energyPerRoom = energyPerRoom;
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
		System.out.println("CheckRoomsCompleted: roomsForRobot " + robotName + " is " + roomsForRobot.toString());
	}
	
	private boolean registerRoomCompletedIfEnergy(String robotName, Integer val, double time) {
		if (core.getRobotEnergyRemaining(robotName) > energyPerRoom) {
			core.depleteEnergyOnRobot(robotName, energyPerRoom);
			registerRoomCompleted(robotName, val, time);
			return true;
		} else {
			core.depleteEnergyOnRobot(robotName, energyPerRoom);
			System.out.println("CheckRoomsCompleted: registerRoomCompleted: robot " + robotName + " lacked energy to complete room " + val);
			return false;
		}
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
				output.write(robotName + "," + completionTimeForRobot + "\n");
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String stripDoubleQuotes(String string) {
		if (string.length() >= 2 && string.charAt(0) == '"' && string.charAt(string.length() - 1) == '"')
		{
			string = string.substring(1, string.length() - 1);
		}
		return string;
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

		// Rooms now set to /rooms

		// Set up watcher that will be triggered on this simulator variable
		core.setupSimVarWatcher("/roomCompleted", (svar, robotName, val) -> {
			double time = core.getTime();
			if (val instanceof Integer) {
				return registerRoomCompletedIfEnergy(robotName, (Integer) val, time);
			}
			if (val instanceof String) {
				System.out.println("CheckRoomsCompleted: registerRoom debugging: string is " + (String) val);
				Integer i = Integer.parseInt((String) val);
				return registerRoomCompletedIfEnergy(robotName, i, time);
			}
			
			return false;
		});
		
		// Set up watcher that will be triggered on this simulator variable
		core.setupSimVarWatcher("/rooms", (svar, robotName, val) -> {
			if (!workAssignedTime.containsKey(robotName)) {
				double time = core.getTime();
				System.out.println("CheckRoomsCompleted: work assigned for robot at " + time);
				workAssignedTime.put(robotName, time);
			}
			
			List<Integer> roomsForRobot = roomsPending.get(robotName);
			if (roomsForRobot == null) {
				roomsForRobot = new ArrayList<Integer>();
			}
			
			String roomString = stripDoubleQuotes((String)val);
			System.out.println("CheckRoomsCompleted: /rooms for robot " + robotName + " is " + roomString);
			
			String [] roomStrs = roomString.split(",");
			for (String roomStr : roomStrs) {
				Integer i = Integer.parseInt(roomStr);
				roomsForRobot.add(i);
				System.out.println("CheckRoomsCompleted: adding room assignment for robot " + robotName + " - room " + i);
			}
			roomsPending.put(robotName,  roomsForRobot);
			
			return true;
			
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
		if (workAssignedTime.containsKey(robotName) && roomsToDo != null && roomsToDo.size() == 0) {
			if (!checkReturnHomeTime.containsKey(robotName)) {
				try {
					Point origLoc = mission.getRobot(robotName).getPointComponentProperty("startLocation");
					if (newLoc.distanceTo(origLoc) < RETURN_HOME_DIST_THRESHOLD) {
						System.out.println("CheckRoomsCompleted: Robot " + robotName + " registered mission completed at " + time);
						checkReturnHomeTime.put(robotName, time);
						
					}
				} catch (MissingProperty e) {
					e.printStackTrace();
				}
			}
		}
	}
}
