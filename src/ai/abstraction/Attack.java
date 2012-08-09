/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
        // move towards the unit:
        UnitAction move = AStar.findPathToAdjacentPosition(unit, target.getX()+target.getY()*gs.getPhysicalGameState().getWidth(), gs);
//        System.out.println("AStarAttak returns: " + move);
        if (move!=null) return move;
        
        // attack the unit:
        if (target.getX() == unit.getX() &&
            target.getY() == unit.getY()-1) return new UnitAction(UnitAction.TYPE_ATTACK,UnitAction.DIRECTION_UP,Unit.NONE);
        if (target.getX() == unit.getX()+1 &&
            target.getY() == unit.getY()) return new UnitAction(UnitAction.TYPE_ATTACK,UnitAction.DIRECTION_RIGHT,Unit.NONE);
        if (target.getX() == unit.getX() &&
            target.getY() == unit.getY()+1) return new UnitAction(UnitAction.TYPE_ATTACK,UnitAction.DIRECTION_DOWN,Unit.NONE);
        if (target.getX() == unit.getX()-1 &&
            target.getY() == unit.getY()) return new UnitAction(UnitAction.TYPE_ATTACK,UnitAction.DIRECTION_LEFT,Unit.NONE);
        return null;
    }    
}
