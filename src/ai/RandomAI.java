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
                    UnitAction ua = l.get(r.nextInt(l.size()));
                    ResourceUsage ru = ua.resourceUsage(u, pgs);
                    if (ru.consistentWith(pa.getResourceUsage(), gs)) {
                        pa.getResourceUsage().merge(ru);                        
                        pa.addUnitAction(u, ua);
                    } else {
                        pa.addUnitAction(u, new UnitAction(UnitAction.TYPE_NONE));
                    }
                }
            }
        }
        
        return pa;
    }
}
