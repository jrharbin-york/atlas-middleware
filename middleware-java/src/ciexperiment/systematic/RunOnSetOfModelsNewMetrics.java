package ciexperiment.systematic;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import atlasdsl.Mission;
import exptrunner.jmetal.FuzzingSelectionsSolution;
import exptrunner.jmetal.InvalidMetrics;
import exptrunner.metrics.Metric;
import exptrunner.metrics.MetricComputeFailure;
import exptrunner.metrics.Metrics;
import exptrunner.metrics.MetricsProcessing;
import faultgen.InvalidFaultFormat;
import fuzzingengine.FuzzingKeySelectionRecord;

public class RunOnSetOfModelsNewMetrics extends ExptResultsLogged {
	private List<String> modelFilePaths;
	
	private String resFileName = "repeatedLog.res";

	private MetricHandler metricsHandler;
	
	private void setupResFile() {
		try {
			this.resFile = new FileWriter(resFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public RunOnSetOfModelsNewMetrics(MetricHandler mh, double runtime, List<String> modelFilePaths, String resFaultFile) throws FileNotFoundException, InvalidFaultFormat {
		super(runtime);
		this.modelFilePaths = modelFilePaths;
		this.metricsHandler = mh;
		this.resFileName = resFaultFile;
		setupResFile();
	}

	public boolean completed() {
		return (modelFilePaths.size() == 0);
	}

	public void printState() {
		System.out.println("Pending models to process: " + modelFilePaths.size());	
	}

	public void advance() {
		modelFilePaths.remove(0);
	}

	public Optional<String> getNextFileName() {
		if (modelFilePaths.size() > 0) {
			return Optional.of(modelFilePaths.get(0));
		} else {
			return Optional.empty();
		}
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
