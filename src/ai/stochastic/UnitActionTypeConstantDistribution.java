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
public class UnitActionTypeConstantDistribution extends UnitActionProbabilityDistribution {

    double m_distribution[] = null;
    
    public UnitActionTypeConstantDistribution(UnitTypeTable a_utt, double distribution[]) throws Exception {
        super(a_utt);
        
        if (distribution==null || distribution.length != UnitAction.NUMBER_OF_ACTION_TYPES) throw new Exception("distribution does not have the right number of elements!");
        m_distribution = distribution;
    }
    
    
    public double[] predictDistribution(Unit u, GameState gs, List<UnitAction> actions) throws Exception
    {
        int nActions = actions.size();
        double d[] = new double[nActions];
        double accum = 0;
        for(int i = 0;i<nActions;i++) {
            int type = actions.get(i).getType();
            d[i] = m_distribution[type];
            accum += d[i];
        }
        
        if (accum <= 0) {
            // if 0 accum, then just make uniform distribution:
            for(int i = 0;i<nActions;i++) d[i] = 1.0/nActions;
        } else {
            for(int i = 0;i<nActions;i++) d[i] /= accum;
        }
        
        return d;    
    }   
    
}
