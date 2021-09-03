package fuzzingengine;

import middleware.core.ATLASCore;

public class FuzzingConditionStartEnd extends FuzzingTimeSpecification {
	FuzzingCondition startCond;
	FuzzingCondition endCond;
	
	boolean active = false;
	
	double endTime;
	
	public FuzzingConditionStartEnd(FuzzingCondition start, FuzzingCondition end) {
		this.startCond = start;
		this.endCond = end;
	}

	public boolean isActiveAtTime(double time) {
		if (!active) {
			active = startCond.evaluate();
		} else {
			active = !endCond.evaluate();
		}
		return active;
	}

	public String getCSVContents() {
		return startCond.toString() + "," + endCond.toString();
	}

	protected FuzzingTimeSpecification dup() {
		return new FuzzingConditionStartEnd(startCond.dup(), endCond.dup());
	}
	
	public String getCSVRecordTag() {
		return "KEYCONDBOTH";
	}
}
