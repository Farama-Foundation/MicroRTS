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
public class Idle extends AbstractAction  {
    
    public Idle(Unit u) {
        super(u);
    }
    
    public boolean completed(GameState gs) {
        return false;
    }

    public UnitAction execute(GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        if (!unit.getType().canAttack) return null;
        for(Unit target:pgs.getUnits()) {
            if (target.getPlayer()!=-1 && target.getPlayer()!=unit.getPlayer()) {
                int dx = target.getX()-unit.getX();
                int dy = target.getY()-unit.getY();
                double d = Math.sqrt(dx*dx+dy*dy);
                if (d<=unit.getAttackRange()) {
                    return new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,target.getX(),target.getY());
                }
            }
        }
        return null;
    }    
}
