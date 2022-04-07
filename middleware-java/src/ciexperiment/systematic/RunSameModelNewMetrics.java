package ciexperiment.systematic;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import atlasdsl.Mission;
import exptrunner.metrics.Metric;
import exptrunner.metrics.MetricComputeFailure;
import fuzzingengine.FuzzingKeySelectionRecord;

public class RunSameModelNewMetrics extends ExptResultsLogged {

	private int countLimit = 0;
	private int countCompleted = 0;
	private String modelFilePath;
	
	private MetricHandler metricsHandler;
	
	public RunSameModelNewMetrics(MetricHandler mh, double runtime, String modelFilePath, String resFileName, int countLimit) throws IOException {
		super(runtime);
		this.countLimit = countLimit;
		this.modelFilePath = modelFilePath;
		this.metricsHandler = mh;
		this.resFile = new FileWriter(resFileName);
	}
	
	public boolean completed() {
		return (countCompleted >= countLimit);
	}

	public void printState() {

	}

	public void advance() {
		countCompleted++;
	}

	public Optional<String> getNextFileName() {
		return null;
	}

	public String getModelFile() {
		return modelFilePath;
	}

	public void logResults(String logDir, String modelFile, String ciClass, Mission mission) {
		System.out.println("Writing results to result file: " + resFileName);
		List<FuzzingKeySelectionRecord> recs = new ArrayList<FuzzingKeySelectionRecord>();
		try {
			Map<Metric,Double> res = metricsHandler.computeAllOffline(mission, recs, logDir);
			metricsHandler.printMetricsToOutputFile(modelFile, res, ciClass);
		} catch (MetricComputeFailure e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
