package ai.mcts.believestatemcts;

import ai.core.AI;
import ai.evaluation.EvaluationFunction;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.mcts.naivemcts.NaiveMCTSNode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PlayerAction;
import rts.UnitAction;
import rts.UnitActionAssignment;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/**
 * The believe state stores the last observed location of an opponent's unit.
 * If the locations is visible again we remove the unit from the believe state.
 *
 * @author albertouri
 */
public class BS2_NaiveMCTS extends NaiveMCTS implements AIWithBelieveState {

    GameState initialGameState = null;
    
    // list of units we "believe" exist (for now it's just "last seen" position)
    List<Unit> lastKnownPosition = new LinkedList<Unit>();
    PartiallyObservableGameState lastObservedGame = null;

    public BS2_NaiveMCTS(UnitTypeTable utt) {
        super(utt);
    }

    public BS2_NaiveMCTS(int available_time, int max_playouts, int lookahead, int max_depth,
            float e_l, float discout_l, float e_g, float discout_g, float e_0, float discout_0, AI policy, EvaluationFunction a_ef, boolean fensa) {
        super(available_time, max_playouts, lookahead, max_depth, e_l, discout_l, e_g, discout_g, e_0, discout_0, policy, a_ef, fensa);
    }

    public BS2_NaiveMCTS(int available_time, int max_playouts, int lookahead, int max_depth,
            float e_l, float e_g, float e_0, AI policy, EvaluationFunction a_ef, boolean fensa) {
        super(available_time, max_playouts, lookahead, max_depth, e_l, e_g, e_0, policy, a_ef, fensa);
    }

    public BS2_NaiveMCTS(int available_time, int max_playouts, int lookahead, int max_depth,
            float e_l, float e_g, float e_0, int a_global_strategy, AI policy, EvaluationFunction a_ef, boolean fensa) {
        super(available_time, max_playouts, lookahead, max_depth, e_l, e_g, e_0, a_global_strategy, policy, a_ef, fensa);
    }

    @Override
    public AI clone() {
        return new BS2_NaiveMCTS(TIME_BUDGET, ITERATIONS_BUDGET, MAXSIMULATIONTIME, MAX_TREE_DEPTH, epsilon_l, discount_l, epsilon_g, discount_g, epsilon_0, discount_0, playoutPolicy, ef, forceExplorationOfNonSampledActions);
    }

