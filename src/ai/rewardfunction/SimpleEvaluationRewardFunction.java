/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.rewardfunction;

import java.util.List;

import ai.evaluation.SimpleEvaluationFunction;
import rts.GameState;
import rts.PhysicalGameState;
import rts.TraceEntry;
import rts.UnitAction;
import rts.units.Unit;
import util.Pair;

/**
 *
 * @author costa
 */
public class SimpleEvaluationRewardFunction implements RewardFunctionInterface{
    public double reward = 0.0;
    public boolean done = false;
    public static float RESOURCE_RETURN_REWARD = 2;
    public static float RESOURCE_HARVEST_REWARD = 1;
    public static float UNIT_BONUS_MULTIPLIER = 4.0f;
    
    double oldReward = 0.0;
    boolean firstRewardCalculation = true;
    SimpleEvaluationFunction ef = new SimpleEvaluationFunction();

    
    public void computeReward(int maxplayer, int minplayer, TraceEntry te, GameState afterGs) {
        if (firstRewardCalculation) {
            oldReward = ef.evaluate(maxplayer, minplayer, afterGs);
            reward = 0;
            firstRewardCalculation = false;
        } else {
            double newReward = ef.evaluate(maxplayer, minplayer, afterGs);
            reward = newReward - oldReward;
            oldReward = newReward;
        }
        done = afterGs.gameover();
    }

    public double getReward() {
        return reward;
    }

    public boolean isDone() {
        return done;
    }
}
