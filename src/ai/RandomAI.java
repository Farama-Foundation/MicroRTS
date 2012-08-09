/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import java.util.List;
import java.util.Random;
import rts.*;
import rts.units.Unit;

/**
 *
 * @author santi
 */
public class RandomAI extends AI {
    Random r = new Random();

    public void reset() {
    }
    
    public AI clone() {
        return new RandomAI();
    }
   
    public PlayerAction getAction(int player, GameState gs) {
        return getRandomPlayerAction(player, gs);
    }
    
    public PlayerAction getRandomPlayerAction(int pID, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        PlayerAction pa = new PlayerAction();
        
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
            if (u.getPlayer()==pID) {
                if (gs.getActionAssignment(u)==null) {
                    List<UnitAction> l = u.getUnitActions(gs);
                    UnitAction ua = l.get(r.nextInt(l.size()));
                    if (ua.resourceUsage(u, pgs).consistentWith(pa.getResourceUsage(), gs)) {
                        ResourceUsage ru = ua.resourceUsage(u, pgs);
                        pa.getResourceUsage().merge(ru);                        
                        pa.addUnitAction(u, ua);
                    } else {
                        pa.addUnitAction(u, new UnitAction(UnitAction.TYPE_NONE,UnitAction.DIRECTION_NONE,Unit.NONE));
                    }
                }
            }
        }
        
        return pa;
    }
}
