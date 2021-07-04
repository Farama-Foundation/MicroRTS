package rts;

import java.util.*;
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

	public int [][][] getMatrixObservation(int player){
        if (matrixObservation == null) {
            matrixObservation = new int[2][numFeatureMaps+1][pgs.height][pgs.width]; 
        }
        // hitpointsMatrix is matrixObservation[player][0]
        // resourcesMatrix is matrixObservation[player][1]
        // playersMatrix is matrixObservation[player][2]
        // unitTypesMatrix is matrixObservation[player][3]
        // unitActionMatrix is matrixObservation[player][4]


        for (int i=0; i<matrixObservation[player][0].length; i++) {
            Arrays.fill(matrixObservation[player][0][i], 0);
            Arrays.fill(matrixObservation[player][1][i], 0);
            Arrays.fill(matrixObservation[player][4][i], 0);
			Arrays.fill(matrixObservation[player][5][i], 0);
            // temp default value for empty spaces
            Arrays.fill(matrixObservation[player][2][i], -1);
            Arrays.fill(matrixObservation[player][3][i], -1);			
        }

        for (int i = 0; i < pgs.units.size(); i++) {
            Unit u = pgs.units.get(i);
            UnitActionAssignment uaa = unitActions.get(u);
            matrixObservation[player][0][u.getY()][u.getX()] = u.getHitPoints();
            matrixObservation[player][1][u.getY()][u.getX()] = u.getResources();
            matrixObservation[player][2][u.getY()][u.getX()] = (u.getPlayer() + player) % 2;
            matrixObservation[player][3][u.getY()][u.getX()] = u.getType().ID;
            if (uaa != null) {
                matrixObservation[player][4][u.getY()][u.getX()] = uaa.action.type;
            } else {
                matrixObservation[player][4][u.getY()][u.getX()] = UnitAction.TYPE_NONE;
            }
        }

        // normalize by getting rid of -1
        for(int i=0; i<matrixObservation[player][2].length; i++) {
            for(int j=0; j<matrixObservation[player][2][i].length; j++) {
                matrixObservation[player][3][i][j] += 1;
                matrixObservation[player][2][i][j] += 1;
            }
        }

		for (int y = 0; y<pgs.height; y++)
		{
			for (int x=0; x<pgs.width; x++)
			{
				if(observable(x,y))
					matrixObservation[player][5][y][x] = 1;
			}
		}

        return matrixObservation[player];
    }

    /* (non-Javadoc)
     * @see rts.GameState#clone()
     */
    public PartiallyObservableGameState clone() {
        return new PartiallyObservableGameState(super.clone(), player);
    }
}
