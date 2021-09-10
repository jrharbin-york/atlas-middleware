package atlasdsl;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import atlasdsl.GoalResult.GoalResultStatus;
import atlassharedclasses.Point;
import middleware.core.ATLASCore;
import middleware.logging.ATLASLog;

// Assume StayInRegion requires a staticGoalRegion currently
public class MaintainDistanceFrom extends GoalAction {
	private ATLASCore core;
	private boolean stopOnFirstViolation = false;
	private double clearanceDistance;
	
	private final String topicName = "/airframe_clearance";
	
	private int violationIncidents = 0;
	private final double TIME_THRESHOLD = 5.0;
	
	private HashMap<String,Double> robotLastViolationTime = new HashMap<String,Double>();
	
	public class MissingRegion extends Exception {
		private static final long serialVersionUID = 1L;
	}

	public MaintainDistanceFrom() {
		// The region is determined during the setup method
	}
	
	public MaintainDistanceFrom(double distance) {
		this.clearanceDistance = distance;
	}
	
	public boolean violationReadyToLog(String robotName, double currentTime) {
		if (!robotLastViolationTime.containsKey(robotName)) {
			robotLastViolationTime.put(robotName, Double.MIN_VALUE);
		}
		
		double lastTime = robotLastViolationTime.get(robotName);
		if ((currentTime - lastTime) >= TIME_THRESHOLD) {
			robotLastViolationTime.put(robotName, currentTime);
			return true;
		} else {
			return false;
		}
	}
	
	private Optional<GoalResult> logIfReady(Robot r, double dist, double clearance) {
		double time = core.getTime();
		if (violationReadyToLog(r.getName(), time)) {
			violationIncidents++;
		
			GoalResultStatus grs;
			if (stopOnFirstViolation) {
				grs = GoalResultStatus.VIOLATED;
			} else {
				grs = GoalResultStatus.CONTINUE;
			}
							
			ATLASLog.logGoalMessage(this, violationIncidents + "," + time + "," + r.getName() + "," + dist + "," + clearance);
			GoalResult gr = new GoalResult(grs);
			GoalResultField gf = new StringResultField("violatingRobot", r.getName());
			GoalResultField pf = new DoubleResultField("dist", dist);
			gr.addField(gf);
			gr.addField(pf);
			return Optional.of(gr);
		} else {
			return Optional.empty();
		}
	}
	
	protected Optional<GoalResult> test(Mission mission, GoalParticipants participants) {
		List<Robot> robots = participants.getParticipants();
		//try {
			for (Robot r : robots) {
				// TODO: test the goal variable supplied for the airframe clearance
				// look up this variable
				// test its value against the threshold
				Object gv = core.getGoalVariable(r.getName(), "/airframe_clearance");
				if (gv != null) {
					edu.wpi.rail.jrosbridge.messages.Message m = (edu.wpi.rail.jrosbridge.messages.Message)gv;
					String typ = m.getMessageType();
					JsonObject jobj = m.toJsonObject();
					JsonNumber n = (JsonNumber)jobj.get("data");
					double distVal = n.doubleValue();
					System.out.println("robot " + r.getName() + ",distVal = " + distVal);
					
					if (distVal < clearanceDistance) {
						return logIfReady(r, distVal, clearanceDistance);
					}
				}
				
				// Check how the goal variable subscriptions are done
			}
		//} catch (MissingProperty mp) {
			// TODO: some properties missing for robots - declare that superclass throws GoalTestFailure
			// or similar in this case.
		//	return Optional.empty();
		//}
		return Optional.empty();
	}

	protected void setup(ATLASCore core, Mission mission, Goal g) throws GoalActionSetupFailure {
		this.core = core;
		// TODO: Look up the associated goal variable
		Optional<GoalRegion> gr_o = g.getGoalRegion();
	}
}
