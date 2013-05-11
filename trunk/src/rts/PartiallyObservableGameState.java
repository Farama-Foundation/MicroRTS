/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class PartiallyObservableGameState extends GameState {
    int player;   // the observer player
    
    // creates a partially observable game state, from the point of view of 'player':
    public PartiallyObservableGameState(PhysicalGameState a_pgs, UnitTypeTable a_utt, int a_player) {
        super(a_pgs, a_utt);
        player = a_player;
    }
    
    public PartiallyObservableGameState(GameState gs, int a_player) {
        super(gs.getPhysicalGameState().cloneIncludingTerrain(), gs.getUnitTypeTable());
        time = gs.time;
        player = a_player;
        for(UnitActionAssignment uaa:gs.unitActions.values()) {
            Unit u = uaa.unit;
            int idx = gs.pgs.getUnits().indexOf(u);
            if (idx==-1) {
                System.out.println("Problematic game state:");
                System.out.println(this);
                System.out.println("Problematic action:");
                System.out.println(uaa);
                throw new Error("Inconsistent game state in the constructor of PartiallyObservableGameState...");
            } else {
                Unit u2 = pgs.getUnits().get(idx);
                unitActions.put(u2,new UnitActionAssignment(u2, uaa.action, uaa.time));
            }                
        }    
        
        List<Unit> toDelete = new LinkedList<Unit>();
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer() != player) {
                if (!observable(u.getX(),u.getY())) {
                    toDelete.add(u);
                }
            }
        }
        for(Unit u:toDelete) removeUnit(u);
        for(int y = 0;y<pgs.getHeight();y++) {
            for(int x = 0;x<pgs.getWidth();x++) {
                if (!observable(x, y)) pgs.setTerrain(x, y, PhysicalGameState.TERRAIN_NONE);
            }
        }
    }

    public boolean observable(int x, int y) {
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer() == player) {
                double d = Math.sqrt((u.getX()-x)*(u.getX()-x) + (u.getY()-y)*(u.getY()-y));
                if (d<=u.getType().sightRadius) return true;
            }
        }
        
        return false;
    }
}
