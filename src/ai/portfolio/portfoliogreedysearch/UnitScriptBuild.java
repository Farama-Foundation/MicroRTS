/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.portfolio.portfoliogreedysearch;

import ai.abstraction.AbstractAction;
import ai.abstraction.Build;
import ai.abstraction.pathfinding.PathFinding;
import java.util.List;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class UnitScriptBuild extends UnitScript {
    
    AbstractAction action = null;
    PathFinding pf = null;
    UnitType ut = null;
    
    public UnitScriptBuild(PathFinding a_pf, UnitType a_ut) {
        pf = a_pf;
        ut = a_ut;
    }
    
    public UnitAction getAction(Unit u, GameState gs) {
        if (action.completed(gs)) {
            return null;
        } else {
            return action.execute(gs);
        }
    }
    
    public UnitScript instantiate(Unit u, GameState gs) {
        int pos = findBuildingPosition(u, gs.getPhysicalGameState());
        if (pos!=-1) {
            UnitScriptBuild script = new UnitScriptBuild(pf, ut);
            script.action = new Build(u, ut, pos%gs.getPhysicalGameState().getWidth(), 
                                             pos/gs.getPhysicalGameState().getWidth(), pf);
            return script;
        } else {
            return null;
        }
    }
    
    
    // Finds the nearest available location at which a building can be placed:
    public int findBuildingPosition(Unit u, PhysicalGameState pgs) {
        int bestPos = -1;
        int bestScore = 0;

        for (int x = 0; x < pgs.getWidth(); x++) {
            for (int y = 0; y < pgs.getHeight(); y++) {
                int pos = x + y * pgs.getWidth();
                if (pgs.getUnitAt(x, y) == null) {
                    int score = 0;

                    score = -(Math.abs(u.getX() - x) + Math.abs(u.getY() - y));

                    if (bestPos == -1 || score > bestScore) {
                        bestPos = pos;
                        bestScore = score;
                    }
                }
            }
        }

        return bestPos;
    }
    
}
