package exptrunner.metrics;

import java.util.List;
import atlasdsl.Mission;
import fuzzingengine.FuzzingKeySelectionRecord;

public abstract class OfflineMetric extends Metric {
	public abstract Double computeFromLogs(List<FuzzingKeySelectionRecord> recs, String logDir, Mission mission) throws MetricComputeFailure;
}
