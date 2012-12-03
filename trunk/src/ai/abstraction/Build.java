/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction;

import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import java.util.LinkedList;
import java.util.List;
import rts.GameState;
import rts.PhysicalGameState;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;

/**
 *
 * @author santi
 */
public class Build extends AbstractAction  {
    UnitType type;
    int x,y;
    PathFinding pf;
    
    public Build(Unit u, UnitType a_type, int a_x, int a_y, PathFinding a_pf) {
        super(u);
        type = a_type;
        x = a_x;
        y = a_y;
        pf = a_pf;
    }

    public boolean completed(GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit u = pgs.getUnitAt(x, y);
        if (u!=null) return true;
        return false;
    }

    public UnitAction execute(GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        UnitAction move = pf.findPathToAdjacentPosition(unit, x+y*pgs.getWidth(), gs);
        if (move!=null) return move;

        // build:
        if (x == unit.getX() &&
            y == unit.getY()-1) return new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_UP,type);
        if (x == unit.getX()+1 &&
            y == unit.getY()) return new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_RIGHT,type);
        if (x == unit.getX() &&
            y == unit.getY()+1) return new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_DOWN,type);
        if (x == unit.getX()-1 &&
            y == unit.getY()) return new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_LEFT,type);

        System.err.println("Harvest.execute: something weird just happened " + unit + " builds at " + x + "," + y);
        return null;
    } 
}
