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
public class EvaluationFunctionForwarding extends EvaluationFunction {
    
    EvaluationFunction baseFunction = null;

    public EvaluationFunctionForwarding(EvaluationFunction base) {
        baseFunction = base;
    }
    
    
    public float evaluate(int maxplayer, int minplayer, GameState gs) {
        GameState gs2 = gs.clone();
        gs2.forceExecuteAllActions();
        
        return baseFunction.evaluate(maxplayer,minplayer,gs) + 
               baseFunction.evaluate(maxplayer,minplayer,gs2) * 0.5f;
    }
    
    public float upperBound(GameState gs) {
        return baseFunction.upperBound(gs)*1.5f;
    }
}
