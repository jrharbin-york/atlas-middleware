package exptrunner.jmetal;

public class InvalidMetrics extends Exception {
	private static final long serialVersionUID = 1L;
	private Metrics metric;
	private String msg;
	
	public InvalidMetrics(Metrics m, String msg) {
		this.metric = m;
		this.msg = msg;
	}
}
