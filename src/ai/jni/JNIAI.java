package ai.jni;

import java.util.List;

import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ParameterSpecification;
import ai.evaluation.SimpleEvaluationFunction;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 * This AI does not actually have the ability to make its own decisions for actions.
 * It is a helper class that we can pass actions that we have already decided upon
 * externally (e.g., in Python code) in vector-format, and this class can convert
 * them for us into the PlayerAction format.
 *
 * @author costa
 */
public class JNIAI extends AIWithComputationBudget implements JNIInterface {
    UnitTypeTable utt = null;
    double reward = 0.0;
    double oldReward = 0.0;
    boolean firstRewardCalculation = true;
    SimpleEvaluationFunction ef = new SimpleEvaluationFunction();
    int maxAttackRadius;

    public JNIAI(int timeBudget, int iterationsBudget, UnitTypeTable a_utt) {
        super(timeBudget, iterationsBudget);
        utt = a_utt;
        maxAttackRadius = utt.getMaxAttackRange() * 2 + 1;
    }

    @Override
    public double computeReward(final int maxplayer, final int minplayer, final GameState gs) throws Exception {
        // do something
        if (firstRewardCalculation) {
            oldReward = ef.evaluate(maxplayer, minplayer, gs);
            reward = 0;
            firstRewardCalculation = false;
        } else {
            double newReward = ef.evaluate(maxplayer, minplayer, gs);
            reward = newReward - oldReward;
            oldReward = newReward;
        }
        return reward;
    }

    @Override
	public PlayerAction getAction(final int player, final GameState gs, final int[][] action) throws Exception {
        PlayerAction pa = PlayerAction.fromVectorAction(action, gs, utt, player, maxAttackRadius);
        pa.fillWithNones(gs, player, 1);
        return pa;
    }

    @Override
	public int[][][] getObservation(final int player, final GameState gs) throws Exception {
        return gs.getVectorObservation(player);
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
    }

    @Override
    public AI clone() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String computeInfo(int player, GameState gs) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
    
}
