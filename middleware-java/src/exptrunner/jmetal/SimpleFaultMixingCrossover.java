package exptrunner.jmetal;

// Breaks up variable-length chromosomes X and Y and splits it into two new ones
// Takes the ones before the cut point - X_left, Y_left  
// Takes the ones after the cut point - X_right, Y_right

// Produces two new ones X_left, Y_right
// and Y_left, X_right
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimpleFaultMixingCrossover implements CrossoverOperator<FaultInstanceSetSolution> {

	private double crossoverProbability;
	private Random randomGenerator;
	private static final long serialVersionUID = 1L;

	public SimpleFaultMixingCrossover(double crossoverProbability, Random randomGenerator) {
		if (crossoverProbability < 0) {
			throw new JMetalException("Crossover probability is negative: " + crossoverProbability);
		}

		this.crossoverProbability = crossoverProbability;
		this.randomGenerator = randomGenerator;
	}

	public List<FaultInstanceSetSolution> doCrossover(FaultInstanceSetSolution cx, FaultInstanceSetSolution cy) {
		List<FaultInstanceSetSolution> output = new ArrayList<FaultInstanceSetSolution>();
		// TODO: generate the cut points for each 0 -> len, splitting into left and
		// right
		// copy X_left, Y_right
		// copy Y_left, X_right
		// add these to the output

		// output.add(new_c1);
		// output.add(new_c2);
		return output;
	}

	public List<FaultInstanceSetSolution> execute(List<FaultInstanceSetSolution> source) {
		// TODO Auto-generated method stub
		return null;
	}

	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	public int getNumberOfRequiredParents() {
		return 2;
	}

	public int getNumberOfGeneratedChildren() {
		return 2;
	}

}
