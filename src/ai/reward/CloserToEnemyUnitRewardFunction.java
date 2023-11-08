/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.reward;
import rts.GameState;
import rts.TraceEntry;
import rts.units.Unit;

/**
 *
 * @author costa
 */
public class CloserToEnemyUnitRewardFunction extends RewardFunctionInterface{

    public void computeReward(int maxplayer, int minplayer, TraceEntry te, GameState afterGs) {
        reward = 0.0;
        done = false;
        int baseX = 0;
        int baseY = 0;
        boolean baseExists = false;
        for(Unit t: te.getPhysicalGameState().getUnits()) {
            if (t.getPlayer() == minplayer && t.getType().name.equals("Base")) {
                baseExists = true;
                baseX = t.getX();
                baseY = t.getY();
                break;
            }
        }
        if (!baseExists) {
            return;
        }
        double oldMinDistanceToEnemyBase = 2000000000;
        for(Unit t: te.getPhysicalGameState().getUnits()) {
            if (t.getPlayer() == maxplayer && (
                t.getType().name.equals("Light") || t.getType().name.equals("Heavy") || 
                t.getType().name.equals("Ranged") || t.getType().name.equals("Worker"))) {
                // Euclidean distance
                double distance = Math.sqrt(Math.pow((baseX-t.getX()), 2.0) + Math.pow((baseY-t.getY()), 2.0));
                if (distance < oldMinDistanceToEnemyBase) {
                    oldMinDistanceToEnemyBase = distance;
                }
            }
        }


        double newMinDistanceToEnemyBase = 2000000000;
        for(Unit t: afterGs.getPhysicalGameState().getUnits()) {
            if (t.getPlayer() == maxplayer && (
                t.getType().name.equals("Light") || t.getType().name.equals("Heavy") || 
                t.getType().name.equals("Ranged") || t.getType().name.equals("Worker"))) {
                // Euclidean distance
                double distance = Math.sqrt(Math.pow((baseX-t.getX()), 2.0) + Math.pow((baseY-t.getY()), 2.0));
                if (distance < newMinDistanceToEnemyBase) {
                    newMinDistanceToEnemyBase = distance;
                }
            }
        }
        reward = oldMinDistanceToEnemyBase - newMinDistanceToEnemyBase;
    }

    public double getReward() {
        return reward;
    }

    public boolean isDone() {
        return done;
    }
}
