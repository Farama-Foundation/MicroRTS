/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.stochastic;

import java.util.List;
import rts.GameState;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public abstract class UnitActionProbabilityDistribution {
    protected UnitTypeTable utt = null;
    
    public UnitActionProbabilityDistribution(UnitTypeTable a_utt) {
        utt = a_utt;
    }
            
    public abstract double[] predictDistribution(Unit u, GameState gs, List<UnitAction> actions) throws Exception;
                
}
