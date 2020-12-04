package carsspecific.ros.rosmapping;

import java.util.List;

public class UFldNodeBrokerProcess extends MOOSProcess {
	public UFldNodeBrokerProcess(MOOSCommunity parent) {
		super("uFldNodeBroker", parent);
		
		resetProperty("AppTick", 1);
		resetProperty("CommsTick", 1);
		
		// TODO: this should look up the shoreside host from the community
		setProperty("TRY_SHORE_HOST", "pshare_route=localhost:9300");
		setProperty("BRIDGE", "src=VIEW_POLYGON");
		setProperty("BRIDGE", "src=VIEW_POINT");
		setProperty("BRIDGE", "src=VIEW_SEGLIST");
		setProperty("BRIDGE", "src=APPCAST");
		setProperty("BRIDGE", "src=NODE_REPORT_LOCAL,  alias=NODE_REPORT");
		setProperty("BRIDGE", "src=NODE_MESSAGE_LOCAL, alias=NODE_MESSAGE");
	}
	
	public void prepareAdditionalProperties() {
		// Add additional properties from the shared variables registered on the
		// parent MOOSCommunity
		List<String> sharedVars = parent.getSharedVars();
		for (String sv : sharedVars) {
			setProperty("BRIDGE", "src=" + sv);
		}
	}
}