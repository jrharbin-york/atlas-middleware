package atlasdsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import atlasdsl.GoalResult.GoalResultStatus;
import middleware.core.ATLASCore;

public class CheckRoomsCompleted extends GoalAction {

	private Map<String,List<Integer>> roomsPending = new HashMap<String,List<Integer>>();
	
	private void registerRoomCompleted(String robotName, Integer val) {
		List<Integer> roomsForRobot = roomsPending.get(robotName);
		roomsForRobot.remove(val);
		roomsPending.put(robotName, roomsForRobot);
	}
	
	protected Optional<GoalResult> test(Mission mission, GoalParticipants participants) {
		boolean isEmpty = true;
		for (Map.Entry<String,List<Integer>> e : roomsPending.entrySet()) {
			List<Integer> rooms = e.getValue();
			if (rooms.size() > 0) {
				isEmpty = false;
			}
		}
		
		if (isEmpty) {
			return Optional.of(new GoalResult(GoalResultStatus.COMPLETED));
		} else {
			return null;
		}
	}

	protected void setup(ATLASCore core, Mission mission, Goal g) throws GoalActionSetupFailure {
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
		core.setupSimVarWatcher("/roomsCompleted", (svar, robotName, val) -> 
		{
			if (val instanceof Integer) {
				registerRoomCompleted(robotName, (Integer)val);
			}
		});
	}
}
