package carsspecific.ros.translations;

public class CARSTransationSetupFailed extends Exception {
	private String string;
	
	public CARSTransationSetupFailed(String string) {
		this.string = string;
	}

	private static final long serialVersionUID = 1L;

}
