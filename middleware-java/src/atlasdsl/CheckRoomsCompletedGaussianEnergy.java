package atlasdsl;

import java.util.Random;

public class CheckRoomsCompletedGaussianEnergy extends CheckRoomsCompleted {

	private double energyPerRoomStdDev;
	private Random rng;
	
	public CheckRoomsCompletedGaussianEnergy(double energyPerRoomMean, double energyPerRoomStdDev, long seed) {
		super(energyPerRoomMean);
		this.energyPerRoomStdDev = energyPerRoomStdDev;
		rng = new Random(seed);
	}
	
	public double getEnergyPerRoom() {
		double randValue = rng.nextGaussian();
		double energy = energyPerRoomMean + randValue * energyPerRoomStdDev;
		energy = Math.max(0.0, energy);
		System.out.println("energyPerRoom gaussian = " + energy);
		return energy;
	}
}
