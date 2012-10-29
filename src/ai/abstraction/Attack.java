/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction;

import rts.GameState;
import rts.PhysicalGameState;
import rts.UnitAction;
import rts.units.Unit;

/**
 *
 * @author santi
 */
public class Attack extends AbstractAction  {
    Unit target;
    
    public Attack(Unit u, Unit a_target) {
        super(u);
        target = a_target;
    }
    
    public boolean completed(GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        if (!pgs.getUnits().contains(target)) return true;
        return false;
    }

    public UnitAction execute(GameState gs) {
        
        int dx = target.getX()-unit.getX();
        int dy = target.getY()-unit.getY();
        double d = Math.sqrt(dx*dx+dy*dy);
        if (d<=unit.getAttackRange()) {
            return new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,target.getX(),target.getY());
        } else {
            // move towards the unit:
            UnitAction move = AStar.findPathToAdjacentPosition(unit, target.getX()+target.getY()*gs.getPhysicalGameState().getWidth(), gs);
    //        System.out.println("AStarAttak returns: " + move);
            return move;    
        }        
    }    
}
