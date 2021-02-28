/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLBasicConditional.functions;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author rubens
 */
public interface IConditionalFunction {
    /**
     * 
     * @param lParam1 = GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt
     * @return 
     */
    public boolean runFunction(List lParam1, HashMap<Long, String> counterByFunction);
    public void setDSLUsed();
    
}
