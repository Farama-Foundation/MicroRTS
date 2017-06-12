package ai.puppet;

import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ParameterSpecification;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import ai.core.InterruptibleAI;

public class PuppetNoPlan extends AIWithComputationBudget implements InterruptibleAI {

    PuppetBase puppet;

    public PuppetNoPlan(PuppetBase puppet) {
        super(puppet.MAX_TIME, puppet.MAX_ITERATIONS);
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
    public void startNewComputation(int player, GameState gs) throws Exception {
        puppet.restartSearch(gs, player);
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
        PuppetNoPlan clone = new PuppetNoPlan(puppet);
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
