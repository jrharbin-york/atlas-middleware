package fuzzingengine;

import atlasdsl.Mission;

public class TimeSpec {
	private double startTime;
	private double endTime;
	private Mission mission;
	
	private final boolean ERROR_IF_MISSION_TIME_EXCEEDED = false;
	
	private void checkTiming() throws InvalidTimeSpec {
		if (endTime < startTime) {
			throw new InvalidTimeSpec("endTime < startTime");
		}		
		
		if (startTime < 0.0) {
			throw new InvalidTimeSpec("startTime < 0.0");
		}
		
		if (endTime > mission.getEndTime()) {
			if (ERROR_IF_MISSION_TIME_EXCEEDED) {
				throw new InvalidTimeSpec("endTime > mission end time");
			} else {
				System.out.println("Fuzzing timespec endTime = " + endTime + " is beyond mission end time - correcting to mission end time");
				endTime = mission.getEndTime();
			}
		}
	}
	
	public TimeSpec(Mission mission, double startTime, double endTime) throws InvalidTimeSpec {
		this.mission = mission;
		this.startTime = startTime;
		this.endTime = endTime;
		checkTiming();
	}
	
	public double getLength() {
		return endTime - startTime;
	}
	
	public double getStartTime() {
		return startTime;
	}
	
	public double getEndTime() {
		return endTime;
	}
}