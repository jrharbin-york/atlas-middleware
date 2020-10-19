package atlassharedclasses;

public class ActivateBehaviour extends BehaviourCommand {
	private Behaviour chosenBehaviour;
	
	public ActivateBehaviour(Behaviour chosenBehaviour) {
		this.setChosenBehaviour(chosenBehaviour);
	}

	public Behaviour getChosenBehaviour() {
		return chosenBehaviour;
	}

	public void setChosenBehaviour(Behaviour chosenBehaviour) {
		this.chosenBehaviour = chosenBehaviour;
	}
}
