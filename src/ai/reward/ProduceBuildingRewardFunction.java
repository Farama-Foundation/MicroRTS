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
public class ProduceBuildingRewardFunction extends RewardFunctionInterface{

    public static float BUILDING_PRODUCE_REWARD = 1;

    public void computeReward(int maxplayer, int minplayer, TraceEntry te, GameState afterGs) {
        reward = 0.0;
        done = false;
        for(Pair<Unit, UnitAction> p:te.getActions()) {
            if (p.m_a.getPlayer()==maxplayer && p.m_b.getType()==UnitAction.TYPE_PRODUCE && p.m_b.getUnitType()!=null) {
                if (p.m_b.getUnitType().name.equals("Barracks") || p.m_b.getUnitType().name.equals("Base")) {
                    reward += BUILDING_PRODUCE_REWARD;
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
