/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.reward;
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
public class ResourceGatherRewardFunction extends RewardFunctionInterface{

    public static float RESOURCE_RETURN_REWARD = 1;
    public static float RESOURCE_HARVEST_REWARD = 1;

    public void computeReward(int maxplayer, int minplayer, TraceEntry te, GameState afterGs) {
        reward = 0.0;
        for(Pair<Unit, UnitAction> p:te.getActions()) {
            if (p.m_a.getPlayer()==maxplayer && p.m_b.getType()==UnitAction.TYPE_HARVEST) {
                reward += RESOURCE_HARVEST_REWARD;
            } else if (p.m_a.getPlayer()==maxplayer && p.m_b.getType()==UnitAction.TYPE_RETURN) {
                reward += RESOURCE_RETURN_REWARD;
            }
        }
        done = true;
        PhysicalGameState pgs = afterGs.getPhysicalGameState();
        for(Unit u:pgs.getUnits()) {
            // If there are Resources left, it's not done
            if (u.getType().name.equals("Resource")) {
                if (u.getResources()>0) {
                    done = false;
                    return;
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
