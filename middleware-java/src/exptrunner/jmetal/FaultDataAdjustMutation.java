package exptrunner.jmetal;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.uma.jmetal.operator.mutation.MutationOperator;
import atlasdsl.faults.Fault;
import atlassharedclasses.FaultInstance;

public class FaultDataAdjustMutation implements MutationOperator<FaultInstanceSetSolution> {
	private enum MutationType {
		EXPAND_TIME_LENGTH, CONTRACT_TIME_LENGTH, CHANGE_ADDITIONAL_INFO, MOVE_TIME_START,
	}

	private static final long serialVersionUID = 1L;

	private Random rng;
	private FileWriter mutationLog;
	private double mutationProb;

	FaultDataAdjustMutation(Random rng, String mutationLogFileName, double mutationProb)
			throws IOException {
		this.rng = rng;
		this.mutationProb = mutationProb;
		this.mutationLog = new FileWriter(mutationLogFileName);
	}

	private FaultInstance mutateFaultInstanceRandomly(FaultInstance input) {
		FaultInstance output = new FaultInstance(input);
		double maxTimeShift = input.getFault().getMaxTimeRange();
		MutationType mutationType = chooseMutationOption();
		System.out.println("Performing mutation on fault instance " + input.toString());
		try {
			mutationLog.write("Performing mutation on fault instance " + input.toString() + "\n");
			switch (mutationType) {
			case CONTRACT_TIME_LENGTH:
				// Type of mutation: contracting an FI - reducing its length
				double expandFactor = rng.nextDouble();
				System.out.println("Contracting length: factor = " + expandFactor);
				mutationLog.write("Performing mutation on fault instance " + input.toString() + "\n");
				output.multLengthFactor(expandFactor);
				break;
			case EXPAND_TIME_LENGTH:
				// Type of mutation: expanding an FI temporally - extending its length
				double contractFactor = rng.nextDouble();
				expandFactor = 1.0 / contractFactor;
				System.out.println("Expanding length: factor = " + expandFactor);
				mutationLog.write("Expanding length: factor = " + expandFactor + "\n");
				output.multLengthFactor(expandFactor);
				break;
			case MOVE_TIME_START:
				double absTimeShift = (rng.nextDouble() - 0.5) * maxTimeShift * 2;
				System.out.println("Moving fault: absTimeShift = " + absTimeShift);
				mutationLog.write("Moving fault: absTimeShift = " + absTimeShift + "\n");
				output.absShiftTimes(absTimeShift);
				break;
			case CHANGE_ADDITIONAL_INFO:
				mutationLog.write("Change additional info\n");
				output = changeAdditionalInfo(input);
			}
			System.out.println("Mutated fault = " + output.toString());
			mutationLog.write("Mutated fault = " + output.toString() + "\n");
			mutationLog.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	private FaultInstance changeAdditionalInfo(FaultInstance input) {
		final double MIN_SPEED_VALUE = 1.0;
		final double MAX_SPEED_VALUE = 5.0;

		Fault f = input.getFault();
		FaultInstance output = input;
		if (f.getName().contains("SPEEDFAULT")) {
			double newSpeed = MIN_SPEED_VALUE + rng.nextDouble() * (MAX_SPEED_VALUE - MIN_SPEED_VALUE);
			output.setExtraData(Double.toString(newSpeed));
		}

		if (f.getName().contains("HEADINGFAULT")) {
			double newHeading = rng.nextDouble() * 360.0;
			output.setExtraData(Double.toString(newHeading));
		}
		return output;
	}

	private MutationType chooseMutationOption() {
		double v = rng.nextDouble();
		if (v < 0.15) {
			return MutationType.CONTRACT_TIME_LENGTH;
		} else if (v < 0.3) {
			return MutationType.EXPAND_TIME_LENGTH;
		} else if (v < 0.45) {
			return MutationType.MOVE_TIME_START;
		} else
			return MutationType.CHANGE_ADDITIONAL_INFO;
	}

	private FaultInstance mutatePossiblyMultipleTimes(FaultInstance input, int maxTimes) {
		FaultInstance mutated = mutateFaultInstanceRandomly(input);
		int extraMutations = rng.nextInt(maxTimes);
		for (int i = 0; i < extraMutations; i++) {
			mutated = mutateFaultInstanceRandomly(mutated);
		}
		return mutated;
	}

	public FaultInstanceSetSolution execute(FaultInstanceSetSolution source) {
		// TODO: pick one or more to alter
		final int MAX_INDIVIDUAL_MUTATIONS = 2;
		FaultInstanceSetSolution output = (FaultInstanceSetSolution)source.copy();
		
		for (int i = 0; i < source.getNumberOfVariables(); i++) {
			FaultInstance faultInstance = source.getVariable(i);
		    FaultInstance fiOut = mutatePossiblyMultipleTimes(faultInstance, MAX_INDIVIDUAL_MUTATIONS);
		    System.out.println("contents length = " + output.getNumberOfVariables());
			output.addContents(i, fiOut);
		}
		return output;
	}

	public double getMutationProbability() {
		return mutationProb;
	}
}
