package carsspecific.moos.moosmapping;

public class PBasicContactMgrProcess extends MOOSProcess {
	public PBasicContactMgrProcess(MOOSCommunity parent) {
		super("pBasicContactMgr", parent);
		
		setProperty("CONTACT_MAX_AGE", 300);
		setProperty("DISPLAY_RADII", true);
		setProperty("contacts_recap_interval", 5);
		setProperty("DEFAULT_ALERT_RANGE", 20);
		setProperty("DEFAULT_CPA_RANGE", 28);
		setProperty("DEFAULT_ALERT_RANGE_COLOR", "gray70");
		setProperty("DEFAULT_CPA_RANGE_COLOR", "gray30");
		
		setProperty("Alert", "id=avd, var=CONTACT_INFO, val=\"name=$[VNAME] # contact=$[VNAME]\", alert_range=40, cpa_range=45, alert_range_color=green, cpa_range_color=invisible");
	}
}
