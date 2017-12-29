/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction.cRush;

import ai.abstraction.AbstractAction;
import ai.abstraction.Attack;
import ai.abstraction.pathfinding.PathFinding;
import rts.GameState;
import rts.PhysicalGameState;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author Cristiano D'Angelo
 */
public class RangedAttack extends AbstractAction  {
    Unit target;
    PathFinding pf;
    Unit racks;
    UnitType workerType;
    UnitType rangedType;
    UnitType heavyType;
    
    public RangedAttack(Unit u, Unit a_target, Unit r, PathFinding a_pf) {
        super(u);
        target = a_target;
        pf = a_pf;
        racks = r;
    }
    
    public boolean completed(GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        if (!pgs.getUnits().contains(target)) return true;
        return false;
    }
    
    
    public boolean equals(Object o)
    {
        if (!(o instanceof RangedAttack)) return false;
        RangedAttack a = (RangedAttack)o;
        if (target.getID() != a.target.getID()) return false;
        if (pf.getClass() != a.pf.getClass()) return false;
        if (racks.getID() != a.racks.getID()) return false;
        
        return true;
    }

    
    public void toxml(XMLWriter w)
    {
        w.tagWithAttributes("RangedAttack","unitID=\""+getUnit().getID()+"\" target=\""+target.getID()+"\" pathfinding=\""+pf.getClass().getSimpleName()+"\" racks=\""+racks.getID()+"\"");
        w.tag("/RangedAttack");
    }
    

    public UnitAction execute(GameState gs, ResourceUsage ru) {
        
        int rdx = 0;
        int rdy = 0;
        double rd = 0.0;
        
        if(racks != null){
            rdx = racks.getX()-getUnit().getX();
            rdy = racks.getY()-getUnit().getY();
            rd = Math.sqrt(rdx*rdx+rdy*rdy);
        }
        int dx = target.getX()-getUnit().getX();
        int dy = target.getY()-getUnit().getY();
        double d = Math.sqrt(dx*dx+dy*dy);
       
        
        
        if(d <= (getUnit().getAttackRange()) - 1 && rd > 2 && getUnit().getMoveTime() < target.getMoveTime()){
            UnitAction move = pf.findPathToPositionInRange(getUnit(), racks.getX()+racks.getY()*gs.getPhysicalGameState().getWidth(), getUnit().getAttackRange(), gs, ru);
            if (move!=null && gs.isUnitActionAllowed(getUnit(), move)) return move;
            return null;
        }
        else if (d<=getUnit().getAttackRange()) {
            return new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,target.getX(),target.getY());
        } else {
            // move towards the unit:
    //        System.out.println("AStarAttak returns: " + move);
            UnitAction move = pf.findPathToPositionInRange(getUnit(), target.getX()+target.getY()*gs.getPhysicalGameState().getWidth(), getUnit().getAttackRange(), gs, ru);
            if (move!=null && gs.isUnitActionAllowed(getUnit(), move)) return move;
            return null;
        }        
    }    
}
