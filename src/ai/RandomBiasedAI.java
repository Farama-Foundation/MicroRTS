/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import ai.core.AI;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import rts.*;
import rts.units.Unit;
import util.Sampler;

/**
 *
 * @author santi
 * 
 * This AI is similar to the RandomBiasedAI, btu instead of discarting "move" actions when there is an
 * attack available, it simply lowers the probability of a move.
 * 
 */
public class RandomBiasedAI extends AI {
    Random r = new Random();

    public void reset() {   
    }    
    
    public AI clone() {
        return new RandomBiasedAI();
    }
    
    public PlayerAction getAction(int player, GameState gs) {
        // attack, harvest and return have 5 times the probability of other actions
        double regularActionWeight = 1;
        double biasedActionWeight = 5;

        PhysicalGameState pgs = gs.getPhysicalGameState();
        PlayerAction pa = new PlayerAction();
        
        if (!gs.canExecuteAnyAction(player)) return pa;

        // Generate the reserved resources:
        for(Unit u:pgs.getUnits()) {
//            if (u.getPlayer()==pID) {
                UnitActionAssignment uaa = gs.getActionAssignment(u);
                if (uaa!=null) {
                    ResourceUsage ru = uaa.action.resourceUsage(u, pgs);
                    pa.getResourceUsage().merge(ru);
                }
//            }
        }
        
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player) {
                if (gs.getActionAssignment(u)==null) {
                    List<UnitAction> l = u.getUnitActions(gs);
                    UnitAction none = null;
                    int nActions = l.size();
                    double []distribution = new double[nActions];

                    // Implement "bias":
                    int i = 0;
                    for(UnitAction a:l) {
                        if (a.getType()==UnitAction.TYPE_NONE) none = a;
                        if (a.getType()==UnitAction.TYPE_ATTACK_LOCATION ||
                            a.getType()==UnitAction.TYPE_HARVEST ||
                            a.getType()==UnitAction.TYPE_RETURN) {
                            distribution[i]=biasedActionWeight;
                        } else {
                            distribution[i]=regularActionWeight;
                        }
                        i++;
                    }
                        
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
