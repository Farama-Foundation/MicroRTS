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
public class EvaluationFunctionForwarding extends EvaluationFunction{
    
    public static float evaluate(int maxplayer, int minplayer, GameState gs) {
        GameState gs2 = gs.clone();
        gs2.forceExecuteAllActions();
        
        /*
        System.out.println("Players: " + maxplayer + " , " + minplayer);
        System.out.println("Score: " + basicScore(maxplayer,minplayer,gs) + " +  0.5 * " + basicScore(maxplayer,minplayer,gs2));
        PhysicalGameState pgs2 = gs2.getPhysicalGameState();
        System.out.println("Detailed for gs2 (max): " + bases(maxplayer,pgs2) + " , " + barracks(maxplayer,pgs2) + " , " + workers(maxplayer,pgs2) + " , " + melee(maxplayer,pgs2) + " , " + resources(maxplayer,gs));
        System.out.println("Detailed for gs2 (min): " + bases(minplayer,pgs2) + " , " + barracks(minplayer,pgs2) + " , " + workers(minplayer,pgs2) + " , " + melee(minplayer,pgs2) + " , " + resources(minplayer,gs));
        System.out.println("Forwarded state:\n" + gs2);
        */
//        return SimpleEvaluationFunction.evaluate(maxplayer, minplayer, gs) + 0.5f * SimpleEvaluationFunction.evaluate(maxplayer,minplayer,gs2);
        return basicScore(maxplayer,minplayer,gs) + 0.5f*basicScore(maxplayer,minplayer,gs2);
    }
     
    static float basicScore(int maxplayer, int minplayer, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        float score = 0;      
        
        score += bases(maxplayer,pgs);
        score += barracks(maxplayer,pgs);
        score += workers(maxplayer,pgs);
        score += melee(maxplayer,pgs);
        score += resources(maxplayer,gs);

        score -= bases(minplayer,pgs);
        score -= barracks(minplayer,pgs);
        score -= workers(minplayer,pgs);
        score -= melee(minplayer,pgs);
        score -= resources(minplayer,gs);
        
        return score;
    }
}
