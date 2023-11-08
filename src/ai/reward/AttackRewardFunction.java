/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.reward;
import rts.GameState;
import rts.TraceEntry;
import rts.UnitAction;
import rts.units.Unit;
import util.Pair;

/**
 *
 * @author costa
 */
public class AttackRewardFunction extends RewardFunctionInterface {

    public static float ATTACK_REWARD = 1;

    public void computeReward(int maxplayer, int minplayer, TraceEntry te, GameState afterGs) {
        reward = 0.0;
        done = false;
        for (Pair<Unit, UnitAction> p : te.getActions()) {
            if (p.m_a.getPlayer() == maxplayer && p.m_b.getType() == UnitAction.TYPE_ATTACK_LOCATION) {
                Unit other = te.getPhysicalGameState().getUnitAt(p.m_b.getLocationX(), p.m_b.getLocationY());
                if (other != null) {
                	if (other.getPlayer() == minplayer) {
                		reward += ATTACK_REWARD;		// Positive reward for attacking opponent
                	}
                	else if (other.getPlayer() == maxplayer) {
                		reward -= ATTACK_REWARD;		// Negative reward for attacking self
                	}
                }
            }
        }
    }

    public double getReward() {
        return reward;
    }

    public boolean isDone() {
        return done;
    }
}
