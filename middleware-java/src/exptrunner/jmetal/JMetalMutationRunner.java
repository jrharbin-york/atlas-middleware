package exptrunner.jmetal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIMeasures;
import org.uma.jmetal.example.AlgorithmRunner;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
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
import exptrunner.jmetal.test.ATLASEvaluationProblemDummy;
import exptrunner.jmetal.test.ATLASEvaluationProblemDummy.EvaluationProblemDummyChoices;

public class JMetalMutationRunner extends AbstractAlgorithmRunner {

	static private int populationSize = 12;
	static private int offspringPopulationSize = 12;
	
	static private int matingPoolSize = populationSize;
	static private boolean actuallyRun = true;
	static private double exptRunTime = 1200.0;

	static private int maxIterations = 2000000;
	static private int maxGenerations = 15;

	static double crossoverProb = 0.2;
	static double mutationProb = 0.6;
	

	// TODO: move this back to RunExperiment?
	static private String LOG_FILE_DIR = RunExperiment.ABS_MIDDLEWARE_PATH + "logs/";

	static private String referenceParetoFront = "";

	public static void jMetalRun(String tag, Mission mission, Optional<EvaluationProblemDummyChoices> testChoice_o, Optional<List<Metrics>> metrics_o) throws ExptError {

		Random problemRNG = new Random();
		Random crossoverRNG = new Random();
		Random mutationRNG = new Random();

		Map<GoalsToCount, Integer> goals = new HashMap<GoalsToCount, Integer>();
		goals.put(GoalsToCount.DISCOVERY_GOALS, 1);
		goals.put(GoalsToCount.OBSTACLE_AVOIDANCE_GOALS, 1);

		Problem<FaultInstanceSetSolution> problem;
			
		try {
			if (testChoice_o.isEmpty()) {
				if (!metrics_o.isPresent()) {
					throw new ExptError("No metrics selected");
				}
				List<Metrics> metrics = metrics_o.get();
				problem = new ATLASEvaluationProblem(problemRNG, mission, actuallyRun, exptRunTime,
						LOG_FILE_DIR, goals, metrics);
				
			} else {
				EvaluationProblemDummyChoices testChoice = testChoice_o.get();
				// Just use dummy metrics
				List<Metrics> metrics = new ArrayList<Metrics>();
				if (testChoice == EvaluationProblemDummyChoices.EXPT_RUNNER_FAKE_FAULTS) {
					problem = new ATLASEvaluationProblem(problemRNG, mission, actuallyRun, exptRunTime,
							LOG_FILE_DIR, goals, metrics);
					((ATLASEvaluationProblem) problem).setFakeExperimentNum(1);
				} else {
					if (testChoice == EvaluationProblemDummyChoices.EXPT_RUNNER_LOG_FAULTS) {
						problem = new ATLASEvaluationProblem(problemRNG, mission, actuallyRun, exptRunTime,
								LOG_FILE_DIR, goals, metrics);
						((ATLASEvaluationProblem) problem).setFakeExperimentNum(2);
					} else {
						problem = new ATLASEvaluationProblemDummy(problemRNG, mission, actuallyRun,
								exptRunTime, LOG_FILE_DIR, testChoice);
					}
				}
			}

			Algorithm<List<FaultInstanceSetSolution>> algorithm;
			CrossoverOperator<FaultInstanceSetSolution> crossover;
			MutationOperator<FaultInstanceSetSolution> mutation;
			SelectionOperator<List<FaultInstanceSetSolution>, FaultInstanceSetSolution> selection;
			SolutionListEvaluator<FaultInstanceSetSolution> evaluator;
			Comparator<FaultInstanceSetSolution> dominanceComparator;

			crossover = new SimpleFaultMixingCrossover(crossoverProb, crossoverRNG);
			mutation = new FaultDataAdjustMutation(mutationRNG, "mutation.log", mutationProb);
			selection = new TournamentSelection<FaultInstanceSetSolution>(5);
			dominanceComparator = new DominanceComparator<>();
			//selection = new RankingAndCrowdingSelection<FaultInstanceSetSolution>(solutionsSelectCount, dominanceComparator);
			//selection = new BestSolutionSelection<FaultInstanceSetSolution>(dominanceComparator);

			evaluator = new SequentialSolutionListEvaluator<FaultInstanceSetSolution>();
			

			algorithm = new NSGAIIMeasures(tag, problem, maxIterations, maxGenerations, populationSize, matingPoolSize,
					offspringPopulationSize, crossover, mutation, selection, dominanceComparator, evaluator);

			AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
			List<FaultInstanceSetSolution> population = algorithm.getResult();
			long computingTime = algorithmRunner.getComputingTime();
			

			JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

			printFinalSolutionSet(population);
			//if (!referenceParetoFront.equals("")) {
				printQualityIndicators(population, referenceParetoFront);
			//}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws JMetalException, FileNotFoundException {
		DSLLoader dslloader = new GeneratedDSLLoader();
		Mission mission;
		try {
			mission = dslloader.loadMission();
			List<Metrics> metrics = new ArrayList<Metrics>();
			// Read experiment number
			metrics.add(Metrics.OBSTACLE_AVOIDANCE_METRIC);
			metrics.add(Metrics.TIME_TOTAL_ABSOLUTE);
			
			jMetalRun("expt1", mission, Optional.empty(), Optional.of(metrics));
		} catch (DSLLoadFailed e) {
			System.out.println("DSL loading failed - configuration problems");
			e.printStackTrace();
		} catch (ExptError e) {
			e.printStackTrace();
		}
	}
}
