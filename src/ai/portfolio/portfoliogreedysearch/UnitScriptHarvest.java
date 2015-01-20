/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.portfolio.portfoliogreedysearch;

import ai.abstraction.AbstractAction;
import ai.abstraction.Harvest;
import ai.abstraction.Idle;
import ai.abstraction.pathfinding.PathFinding;
import rts.GameState;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class UnitScriptHarvest extends UnitScript {
    
    AbstractAction action = null;
    PathFinding pf = null;
    UnitTypeTable utt = null;
    
    public UnitScriptHarvest(PathFinding a_pf, UnitTypeTable a_utt) {
        pf = a_pf;
        utt = a_utt;
    }
    
    public UnitAction getAction(Unit u, GameState gs) {
        if (action.completed(gs)) {
            return null;
        } else {
            return action.execute(gs);
        }
    }
    
    public UnitScript instantiate(Unit u, GameState gs) {
        Unit closestResource = closestUnitOfType(u, gs, utt.getUnitType("Resource"), null);
        Unit closestBase = closestUnitOfType(u, gs, utt.getUnitType("Base"), u.getPlayer());
        if (closestResource != null && closestBase != null) {
            UnitScriptHarvest script = new UnitScriptHarvest(pf, utt);
            script.action = new Harvest(u, closestResource, closestBase, pf);
            return script;
        } else {
            return null;
        }
    }
    
    
    public Unit closestUnitOfType(Unit u, GameState gs, UnitType type, Integer player) {
        Unit closest = null;
        int closestDistance = 0;
        for (Unit u2 : gs.getPhysicalGameState().getUnits()) {
            if (u2.getType() == type) {
                if (player!=null && u2.getPlayer()!=player) continue;
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closest == null || d < closestDistance) {
                    closest = u2;
                    closestDistance = d;
                }
            }
        }
        return closest;
    }
    
}
