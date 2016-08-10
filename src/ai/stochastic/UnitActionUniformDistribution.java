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
public class UnitActionUniformDistribution extends UnitActionProbabilityDistribution {

    public UnitActionUniformDistribution(UnitTypeTable a_utt) throws Exception {
        super(a_utt);
    }
    
    
    public double[] predictDistribution(Unit u, GameState gs, List<UnitAction> actions) throws Exception
    {
        int nActions = actions.size();
        double d[] = new double[nActions];
        for(int i = 0;i<nActions;i++) d[i] = 1.0/nActions;
        
        return d;    
    }   
    
}
