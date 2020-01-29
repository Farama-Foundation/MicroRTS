/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.rewardfunction;

import java.util.List;

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
public class WinLossRewardFunction implements RewardFunctionInterface{
    public double reward = 0.0;
    public boolean done = false;

    public void computeReward(int maxplayer, int minplayer, TraceEntry te, GameState afterGs) {
        reward = 0.0;
        done = false;
        if (afterGs.winner()==maxplayer) {
            reward = 1.0;
            done = true;
        }
        else if (afterGs.winner()==minplayer) {
            reward = -1.0;
            done = true;
        }
    }

    public double getReward() {
        return reward;
    }

    public boolean isDone() {
        return done;
    }
}
