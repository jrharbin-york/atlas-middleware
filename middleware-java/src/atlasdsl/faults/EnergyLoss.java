package atlasdsl.faults;

import java.util.Optional;

import atlasdsl.*;
import middleware.core.ATLASCore;

// TODO: need to change this to refer to the parent Robot
public class EnergyLoss extends ComponentImpact {
	private int fixedEnergyLoss;
	
	public EnergyLoss(Component c, int fixedEnergyLoss, int powerDraw) throws InvalidComponentType {
		// Verify here that the component is a Battery
		// For now, EnergyLoss faults are only considering sudden depletion of a battery
		super(c);
		if (c instanceof Battery) {
			this.fixedEnergyLoss = fixedEnergyLoss;
		} else {
			throw new InvalidComponentType(c, c.getClass(), Battery.class);
		}	
	}
	
	public void immediateEffects(ATLASCore core, Optional<String> additionalData) {
		Battery b = (Battery)affectedComponent;
		
		if (b.getParent() instanceof Robot) {
			Robot r = (Robot)b.getParent();
			core.depleteEnergyOnRobot(r.getName(), (double)fixedEnergyLoss);
		}
	}

	public void completionEffects(ATLASCore core, Optional<String> additionalData) {
		
	}
}
