/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.evaluation;

import rts.GameState;
import rts.PhysicalGameState;
import rts.UnitAction;
import rts.UnitActionAssignment;
import rts.units.*;

/**
 *
 * @author santi
 */
public class SimpleEvaluationFunction {
    public static float VICTORY = 10000;
    
    public static float RESOURCE = 20;
    public static float RESOURCE_IN_WORKER = 10;
    public static float UNIT_BONUS_MULTIPLIER = 40.0f;
    
    
    public static float evaluate(int maxplayer, int minplayer, GameState gs) {
        return base_score(maxplayer,gs) - base_score(minplayer,gs);
    }
    
    public static float base_score(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        float score = gs.getPlayer(player).getResources()*RESOURCE;
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player) {
                score += u.getResources() * RESOURCE_IN_WORKER;
                score += UNIT_BONUS_MULTIPLIER * (u.getCost()*u.getHitPoints())/(float)u.getMaxHitPoints();
/*                
                if (u instanceof Base) score += UNIT_BONUS_MULTIPLIER * (Base.BASE_COST*u.getHitPoints())/Base.BASE_HITPOINTS;
                if (u instanceof Barracks) score += UNIT_BONUS_MULTIPLIER * (Barracks.BARRACKS_COST*u.getHitPoints())/Barracks.BARRACKS_HITPOINTS;
                if (u instanceof Worker) {
                    score += UNIT_BONUS_MULTIPLIER * (Worker.WORKER_COST*u.getHitPoints())/Worker.WORKER_HITPOINTS;                    
                    score += u.getResources() * RESOURCE_IN_WORKER;
                }
                if (u instanceof Light) score += UNIT_BONUS_MULTIPLIER * (Light.LIGHT_COST*u.getHitPoints())/Light.LIGHT_HITPOINTS;
                if (u instanceof Heavy) score += UNIT_BONUS_MULTIPLIER * (Heavy.HEAVY_COST*u.getHitPoints())/Heavy.HEAVY_HITPOINTS;                                
                */
            }
        }
        return score;
    }    
    
    public static float upperBound(GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        int free_resources = 0;
        int player_resources[] = {gs.getPlayer(0).getResources(),gs.getPlayer(1).getResources()};
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==-1) free_resources+=u.getResources();
            if (u.getPlayer()==0) {
                player_resources[0] += u.getResources();
                player_resources[0] += u.getCost();
            }
            if (u.getPlayer()==1) {
                player_resources[1] += u.getResources();
                player_resources[1] += u.getCost();                
            }
        }
//        System.out.println(free_resources + " + [" + player_resources[0] + " , " + player_resources[1] + "]");
//        if (free_resources + player_resources[0] + player_resources[1]>62) {
//            System.out.println(gs);
//        }
        return (free_resources + Math.max(player_resources[0],player_resources[1]))*UNIT_BONUS_MULTIPLIER;
    }
}
