/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction.partialobservability;

import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class POWorkerRush extends WorkerRush {

    public POWorkerRush(UnitTypeTable a_utt) {
        this(a_utt, new AStarPathFinding());
    }
    
    
    public POWorkerRush(UnitTypeTable a_utt, PathFinding a_pf) {
        super(a_utt, a_pf);
    }

    public void reset() {
    	super.reset();
    }

    public AI clone() {
        return new POWorkerRush(utt, pf);
    }

    public void meleeUnitBehavior(Unit u, Player p, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit closestEnemy = null;
        int closestDistance = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() != p.getID()) {
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestEnemy == null || d < closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;
                }
            }
        }
        if (closestEnemy != null) {
            attack(u, closestEnemy);
        } else if (gs instanceof PartiallyObservableGameState) {
            PartiallyObservableGameState pogs = (PartiallyObservableGameState)gs;
            // there are no enemies, so we need to explore (find the nearest non-observable place):
            int closest_x = 0;
            int closest_y = 0;
            closestDistance = -1;
            for(int i = 0;i<pgs.getHeight();i++) {
                for(int j = 0;j<pgs.getWidth();j++) {
                    if (!pogs.observable(j, i)) {
                        int d = (u.getX() - j)*(u.getX() - j) + (u.getY() - i)*(u.getY() - i);
                        if (closestDistance == -1 || d<closestDistance) {
                            closest_x = j;
                            closest_y = i;
                            closestDistance = d;
                        }
                    }
                }
            }
            if (closestDistance!=-1) {
                move(u, closest_x, closest_y);
            }
        }
    }

}
