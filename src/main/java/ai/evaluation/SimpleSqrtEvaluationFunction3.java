/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.evaluation;

import rts.GameState;
import rts.PhysicalGameState;
import rts.units.*;

/**
 *
 * @author santi
 * 
 * This function uses the same base evaluation as SimpleSqrtEvaluationFunction and SimpleSqrtEvaluationFunction2, but returns the (proportion*2)-1 of the total score on the board that belongs to one player.
 * The advantage of this function is that evaluation is bounded between -1 and 1.
 */
public class SimpleSqrtEvaluationFunction3 extends EvaluationFunction {    
    public static float RESOURCE = 20;
    public static float RESOURCE_IN_WORKER = 10;
    public static float UNIT_BONUS_MULTIPLIER = 40.0f;
    
    
    public float evaluate(int maxplayer, int minplayer, GameState gs) {
        float s1 = base_score(maxplayer,gs);
        float s2 = base_score(minplayer,gs);
        if (s1 + s2 == 0) return 0.5f;
        return  (2*s1 / (s1 + s2))-1;
    }
    
    public float base_score(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        float score = gs.getPlayer(player).getResources()*RESOURCE;
        boolean anyunit = false;
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player) {
                anyunit = true;
                score += u.getResources() * RESOURCE_IN_WORKER;
                score += UNIT_BONUS_MULTIPLIER * u.getCost()*Math.sqrt( u.getHitPoints()/u.getMaxHitPoints() );
            }
        }
        if (!anyunit) return 0;
        return score;
    }    
    
    public float upperBound(GameState gs) {
        return 1.0f;
    }
}
