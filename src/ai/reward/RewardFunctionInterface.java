/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.reward;
import rts.GameState;
import rts.TraceEntry;

/**
 *
 * @author costa
 */
public abstract class RewardFunctionInterface {    
    public double reward = 0.0;
    public boolean done = false;

    public abstract void computeReward(int maxplayer, int minplayer, TraceEntry te, GameState afterGs);

    public double getReward() {
        return reward;
    }

    public boolean isDone() {
        return done;
    }

    public String toString() {
        return getClass().getSimpleName();
    }
}
