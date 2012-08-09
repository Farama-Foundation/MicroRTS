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
public class Move extends AbstractAction {

    int x,y;
    
    public Move(Unit u, int a_x, int a_y) {
        super(u);
        x = a_x;
        y = a_y;
    }
    
    public boolean completed(GameState gs) {
        if (unit.getX()==x && unit.getY()==y) return true;
        return false;
    }

    public UnitAction execute(GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        UnitAction move = AStar.findPath(unit, x+y*pgs.getWidth(), gs);
//        System.out.println("AStarAttak returns: " + move);
        if (move!=null) return move;
        return null;
    }
}
