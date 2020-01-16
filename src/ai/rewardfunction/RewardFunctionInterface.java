package ai.rewardfunction;

import rts.GameState;
import rts.TraceEntry;

/**
 *
 * @author costa
 */
public interface RewardFunctionInterface {    
    // double oldReward = 0.0;
    // boolean firstRewardCalculation = true;
    // SimpleEvaluationFunction ef = new SimpleEvaluationFunction();

    public abstract void computeReward(int maxplayer, int minplayer, TraceEntry te, GameState afterGs);

    public double getReward();

    public boolean isDone();

    // public void setReward(double reward) {
    //     this.reward = reward;
    // }

    // public void setDone(boolean done) {
    //     this.done = done;
    // }
}
