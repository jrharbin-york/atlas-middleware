package exptrunner.jmetal;

import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.solution.Solution;

public class JMetalMutationRunner extends AbstractAlgorithmRunner {
	
	private void test() {
		String problemName = "test.prb";
		
	    Problem<FaultInstanceSetSolution> problem = ProblemUtils.<FaultInstanceSetSolution>loadProblem(problemName);
	    Algorithm<List<FaultInstanceSetSolution>> algorithm;
	    CrossoverOperator<FaultInstanceSetSolution> crossover;
	    MutationOperator<FaultInstanceSetSolution> mutation;
	    SelectionOperator<List<FaultInstanceSetSolution>, FaultInstanceSetSolution> selection;
	   
	    // TODO: define crossover and mutation
	    crossover = createOnePointCrossover();
	    mutation = simpleVariableAdjustment();
	    
	    algorithm = new NSGAIIBuilder<FaultInstanceSetSolution>(problem, crossover, mutation)
	        .setSelectionOperator(selection)
            .setMaxEvaluations(25000)
	        .setPopulationSize(100)
	        .build();
	}
}
