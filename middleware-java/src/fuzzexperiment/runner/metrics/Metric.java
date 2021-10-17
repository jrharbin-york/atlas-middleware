package fuzzexperiment.runner.metrics;

public abstract class Metric {
	public enum MetricDirection {
		LOWEST,
		HIGHEST;
	}
	
	public abstract MetricDirection optimiseDirection();
}