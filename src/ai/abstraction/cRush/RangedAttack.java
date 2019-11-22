/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction.cRush;

import ai.abstraction.AbstractAction;
import ai.abstraction.pathfinding.PathFinding;
import rts.GameState;
import rts.PhysicalGameState;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
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
        return !pgs.getUnits().contains(target);
    }
    
    
    public boolean equals(Object o)
    {
        if (!(o instanceof RangedAttack)) return false;
        RangedAttack a = (RangedAttack)o;
        return target.getID() == a.target.getID() && pf.getClass() == a.pf.getClass()
            && racks.getID() == a.racks.getID();
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
