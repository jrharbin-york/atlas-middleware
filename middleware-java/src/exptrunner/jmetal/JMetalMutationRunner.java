package exptrunner.jmetal;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.example.AlgorithmRunner;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;
import org.uma.jmetal.solution.Solution;

public class JMetalMutationRunner extends AbstractAlgorithmRunner {

	private void test() {
		String problemName = "test.prb";

		Problem<FaultInstanceSetSolution> problem = ProblemUtils.<FaultInstanceSetSolution>loadProblem(problemName);
		Algorithm<List<FaultInstanceSetSolution>> algorithm;
		CrossoverOperator<FaultInstanceSetSolution> crossover;
		MutationOperator<FaultInstanceSetSolution> mutation;
		SelectionOperator<List<FaultInstanceSetSolution>, FaultInstanceSetSolution> selection;

		double crossoverProb = 0.1;
		double mutationProb = 0.2;
		
		String referenceParetoFront = "" ;

		Random crossoverRNG = new Random();
		Random mutationRNG = new Random();

		try {
			crossover = new SimpleFaultMixingCrossover(crossoverProb, crossoverRNG);
			mutation = new FaultDataAdjustMutation(mutationRNG, "mutation.log", mutationProb);
			selection = new BestSelection();

			// TODO: what is the zero?
			algorithm = new NSGAIIBuilder<FaultInstanceSetSolution>(problem, crossover, mutation, 0)
					.setSelectionOperator(selection).setMaxEvaluations(25000).setOffspringPopulationSize(100).build();

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
}
