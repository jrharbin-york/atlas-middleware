package carsspecific.ros.rosmapping;

public class PMarineViewerProcess extends MOOSProcess {
	public PMarineViewerProcess(MOOSCommunity parent) {
		super("pMarineViewer", parent);
		setProperty("TIFF_FILE", "forrest19.tif");
		setProperty("set_pan_x",-90);
		setProperty("set_pan_y",-280);
		setProperty("zoom",0.65);
		setProperty("vehicles_shape_scale",1.5);
		setProperty("vehicles_name_viewable","names");
		setProperty("appcast_viewable", true);
		setProperty("appcast_color_scheme","indigo");
		setProperty("SCOPE", "NODE_BROKER_PING");
		setProperty("SCOPE", "DEPLOY_ALL");
		setProperty("SCOPE", "CONTACT_INFO");
		setProperty("ACTION","PERMUTING = true");
		setProperty("ACTION","PERMUTING = false");
		setProperty("ACTION","WIND_GUSTS = true");
		setProperty("ACTION","WIND_GUSTS = false");
		setProperty("ACTION","STATION_ALL = false");
		setProperty("ACTION","STATION_ALL = true");

		// For now, all the buttons are removed from the interface 
		// The CI and middleware control all MOOS behaviour
		//setProperty("BUTTON_ONE","DEPLOY  # DEPLOY_ALL=true");
		//setProperty("BUTTON_ONE","MOOS_MANUAL_OVERRIDE_ALL=false");
		//setProperty("BUTTON_ONE","RETURN_ALL=false");
		//setProperty("BUTTON_TWO","RETURN  # RETURN_ALL=true");
		//setProperty("BUTTON_THREE","PERMUTE   # UTS_FORWARD=0");
	}
}
