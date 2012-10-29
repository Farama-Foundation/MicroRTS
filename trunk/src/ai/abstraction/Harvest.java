/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction;

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
public class Harvest extends AbstractAction  {
    Unit target;
    Unit base;
    
    public Harvest(Unit u, Unit a_target, Unit a_base) {
        super(u);
        target = a_target;
        base = a_base;
    }
    
    public boolean completed(GameState gs) {
        if (!gs.getPhysicalGameState().getUnits().contains(target)) return true;
        return false;
    }

    
    public UnitAction execute(GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        if (unit.getResources()==0) {
            // go get resources:
            UnitAction move = AStar.findPathToAdjacentPosition(unit, target.getX()+target.getY()*gs.getPhysicalGameState().getWidth(), gs);
            if (move!=null) return move;

            // harvest:
            if (target.getX() == unit.getX() &&
                target.getY() == unit.getY()-1) return new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_UP);
            if (target.getX() == unit.getX()+1 &&
                target.getY() == unit.getY()) return new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_RIGHT);
            if (target.getX() == unit.getX() &&
                target.getY() == unit.getY()+1) return new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_DOWN);
            if (target.getX() == unit.getX()-1 &&
                target.getY() == unit.getY()) return new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_LEFT);
        } else {
            // return resources:
            UnitAction move = AStar.findPathToAdjacentPosition(unit, base.getX()+base.getY()*gs.getPhysicalGameState().getWidth(), gs);
            if (move!=null) return move;

            // harvest:
            if (base.getX() == unit.getX() &&
                base.getY() == unit.getY()-1) return new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_UP);
            if (base.getX() == unit.getX()+1 &&
                base.getY() == unit.getY()) return new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_RIGHT);
            if (base.getX() == unit.getX() &&
                base.getY() == unit.getY()+1) return new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_DOWN);
            if (base.getX() == unit.getX()-1 &&
                base.getY() == unit.getY()) return new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_LEFT);
        }
        return null;
    }    
}
