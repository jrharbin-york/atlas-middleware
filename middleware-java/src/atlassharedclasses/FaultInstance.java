package atlassharedclasses;

import java.util.Optional;

import atlasdsl.faults.*;

public class FaultInstance implements Comparable<FaultInstance> {
	private double startTime;
	private double endTime;
	// Fault instances are always active by default - for compatibility
	private boolean isActive = true;
	private Fault fault;
	private Optional<String> extraData;
	private int _counter;

	private static int _fIDcounter = 0;

	private void setupExtraData(Optional<String> newExtraData) {
		// Ensure the extra data uses a unique optional
		if (newExtraData.isPresent()) {
			this.extraData = Optional.of(newExtraData.get());
		} else {
			this.extraData = Optional.empty();
		}
	}

	public FaultInstance(Double startTime, Double endTime, Fault f, Optional<String> extraData) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.fault = f;
		setupExtraData(extraData);
		this._counter = _fIDcounter++;
	}

	public FaultInstance(FaultInstance orig) {
		this.endTime = orig.endTime;
		this.startTime = orig.startTime;
		this.fault = orig.fault;
		this.extraData = orig.extraData;
		setupExtraData(extraData);
		this.isActive = orig.isActive;
		this._counter = _fIDcounter++;
	}

	public void setActiveFlag(boolean flag) {
		this.isActive = flag;
	}

	public int compareTo(FaultInstance other) {
		int timeCompare = Double.compare(this.startTime, other.startTime);
		if (timeCompare != 0) {
			return timeCompare;
		} else {
			int idCompare = this.fault.getName().compareTo(other.fault.getName());
			return idCompare;
		}
	}

	public String toString() {
		String isActiveStr = "";
		if (!isActive) {
			isActiveStr = ",[INACTIVE]";
		}
		return _counter + "," + fault.getName() + "," + startTime + "," + endTime + "," + extraData + isActiveStr + ":";
	}

	public boolean isReady(double time) {
		return (time >= startTime) && (time <= endTime);
	}

	public Fault getFault() {
		return fault;
	}

	public boolean isValid() {
		FaultTimeProperties ftp = fault.getTimeProperties();
		return ftp.isInRange(startTime, endTime);
	}

	public boolean isFinished(double time) {
		return (time > endTime);
	}

	public double getEndTime() {
		return endTime;
	}

	public double getStartTime() {
		return startTime;
	}

	public double getLength() {
		return endTime - startTime;
	}

	public String getExtraData() {
		if (extraData.isPresent()) {
			return extraData.get();
		} else {
			return "";
		}
	}

	public Optional<String> getExtraDataOpt() {
		return extraData;
	}

	private void constrainTimeValid() {
		Fault f = this.getFault();
		double duration = this.endTime - this.startTime;

		if (startTime < f.getEarliestStartTime()) {
			startTime = f.getEarliestStartTime();
			endTime = startTime + duration;
		}

		if (endTime < f.getLatestEndTime()) {
			endTime = f.getLatestEndTime();
			startTime = endTime - duration;
		}
	}

	public void multLengthFactor(double factor) {
		double origLen = endTime - startTime;
		System.out.println("origLen = " + origLen);
		endTime = startTime + (origLen * factor);

		System.out.println("endTime=" + endTime);
		constrainTimeValid();
	}

	public void absShiftTimes(double absTimeShift) {
		Fault f = this.getFault();
		this.startTime += absTimeShift;
		this.endTime += absTimeShift;
		constrainTimeValid();
	}

	public boolean isActive() {
		return isActive;
	}

	public void flipActiveFlag() {
		isActive = !isActive;
	}

	public void setExtraData(String string) {
		extraData = Optional.of(string);
	}
}
