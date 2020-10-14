package exptrunner.jmetal;

public class ExptError extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String msg;
	
	ExptError(String msg) {
		this.msg = msg;
	}
}
