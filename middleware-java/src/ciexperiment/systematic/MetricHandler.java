package ciexperiment.systematic;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import atlasdsl.Mission;
import exptrunner.metrics.*;
import fuzzingengine.FuzzingKeySelectionRecord;

public class MetricHandler {
	protected List<OfflineMetric> metrics = new ArrayList<OfflineMetric>();
	protected FileWriter resFile;
	protected String resFileName;

	public void setupFileIfNotDone() throws IOException {
		if (resFile == null) {
			resFile = new FileWriter(resFileName);
		}
	}
	
	public MetricHandler(List<OfflineMetric> metrics, String resFileName) throws IOException {
		this.metrics = metrics;
		this.resFileName = resFileName;
		setupFileIfNotDone();
	}

	public void printHeader() throws IOException {
		for (OfflineMetric m : metrics) {
			resFile.write(m.getClass().getSimpleName());
		}
	}

	public void printMetricsToOutputFile(String modelFilename, Map<Metric, Double> metricRes, String extra) throws IOException {
		System.out.println("Writing results to result file: " + resFileName);
		resFile.write(modelFilename + ",");
		for (OfflineMetric m : metrics) {
			Object val = metricRes.get(m);
			if (val == null) {
				System.out.println("Metric " + m.getClass().getSimpleName() + "produced null - is its code implemented?");
				resFile.write("<null>,");
			} else {
				System.out.println(m + "=" + val);
				resFile.write(val.toString() + ",");
			}
		}
		resFile.write(extra + "\n");
		resFile.flush();
	}
	
	public void closeRes() throws IOException {
		resFile.close();
	}

	public Map<Metric, Double> computeAllOffline(Mission mission, List<FuzzingKeySelectionRecord> recs, String logDir) throws MetricComputeFailure {
		Map<Metric, Double> results = new HashMap<Metric, Double>();
		for (OfflineMetric m : metrics) {
			Double res = m.computeFromLogs(recs, logDir, mission);
			results.put(m, res);
		}
		return results;
	}
	
	public List<OfflineMetric> getMetrics() {
		return metrics;
	}
	
	public Optional<Integer> getMetricNumberInList(Metric m) {
		Optional<Integer> res = Optional.empty();
		for (int i = 0; i < metrics.size(); i++) {
			if (metrics.get(i) == m) {
				res = Optional.of(i);
			}
		}
		return res;
	}
}