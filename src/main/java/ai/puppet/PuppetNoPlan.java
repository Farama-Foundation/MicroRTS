package ai.puppet;

import ai.abstraction.pathfinding.FloodFillPathFinding;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ParameterSpecification;
import ai.evaluation.SimpleSqrtEvaluationFunction3;

import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import ai.core.InterruptibleAI;

public class PuppetNoPlan extends AIWithComputationBudget implements InterruptibleAI {

    PuppetBase puppet;
    
    //By default use ABCD
    public PuppetNoPlan(UnitTypeTable utt) {
    	this(new PuppetSearchAB(100, -1,
             -1, -1,
             100,
             new BasicConfigurableScript(utt, new FloodFillPathFinding()),
             new SimpleSqrtEvaluationFunction3()));
    }
    public PuppetNoPlan(PuppetBase puppet) {
        super(puppet.getTimeBudget(), puppet.getIterationsBudget());
        this.puppet = puppet;
    }

    public final PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.canExecuteAnyAction(player)) {
            startNewComputation(player, gs.clone());
            computeDuringOneGameFrame();
            return getBestActionSoFar();
        } else {
            return new PlayerAction();
        }
    }

    @Override
	public void setTimeBudget(int a_tb) {
		puppet.setTimeBudget(a_tb);
	}

	@Override
	public int getTimeBudget() {
		return puppet.getTimeBudget();
	}
	
    @Override
	public int getIterationsBudget() {
		return puppet.getIterationsBudget();
	}

	@Override
	public void setIterationsBudget(int a_ib) {
		puppet.setIterationsBudget(a_ib);
	}
	
	@Override
    public void startNewComputation(int player, GameState gs) throws Exception {
        puppet.startNewComputation(player, gs);
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
        PuppetNoPlan clone = new PuppetNoPlan((PuppetBase)puppet.clone());
        return clone;
    }

    public String toString() {
        return getClass().getSimpleName() + "(" + puppet.toString() + ")";
    }

    @Override
    public String statisticsString() {
        return puppet.statisticsString();
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        return puppet.getParameters();
    }
}
