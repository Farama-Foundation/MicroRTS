/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction;

import ai.abstraction.pathfinding.PathFinding;
import rts.GameState;
import rts.PhysicalGameState;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import util.XMLWriter;

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
        return unit.getX() == x && unit.getY() == y;
    }
    
    
    public boolean equals(Object o)
    {
        if (!(o instanceof Move)) return false;
        Move a = (Move)o;
        return x == a.x && y == a.y && pf.getClass() == a.pf.getClass();
    }

    
    public void toxml(XMLWriter w)
    {
        w.tagWithAttributes("Move","unitID=\""+unit.getID()+"\" x=\""+x+"\" y=\""+y+"\" pathfinding=\""+pf.getClass().getSimpleName()+"\"");
        w.tag("/Move");
    }       

    public UnitAction execute(GameState gs, ResourceUsage ru) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        UnitAction move = pf.findPath(unit, x+y*pgs.getWidth(), gs, ru);
//        System.out.println("AStarAttak returns: " + move);
        if (move!=null && gs.isUnitActionAllowed(unit, move)) return move;
        return null;
    }
}
