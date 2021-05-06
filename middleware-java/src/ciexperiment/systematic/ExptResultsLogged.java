package ciexperiment.systematic;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import exptrunner.jmetal.InvalidMetrics;
import exptrunner.metrics.Metrics;
import exptrunner.metrics.MetricsProcessing;

public abstract class ExptResultsLogged extends ExptParams {
	protected MetricsProcessing metricsProcessing;
	protected FileWriter resFile;
	protected String resFileName = "repeatedLog.res";
	
	ExptResultsLogged(double runtime) {
		super(runtime);
	}
	
	private String ciClassLastName(String ciClassFull) {
		String [] fields = ciClassFull.split(".");
		if (fields.length > 0) {
			return fields[fields.length-1];
		} else { 
			return ciClassFull;
		}	
	}
	
	public void logResults(String logDir, String modelFile, String ciClass) {
		System.out.println("Writing results to result file: " + resFileName);
		try {
			Map<Metrics,Object> metricRes = metricsProcessing.readMetricsFromLogFiles(logDir);
			resFile.write(modelFile + "," + ciClassLastName(ciClass) + ",");
			List<Metrics> metricsToOutput = metricsProcessing.getAllMetrics(); 
						
			for (Metrics m : metricsToOutput) {
				Object val = metricRes.get(m);
				System.out.println(m + "=" + val);
				resFile.write(val.toString() + ",");
			}			
			resFile.write("\n");
			resFile.flush();
			
		} catch (InvalidMetrics e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
