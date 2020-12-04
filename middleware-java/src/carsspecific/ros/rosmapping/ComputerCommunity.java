package carsspecific.ros.rosmapping;

public class ComputerCommunity extends MOOSCommunity {
	public ComputerCommunity(MOOSSimulation sim, String computerName) {
		super(sim,computerName);
		addProcess(new PMarineViewerProcess(this));
		addProcess(new PHostinfoProcess(this));
		addProcess(new UFldShoreBrokerProcess(this));
		
		PShareProcess pshare = new PShareProcess(this, dbPortOffset);
		pshare.addShoresideInput();
		addProcess(pshare);
		
		addProcess(new UFldNodeCommsProcess(this));
	}
}
