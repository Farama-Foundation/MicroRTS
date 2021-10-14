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
public class WinLossRewardFunction extends RewardFunctionInterface{


    public void computeReward(int maxplayer, int minplayer, TraceEntry te, GameState afterGs) {
        reward = 0.0;
        done = false;
        if (afterGs.gameover()) {
            done = true;
            reward = afterGs.winner()==maxplayer ? 1.0 : -1.0;
        }

    }
}
