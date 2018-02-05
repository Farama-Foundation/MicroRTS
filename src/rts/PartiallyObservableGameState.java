package rts;

import java.util.LinkedList;
import java.util.List;
import rts.units.Unit;

/**
 * A partially observable game state. It is associated with a player to check whether
 * it is able to observe the map portion
 * @author santi
 */
public class PartiallyObservableGameState extends GameState {
    /**
	 * 
	 */
	protected int player;   // the observer player

    /** 
     * Creates a partially observable game state, from the point of view of 'player':
     * @param gs a fully-observable game state
     * @param a_player
     */
    public PartiallyObservableGameState(GameState gs, int a_player) {
		super(gs.getPhysicalGameState().cloneKeepingUnits(), gs.getUnitTypeTable());
		unitCancelationCounter = gs.unitCancelationCounter;
		time = gs.time;

		player = a_player;

		unitActions.putAll(gs.unitActions);

		List<Unit> toDelete = new LinkedList<Unit>();
		for (Unit u : pgs.getUnits()) {
			if (u.getPlayer() != player) {
				if (!observable(u.getX(), u.getY())) {
					toDelete.add(u);
				}
			}
		}
		for (Unit u : toDelete)
			removeUnit(u);
    }

    /**
     * Returns whether the position is within view of the player
     * @see rts.GameState#observable(int, int)
     */
    public boolean observable(int x, int y) {
		for (Unit u : pgs.getUnits()) {
			if (u.getPlayer() == player) {
				double d = Math.sqrt((u.getX() - x) * (u.getX() - x) + (u.getY() - y) * (u.getY() - y));
				if (d <= u.getType().sightRadius)
					return true;
			}
		}

        return false;
    }

    /* (non-Javadoc)
     * @see rts.GameState#clone()
     */
    public PartiallyObservableGameState clone() {
        return new PartiallyObservableGameState(super.clone(), player);
    }
}
