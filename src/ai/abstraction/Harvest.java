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
public class Harvest extends AbstractAction  {
    Unit target;
    Unit base;
    PathFinding pf;
    
    public Harvest(Unit u, Unit a_target, Unit a_base, PathFinding a_pf) {
        super(u);
        target = a_target;
        base = a_base;
        pf = a_pf;
    }
    
    
    public Unit getTarget() {
        return target;
    }
    
    
    public Unit getBase() {
        return base;
    }
    
    
    public boolean completed(GameState gs) {
        if (!gs.getPhysicalGameState().getUnits().contains(target)) return true;
        return false;
    }
    
    
    public boolean equals(Object o)
    {
        if (!(o instanceof Harvest)) return false;
        Harvest a = (Harvest)o;
        if (target.getID() != a.target.getID()) return false;
        if (base.getID() != a.base.getID()) return false;
        if (pf.getClass() != a.pf.getClass()) return false;
        
        return true;
    }
    

    public void toxml(XMLWriter w)
    {
        w.tagWithAttributes("Harvest","unitID=\""+unit.getID()+"\" target=\""+target.getID()+"\" base=\""+base.getID()+"\" pathfinding=\""+pf.getClass().getSimpleName()+"\"");
        w.tag("/Harvest");
    }           
    
    public UnitAction execute(GameState gs, ResourceUsage ru) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        if (unit.getResources()==0) {
            // go get resources:
//            System.out.println("findPathToAdjacentPosition from Harvest: (" + target.getX() + "," + target.getY() + ")");
            UnitAction move = pf.findPathToAdjacentPosition(unit, target.getX()+target.getY()*gs.getPhysicalGameState().getWidth(), gs, ru);
            if (move!=null) {
                if (gs.isUnitActionAllowed(unit, move)) return move;
                return null;
            }

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
//            System.out.println("findPathToAdjacentPosition from Return: (" + target.getX() + "," + target.getY() + ")");
            UnitAction move = pf.findPathToAdjacentPosition(unit, base.getX()+base.getY()*gs.getPhysicalGameState().getWidth(), gs, ru);
            if (move!=null) {
                if (gs.isUnitActionAllowed(unit, move)) return move;
                return null;
            }

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
