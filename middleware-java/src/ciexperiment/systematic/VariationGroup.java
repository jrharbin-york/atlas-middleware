package ciexperiment.systematic;

import java.util.ArrayList;

public class VariationGroup {
	
	ArrayList<String> memberIds = new ArrayList<String>();
	String groupID = "";
	public String getGroupID() {
		return groupID;
	}
	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}
	public ArrayList<String> getMemberIds() {
		return memberIds;
	}
	public void setMemberIds(ArrayList<String> memberIds) {
		this.memberIds = memberIds;
	}
	public int getMaxLimit() {
		return maxLimit;
	}
	public void setMaxLimit(int maxLimit) {
		this.maxLimit = maxLimit;
	}
	public int getMinLimit() {
		return minLimit;
	}
	public void setMinLimit(int minLimit) {
		this.minLimit = minLimit;
	}
	int maxLimit;
	int minLimit;

}
