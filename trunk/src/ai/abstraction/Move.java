/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction;

import ai.abstraction.pathfinding.PathFinding;
import rts.GameState;
import rts.PhysicalGameState;
import rts.UnitAction;
import rts.units.Unit;

/**
 *
 * @author santi
 */
public class Move extends AbstractAction {

    int x,y;
    PathFinding pf;

    
    public Move(Unit u, int a_x, int a_y, PathFinding a_pf) {
        super(u);
        x = a_x;
        y = a_y;
        pf = a_pf;
    }
    
    public boolean completed(GameState gs) {
        if (unit.getX()==x && unit.getY()==y) return true;
        return false;
    }

    public UnitAction execute(GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        UnitAction move = pf.findPath(unit, x+y*pgs.getWidth(), gs, null);
//        System.out.println("AStarAttak returns: " + move);
        if (move!=null && gs.isUnitActionAllowed(unit, move)) return move;
        return null;
    }
}
