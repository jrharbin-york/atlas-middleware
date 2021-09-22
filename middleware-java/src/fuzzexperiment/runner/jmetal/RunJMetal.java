package fuzzexperiment.runner.jmetal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.util.*;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIMeasures;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BestSolutionSelection;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.operator.selection.impl.RankingAndCrowdingSelection;
import org.uma.jmetal.operator.selection.impl.TournamentSelection;
import org.uma.jmetal.problem.Problem;

import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import atlasdsl.Mission;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import atlassharedclasses.FaultInstance;
import fuzzexperiment.runner.StartFuzzingProcesses;
import fuzzexperiment.runner.metrics.Metric;
import fuzzexperiment.runner.metrics.OfflineMetric;
import fuzzexperiment.runner.metrics.fake.FindSpecificTime;

public class RunJMetal extends AbstractAlgorithmRunner {

	static private int populationSize = 6;
	static private int offspringPopulationSize = 6;
	
	static private int matingPoolSize = populationSize;
	static private boolean actuallyRun = false;
	static private double exptRunTime = 1200.0;

	static private int maxIterations = 1000;

	static double crossoverProb = 0.2;
	static double mutationProb = 0.6;

	static private String referenceParetoFront = "";
	
	private String logPath;

	private void readProperties() {
		Properties prop = new Properties();
		String fileName = "fuzzingexpt.config";
		InputStream is = null;
		try {
		    is = new FileInputStream(fileName);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		try {
		    prop.load(is);
		    logPath = prop.getProperty("paths.ros.log");
		    is.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private RunJMetal() {
		readProperties();
	}
	
	public void jMetalRun(String tag, Mission mission) throws ExptError, DSLLoadFailed {
		
		Set<Metric> metrics_s = mission.getAllMetrics();
		List<OfflineMetric> metrics = new ArrayList<OfflineMetric>();
		
//		for (Metric m : metrics_s) {
//			if (m instanceof OfflineMetric) {
//				metrics.add((OfflineMetric)m);
//			}
//		}

		Random problemRNG = new Random();
		Random crossoverRNG = new Random();
		Random mutationRNG = new Random();

		Problem<FuzzingSelectionsSolution> problem;
			
		try {
			problem = new SAFEMUVEvaluationProblem(populationSize, problemRNG, mission, actuallyRun, exptRunTime,
						logPath, metrics);
				

			Algorithm<List<FuzzingSelectionsSolution>> algorithm;
			CrossoverOperator<FuzzingSelectionsSolution> crossover;
			MutationOperator<FuzzingSelectionsSolution> mutation;
			SelectionOperator<List<FuzzingSelectionsSolution>, FuzzingSelectionsSolution> selection;
			SolutionListEvaluator<FuzzingSelectionsSolution> evaluator;
			Comparator<FuzzingSelectionsSolution> dominanceComparator;

			crossover = new NullFuzzingCrossover(crossoverProb, crossoverRNG);
			
			mutation = new FuzzingSelectionsMutation(mutationRNG, "mutation.log", mutationProb);
			selection = new TournamentSelection<FuzzingSelectionsSolution>(5);
			dominanceComparator = new DominanceComparator<>();
			evaluator = new SequentialSolutionListEvaluator<FuzzingSelectionsSolution>();
			

			algorithm = new NSGAIIMeasures(problem, maxIterations, populationSize, matingPoolSize, offspringPopulationSize,
					crossover, mutation, selection, dominanceComparator, evaluator);

			// For some reason, can't create algorithm executor. Just run it
			//AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
			algorithm.run();
			List<FuzzingSelectionsSolution> population = algorithm.getResult();
			
			//long computingTime = algorithmRunner.getComputingTime();
			//JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

			printFinalSolutionSet(population);
			if (!referenceParetoFront.equals("")) {
				printQualityIndicators(population, referenceParetoFront);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws JMetalException, FileNotFoundException {
		DSLLoader dslloader = new GeneratedDSLLoader();
		Mission mission;
		try {
			mission = dslloader.loadMission();
			exptRunTime = mission.getEndTime();
			RunJMetal runjmetal = new RunJMetal();
			runjmetal.jMetalRun("expt1", mission);
		} catch (DSLLoadFailed e) {
			System.out.println("DSL loading failed - configuration problems");
			e.printStackTrace();
		} catch (ExptError e) {
			e.printStackTrace();
		}
	}
}
