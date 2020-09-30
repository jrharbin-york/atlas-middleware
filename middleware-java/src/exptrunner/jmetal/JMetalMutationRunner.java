package exptrunner.jmetal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;

import org.uma.jmetal.example.AlgorithmRunner;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;

import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;

import atlasdsl.Mission;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import exptrunner.jmetal.test.ATLASEvaluationProblemDummy;

public class JMetalMutationRunner extends AbstractAlgorithmRunner {

	static private int popSize = 10;
	static private int initialFaultCount = 10;
	static private boolean actuallyRun = true;
	static private double exptRunTime = 1200.0;
	
	static private int maxEvaluations = 25000;
	
	static double crossoverProb = 0.1;
	static double mutationProb = 0.2;
	static int offspringPopulationSize = 10;
	
	// TODO: move this back to RunExperiment?
	static private String LOG_FILE_DIR = RunExperiment.ABS_MIDDLEWARE_PATH + "logs/";

	static private String referenceParetoFront = "";

	private static void jMetalRun(Mission mission) {

		Random problemRNG = new Random();
		Random crossoverRNG = new Random();
		Random mutationRNG = new Random();

		Map<GoalsToCount, Integer> goals = new HashMap<GoalsToCount,Integer>();
		goals.put(GoalsToCount.DISCOVERY_GOALS, 1);
		goals.put(GoalsToCount.OBSTACLE_AVOIDANCE_GOALS, 1);
		
		Problem<FaultInstanceSetSolution> problem = new ATLASEvaluationProblem(problemRNG, mission, initialFaultCount,
				actuallyRun, exptRunTime, LOG_FILE_DIR, goals);
		
		Algorithm<List<FaultInstanceSetSolution>> algorithm;
		CrossoverOperator<FaultInstanceSetSolution> crossover;
		MutationOperator<FaultInstanceSetSolution> mutation;
		SelectionOperator<List<FaultInstanceSetSolution>, FaultInstanceSetSolution> selection;

		try {
			crossover = new SimpleFaultMixingCrossover(crossoverProb, crossoverRNG);
			mutation = new FaultDataAdjustMutation(mutationRNG, "mutation.log", mutationProb);
			selection = new BinaryTournamentSelection<FaultInstanceSetSolution>();

			algorithm = new NSGAIIBuilder<FaultInstanceSetSolution>(problem, crossover, mutation, popSize)
					.setMaxEvaluations(maxEvaluations)
					.setSelectionOperator(selection)
					.setOffspringPopulationSize(offspringPopulationSize).build();

			AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

			List<FaultInstanceSetSolution> population = algorithm.getResult();
			long computingTime = algorithmRunner.getComputingTime();

			JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

			printFinalSolutionSet(population);
			if (!referenceParetoFront.equals("")) {
				printQualityIndicators(population, referenceParetoFront);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// JMetalLogger.logger.info(... )
	public static void main(String[] args) throws JMetalException, FileNotFoundException {
		DSLLoader dslloader = new GeneratedDSLLoader();
		Mission mission;
		try {
			mission = dslloader.loadMission();
			jMetalRun(mission);
		} catch (DSLLoadFailed e) {
			System.out.println("DSL loading failed - configuration problems");
			e.printStackTrace();
		}
	}
}
