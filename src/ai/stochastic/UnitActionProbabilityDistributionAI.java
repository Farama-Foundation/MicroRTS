/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.stochastic;

import ai.core.AI;
import java.util.List;
import java.util.Random;
import ai.stochastic.UnitActionProbabilityDistribution;
import rts.*;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Sampler;

/**
 *
 * @author santi
 * 
 */
public class UnitActionProbabilityDistributionAI extends AI {
    public static int DEBUG = 0;
    
    Random r = new Random();
    UnitActionProbabilityDistribution model = null;
    String modelName = "";  // name of the model for the toString method, so it can be identified
    UnitTypeTable utt = null;
    
    public UnitActionProbabilityDistributionAI(UnitActionProbabilityDistribution a_model, UnitTypeTable a_utt, String a_modelName) {
        model = a_model;
        utt = a_utt;
        modelName = a_modelName;
    }
    
    
    public String toString() {
        return this.getClass().getSimpleName()+"("+modelName+")";
    }   
    
    
    public void reset() {   
    }    
    
    
    public AI clone() {
        return new UnitActionProbabilityDistributionAI(model, utt, modelName);
    }
    
    
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.getUnitTypeTable() != utt) throw new Exception("UnitActionDistributionAI uses a UnitTypeTable different from the one used to play!");
        PhysicalGameState pgs = gs.getPhysicalGameState();
        PlayerAction pa = new PlayerAction();
        
        if (!gs.canExecuteAnyAction(player)) return pa;

        // Generate the reserved resources:
        for(Unit u:pgs.getUnits()) {
            UnitActionAssignment uaa = gs.getActionAssignment(u);
            if (uaa!=null) {
                ResourceUsage ru = uaa.action.resourceUsage(u, pgs);
                pa.getResourceUsage().merge(ru);
            }
        }
        
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player) {
                if (gs.getActionAssignment(u)==null) {
                    List<UnitAction> l = u.getUnitActions(gs);
                    double []distribution = model.predictDistribution(u, gs, l);
                    UnitAction none = null;
                    for(UnitAction ua:l) 
                        if (ua.getType()==UnitAction.TYPE_NONE) none = ua;
                    
                    try {
                        UnitAction ua = l.get(Sampler.weighted(distribution));
                        if (ua.resourceUsage(u, pgs).consistentWith(pa.getResourceUsage(), gs)) {
                            ResourceUsage ru = ua.resourceUsage(u, pgs);
                            pa.getResourceUsage().merge(ru);                        
                            pa.addUnitAction(u, ua);
                        } else {
                            pa.addUnitAction(u, none);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        pa.addUnitAction(u, none);
                    }
                }
            }
        }
        
        return pa;
    }
    
}