   @Override
    public final PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.canExecuteAnyAction(player)) {
            startNewComputation(player, gs);
            computeDuringOneGameFrame();
            return getBestActionSoFar();
        } else {
            return new PlayerAction();
        }
    }
    
    
    @Override
    public void startNewComputation(int a_player, GameState gs) throws Exception {
        if (initialGameState!=null && gs.getTime()==0) {
            setInitialBelieveState(a_player, initialGameState.clone(), new PartiallyObservableGameState(initialGameState, a_player));
        }
        
        if (gs instanceof PartiallyObservableGameState) {
            // create a sampling world from our believe-states
            updateBelieveState(player, (PartiallyObservableGameState) gs);
            gs = sampleWorld(player, (PartiallyObservableGameState) gs);
        }        
        
        player = a_player;
        current_iteration = 0;
        tree = new NaiveMCTSNode(player, 1-player, gs, null, ef.upperBound(gs), current_iteration++, forceExplorationOfNonSampledActions);
        
        if (tree.moveGenerator==null) {
            max_actions_so_far = 0;
        } else {
            max_actions_so_far = Math.max(tree.moveGenerator.getSize(),max_actions_so_far);        
        }
        gs_to_start_from = gs;
        
        epsilon_l = initial_epsilon_l;
        epsilon_g = initial_epsilon_g;
        epsilon_0 = initial_epsilon_0;        
    }    

    
    public void reset()
    {
        initialGameState = null;
    }   
    

    public void preGameAnalysis(GameState gs, long milliseconds) throws Exception
    {
        initialGameState = gs.clone();
    }    
    

    @Override
    public void setInitialBelieveState(int player, GameState gs, PartiallyObservableGameState pogs) {
        int opponent = 1 - player;
        // save all enemy's units that are not visible
        for (Unit u : gs.getUnits()) {
            if (u.getPlayer() == opponent && !pogs.observable(u.getX(), u.getY())) {
                lastKnownPosition.add(u);
            }
        }
        // save initila observed game
        lastObservedGame = pogs;
    }

    @Override
    public List<Unit> getBelieveUnits() {
        List<Unit> l = new LinkedList<Unit>();
        l.addAll(lastKnownPosition);
        return l;
    }

    public GameState sampleWorld(int player, PartiallyObservableGameState gs) {
        GameState newWorld = gs.clone();

        List<Unit> toDelete = new ArrayList<>();
        for (Unit u : lastKnownPosition) {
            // remove unit if location is visible or not valid
            if (gs.observable(u.getX(), u.getY())) {
                toDelete.add(u);
            } else {
                newWorld.getPhysicalGameState().addUnit(u);
            }
        }
        lastKnownPosition.removeAll(toDelete);

        return newWorld;
    }

    public void updateBelieveState(int player, PartiallyObservableGameState gs) {
        int opponent = 1 - player;

        // Handling missing units form previous game state
        for (Unit u : lastObservedGame.getUnits()) {
            if (u.getPlayer() == opponent && gs.free(u.getX(), u.getY())) {
                // check for enemy units that moved into the fog-of-war
                UnitActionAssignment uaa = lastObservedGame.getActionAssignment(u);
                if (uaa != null && uaa.action.getType() == UnitAction.TYPE_MOVE) {
                    int offsx = 0;
                    int offsy = 0;
                    if (uaa.action.getDirection() == UnitAction.DIRECTION_UP) offsy = -1;
                    if (uaa.action.getDirection() == UnitAction.DIRECTION_RIGHT) offsx = 1;
                    if (uaa.action.getDirection() == UnitAction.DIRECTION_DOWN) offsy = 1;
                    if (uaa.action.getDirection() == UnitAction.DIRECTION_LEFT) offsx = -1;
                    if (!gs.observable(u.getX() + offsx, u.getY() + offsy)) {
//                        System.out.println("Enemy moved to fog of war!");
                        lastKnownPosition.add(u.clone());
                    }
                } else { // unit was static
                    if (!gs.observable(u.getX(), u.getY())) { // is location still observable?
                        if (!wasUnderAttack(u)) { // wasn't under attack (sometimes units kill each other)
//                            System.out.println("Enemy now is out of sight! " + u.toString());
                            lastKnownPosition.add(u.clone());
                        } else {
//                            System.out.println("Enemy unit killed out of sight! (probably they kill each other)");
                        }
                    }
                }
            }
        }

        // Handling new units frome previous game state
        for (Unit u : gs.getUnits()) {
            if (u.getPlayer() == opponent) {

                // check if a new visible unit has a last known position
                for (Unit bu : lastKnownPosition) {
                    if (bu.getID() == u.getID()) {
                        lastKnownPosition.remove(bu);
                        break;
                    }
                }

                // sometimes a visible unit start to move or produce a unit on top of a believe unit
                UnitActionAssignment uaa = gs.getActionAssignment(u);
                if (uaa != null && (uaa.action.getType() == UnitAction.TYPE_MOVE || uaa.action.getType() == UnitAction.TYPE_PRODUCE)) {
                    int offsx = u.getX();
                    int offsy = u.getY();
                    if (uaa.action.getDirection() == UnitAction.DIRECTION_UP) offsy -= 1;
                    if (uaa.action.getDirection() == UnitAction.DIRECTION_RIGHT) offsx += 1;
                    if (uaa.action.getDirection() == UnitAction.DIRECTION_DOWN) offsy += 1;
                    if (uaa.action.getDirection() == UnitAction.DIRECTION_LEFT) offsx -= 1;
                    for (Unit bu : lastKnownPosition) {
                        if (bu.getX() == offsx && bu.getY() == offsy) {
                            lastKnownPosition.remove(bu);
                            break;
                        }
                    }
                }
            }
        }

        // at the end, update the last observed game state
        lastObservedGame = gs.clone();
    }

    public boolean wasUnderAttack(Unit u) {

        for (UnitActionAssignment ua : lastObservedGame.getUnitActions().values()) {
            if (ua.action.getType() == UnitAction.TYPE_ATTACK_LOCATION
                    && ua.action.getLocationX() == u.getX() && ua.action.getLocationY() == u.getY()) {
                return true;
            }
        }

        return false;
    }

}
