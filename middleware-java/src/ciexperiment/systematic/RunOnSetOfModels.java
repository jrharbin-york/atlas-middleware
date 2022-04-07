package ciexperiment.systematic;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import exptrunner.jmetal.FuzzingSelectionsSolution;
import exptrunner.jmetal.InvalidMetrics;
import exptrunner.metrics.Metrics;
import exptrunner.metrics.MetricsProcessing;
import faultgen.InvalidFaultFormat;

public class RunOnSetOfModels extends ExptResultsLogged {
	private List<String> modelFilePaths;
	
	private String resFileName = "repeatedLog.res";
	
	private void setupResFile() {
		try {
			this.resFile = new FileWriter(resFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public RunOnSetOfModels(MetricsProcessing mp, double runtime, List<String> modelFilePaths, String resFaultFile) throws FileNotFoundException, InvalidFaultFormat {
		super(runtime);
		this.modelFilePaths = modelFilePaths;
		this.metricsProcessing = mp;
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

	protected String getModelFile() {
		return "";
	}
}
