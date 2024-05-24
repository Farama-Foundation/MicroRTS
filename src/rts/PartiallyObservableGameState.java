package rts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import rts.units.Unit;

/**
 * A partially observable game state. It is associated with a player to check whether
 * it is able to observe the map portion
 * @author santi
 */
public class PartiallyObservableGameState extends GameState {

	protected int observer;   // the observer player

	// Feature maps:
    // 1: hit points
    // 2: resources
    // 3: player
    // 4: unit type
    // 5: current unit action
    // 6: walls
	// 7: which cells can I see?
	// 8: for which cells do I know that my opponent can see them?
	public static final int NUM_VECTOR_OBSERVATION_FEATURE_MAPS_PARTIAL_OBS = 8;

    /** 
     * Creates a partially observable game state, from the point of view of 'player':
     * @param gs a fully-observable game state
     * @param a_player
     */
    public PartiallyObservableGameState(GameState gs, int a_player) {
		super(gs.getPhysicalGameState().cloneKeepingUnits(), gs.getUnitTypeTable());
		unitCancelationCounter = gs.unitCancelationCounter;
		time = gs.time;

		observer = a_player;

		unitActions.putAll(gs.unitActions);

		final List<Unit> toDelete = new LinkedList<>();
		for (final Unit u : pgs.getUnits()) {
			if (u.getPlayer() != observer) {
				if (!observable(u.getX(), u.getY())) {
					toDelete.add(u);
				}
			}
		}
		for (final Unit u : toDelete)
			removeUnit(u);
    }

    /**
     * Returns whether the position is within view of the player
     * @see rts.GameState#observable(int, int)
     */
    @Override
	public boolean observable(final int x, final int y) {
		for (final Unit u : pgs.getUnits()) {
			if (u.getPlayer() == observer) {
				final int dSquared = (u.getX() - x) * (u.getX() - x) + (u.getY() - y) * (u.getY() - y);
				if (dSquared <= u.getType().sightRadius * u.getType().sightRadius)
					return true;
			}
		}

        return false;
    }

    /* (non-Javadoc)
     * @see rts.GameState#clone()
     */
    @Override
	public PartiallyObservableGameState clone() {
        return new PartiallyObservableGameState(super.clone(), observer);
    }

	@Override
	public int [][][] getVectorObservation(final int player){
        if (vectorObservation == null) {
            vectorObservation = new int[2][NUM_VECTOR_OBSERVATION_FEATURE_MAPS_PARTIAL_OBS][pgs.height][pgs.width]; 
        }
        
        List<int[]> friendlyUnits = new ArrayList<>();
        List<int[]> enemyUnits = new ArrayList<>();
        
        // hitpointsMatrix is vectorObservation[player][0]
        // resourcesMatrix is vectorObservation[player][1]
        // playersMatrix is vectorObservation[player][2]
        // unitTypesMatrix is vectorObservation[player][3]
        // unitActionMatrix is vectorObservation[player][4]
        // wallMatrix is vectorObservation[player][5]
		// myVisibilityMatrix is vectorObservation[player][6]
        // opponentVisibilityMatrix is vectorObservation[player][7]

        for (int i=0; i<vectorObservation[player][0].length; i++) {
            Arrays.fill(vectorObservation[player][0][i], 0);
            Arrays.fill(vectorObservation[player][1][i], 0);
            Arrays.fill(vectorObservation[player][2][i], 0);
            Arrays.fill(vectorObservation[player][3][i], 0);
            Arrays.fill(vectorObservation[player][4][i], 0);
            Arrays.fill(vectorObservation[player][5][i], 0);
			Arrays.fill(vectorObservation[player][6][i], 0);
            Arrays.fill(vectorObservation[player][7][i], 0);
        }

        for (int i = 0; i < pgs.units.size(); i++) {
            Unit u = pgs.units.get(i);
            UnitActionAssignment uaa = unitActions.get(u);
            
            vectorObservation[player][0][u.getY()][u.getX()] = u.getHitPoints();
            vectorObservation[player][1][u.getY()][u.getX()] = u.getResources();
            
            final int owner = u.getPlayer();
            if (owner >= 0) {		// Owned by a player, not neutral
            	vectorObservation[player][2][u.getY()][u.getX()] = ((u.getPlayer() + player) % 2) + 1;
            
	            // Split units based on owner (used for last two layers of the observation)
	            if (owner == player)
	                friendlyUnits.add(new int[]{u.getX(), u.getY(), u.getType().sightRadius});
	            else
	                enemyUnits.add(new int[]{u.getX(), u.getY(), u.getType().sightRadius});
            }
            
            vectorObservation[player][3][u.getY()][u.getX()] = u.getType().ID + 1;
            
            if (uaa != null) {
                vectorObservation[player][4][u.getY()][u.getX()] = uaa.action.type;
            } else {
                // Commented line of code is unnecessary: already initialised to 0
            	//vectorObservation[player][4][u.getY()][u.getX()] = UnitAction.TYPE_NONE;
            } 
        }
        
        // Encode the presence of walls
        final int[] terrain = pgs.terrain;
        for (int y = 0; y < pgs.height; ++y) {
        	System.arraycopy(terrain, y * pgs.width, vectorObservation[player][5][y], 0, pgs.width);
        }
        
        // Encode visibility
        final int[][] playerVisibility = calculateVisibility(friendlyUnits, pgs.width, pgs.height);
        final int[][] opponentVisibility = calculateVisibility(enemyUnits, pgs.width, pgs.height);

        for (int y = 0; y < pgs.height; y++) {
            System.arraycopy(playerVisibility[y], 0, vectorObservation[player][6][y], 0, pgs.width);
            System.arraycopy(opponentVisibility[y], 0, vectorObservation[player][7][y], 0, pgs.width);
        }

        return vectorObservation[player];
    }
    
    private static int[][] calculateVisibility(final List<int[]> units, final int width, final int height) {
        final int[][] visibility = new int[height][width];
        for (final int[] unit : units) {
            final int ux = unit[0];
            final int uy = unit[1];
            final int sightRadius = unit[2];
            final int sightRadiusSquared = sightRadius * sightRadius;
    
            for (int dy = -sightRadius; dy <= sightRadius; dy++) {
                for (int dx = -sightRadius; dx <= sightRadius; dx++) {
                    final int x = ux + dx;
                    final int y = uy + dy;
                    
                    if (x >= 0 && x < width && y >= 0 && y < height) {
                        final int distanceSquared = dx * dx + dy * dy;
                        if (distanceSquared <= sightRadiusSquared) {
                            visibility[y][x] = 1;
                        }
                    }
                }
            }
        }
        return visibility;
    }
}
