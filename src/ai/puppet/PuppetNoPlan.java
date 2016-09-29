package ai.puppet;

import ai.core.AI;
import ai.core.InterruptibleAIWithComputationBudget;
import rts.GameState;
import rts.PlayerAction;

public class PuppetNoPlan extends InterruptibleAIWithComputationBudget {

	PuppetBase puppet;
	public PuppetNoPlan(PuppetBase puppet) {
		super(puppet.MAX_TIME, puppet.MAX_ITERATIONS);
		this.puppet=puppet;
	}

	@Override
	public void startNewComputation(int player, GameState gs) throws Exception {
		puppet.restartSearch(gs,player);
	}

	@Override
	public void computeDuringOneGameFrame() throws Exception {
		puppet.computeDuringOneGameFrame();

	}

	@Override
	public PlayerAction getBestActionSoFar() throws Exception {
		return puppet.getBestActionSoFar();
	}

	@Override
	public void reset() {
		puppet.reset();
	}

	@Override
	public AI clone() {
		PuppetNoPlan clone=new PuppetNoPlan(puppet);
		return clone;
	}

	public String toString(){
		return "PuppetNoPlan("+puppet.toString()+")";
	}
	
	@Override
	public String statisticsString() {
		return puppet.statisticsString();
	}   
}
