package exptrunner.jmetal;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.uma.jmetal.operator.mutation.MutationOperator;
import atlasdsl.faults.Fault;
import atlassharedclasses.FaultInstance;

public class FaultDataAdjustMutation implements MutationOperator<FaultInstanceSetSolution> {
	private enum MutationType {
		EXPAND_TIME_LENGTH, CONTRACT_TIME_LENGTH, CHANGE_ADDITIONAL_INFO, MOVE_TIME_START, NONE
	}

	private static final long serialVersionUID = 1L;

	private Random rng;
	private FileWriter mutationLog;
	private double mutationProb;

	FaultDataAdjustMutation(Random rng, String mutationLogFileName, double mutationProb) throws IOException {
		this.rng = rng;
		this.mutationProb = mutationProb;
		this.mutationLog = new FileWriter(mutationLogFileName);
	}

	// Fixed to work in-place on the given fault instance
	private void mutateFaultInstanceRandomly(FaultInstance fi) {
		double maxTimeShift = fi.getFault().getMaxTimeRange();
		MutationType mutationType = chooseMutationOption();
		System.out.println("Performing mutation on fault instance " + fi.toString());
		try {
			mutationLog.write("Performing mutation on fault instance " + fi.toString() + "\n");
			switch (mutationType) {
			case CONTRACT_TIME_LENGTH:
				// Type of mutation: contracting a FI - reducing its length
				double expandFactor = rng.nextDouble();
				System.out.println("Contracting length: factor = " + expandFactor);
				mutationLog.write("Performing mutation on fault instance " + fi.toString() + "\n");
				fi.multLengthFactor(expandFactor);
				break;
			case EXPAND_TIME_LENGTH:
				// Type of mutation: expanding a FI temporally - extending its length
				double contractFactor = rng.nextDouble();
				expandFactor = 1.0 / contractFactor;
				System.out.println("Expanding length: factor = " + expandFactor);
				mutationLog.write("Expanding length: factor = " + expandFactor + "\n");
				fi.multLengthFactor(expandFactor);
				break;
			case MOVE_TIME_START:
				// Add a maximum proportion of the mission to timeshift by
				// Possible TODO: MOVE_TIME_START
				double absTimeShift = (rng.nextDouble() - 0.5) * maxTimeShift * 2;
				System.out.println("Moving fault: absTimeShift = " + absTimeShift);
				mutationLog.write("Moving fault: absTimeShift = " + absTimeShift + "\n");
				fi.absShiftTimes(absTimeShift);
				break;
			case CHANGE_ADDITIONAL_INFO:
				mutationLog.write("Change additional info\n");
				changeAdditionalInfo(fi);
			case NONE:
				mutationLog.write("No mutation (mutation prob not met)\n");
				System.out.println("No mutation (mutation prob not met)\n");
				break;
			}
			System.out.println("Mutated fault = " + fi.toString());
			mutationLog.write("Mutated fault = " + fi.toString() + "\n");
			mutationLog.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Fixed to work in-place on the given fault instance
	private void changeAdditionalInfo(FaultInstance fi) {
		final double MIN_SPEED_VALUE = 1.0;
		final double MAX_SPEED_VALUE = 5.0;

		Fault f = fi.getFault();
		if (f.getName().contains("SPEEDFAULT")) {
			double newSpeed = MIN_SPEED_VALUE + rng.nextDouble() * (MAX_SPEED_VALUE - MIN_SPEED_VALUE);
			fi.setExtraData(Double.toString(newSpeed));
		}

		if (f.getName().contains("HEADINGFAULT")) {
			double newHeading = rng.nextDouble() * 360.0;
			fi.setExtraData(Double.toString(newHeading));
		}
	}

	private MutationType chooseMutationOption() {
		// Need to choose whether mutation is done based on the mutationProb - it is
		// currently not done at all
		double shouldDoMutation = rng.nextDouble();
		if (shouldDoMutation < mutationProb) {
			// After deciding to perform mutation, pick a type
			// Each type is currently equiprobable
			double v = rng.nextDouble();
			if (v < 0.25) {
				return MutationType.CONTRACT_TIME_LENGTH;
			} else if (v < 0.5) {
				return MutationType.EXPAND_TIME_LENGTH;
			} else if (v < 0.75) {
				return MutationType.MOVE_TIME_START;
			} else
				return MutationType.CHANGE_ADDITIONAL_INFO;
		} else {
			return MutationType.NONE;
		}
	}

	private void mutatePossiblyMultipleTimes(FaultInstance input, int maxTimes) {
		mutateFaultInstanceRandomly(input);
		int extraMutations = rng.nextInt(maxTimes);
		for (int i = 0; i < extraMutations; i++) {
			mutateFaultInstanceRandomly(input);
		}
	}

	public FaultInstanceSetSolution execute(FaultInstanceSetSolution source) {
		final int MAX_INDIVIDUAL_MUTATIONS = 2;

		for (int i = 0; i < source.getNumberOfVariables(); i++) {
			FaultInstance faultInstance = source.getVariable(i);
			mutatePossiblyMultipleTimes(faultInstance, MAX_INDIVIDUAL_MUTATIONS);
			System.out.println("contents length = " + source.getNumberOfVariables());
		}
		return source;
	}

	public double getMutationProbability() {
		return mutationProb;
	}
}
