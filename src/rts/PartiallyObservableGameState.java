/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts;

import java.util.LinkedList;
import java.util.List;
import rts.units.Unit;

/**
 *
 * @author santi
 */
public class PartiallyObservableGameState extends GameState {
    protected int player;   // the observer player

    // creates a partially observable game state, from the point of view of 'player':
    public PartiallyObservableGameState(GameState gs, int a_player) {
        super(gs.getPhysicalGameState().cloneKeepingUnits(), gs.getUnitTypeTable());
        unitCancelationCounter = gs.unitCancelationCounter;
        time = gs.time;

        player = a_player;

        unitActions.putAll(gs.unitActions);

        List<Unit> toDelete = new LinkedList<Unit>();
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer() != player) {
                if (!observable(u.getX(),u.getY())) {
                    toDelete.add(u);
                }
            }
        }
        for(Unit u:toDelete) removeUnit(u);
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

    public PartiallyObservableGameState clone() {
        return new PartiallyObservableGameState(super.clone(), player);
    }
}
