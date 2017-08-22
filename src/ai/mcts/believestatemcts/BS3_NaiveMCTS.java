package ai.mcts.believestatemcts;

import ai.core.AI;
import ai.evaluation.EvaluationFunction;
import static ai.mcts.MCTSNode.r;
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
 * If the locations is visible again we search for the nearest not visible location.
 * We do inference on:
 * - possible buildings not seen before (i.e. if we see a military unit, it should be a barracks)
 * - when a new visible unit, remove closest last seen position
 *
 * @author albertouri
 */
public class BS3_NaiveMCTS extends NaiveMCTS implements AIWithBelieveState {

    GameState initialGameState = null;
    List<Unit> lastKnownPosition = new LinkedList<Unit>();
    List<Unit> inferedUnits = new LinkedList<Unit>();
    PartiallyObservableGameState lastObservedGame = null;
    boolean[] typeSeen;

    public BS3_NaiveMCTS(UnitTypeTable utt) {
        super(utt);
    }

    public BS3_NaiveMCTS(int available_time, int max_playouts, int lookahead, int max_depth,
            float e_l, float discout_l, float e_g, float discout_g, float e_0, float discout_0, AI policy, EvaluationFunction a_ef, boolean fensa) {
        super(available_time, max_playouts, lookahead, max_depth, e_l, discout_l, e_g, discout_g, e_0, discout_0, policy, a_ef, fensa);
    }

    public BS3_NaiveMCTS(int available_time, int max_playouts, int lookahead, int max_depth,
            float e_l, float e_g, float e_0, AI policy, EvaluationFunction a_ef, boolean fensa) {
        super(available_time, max_playouts, lookahead, max_depth, e_l, e_g, e_0, policy, a_ef, fensa);
    }

    public BS3_NaiveMCTS(int available_time, int max_playouts, int lookahead, int max_depth,
            float e_l, float e_g, float e_0, int a_global_strategy, AI policy, EvaluationFunction a_ef, boolean fensa) {
        super(available_time, max_playouts, lookahead, max_depth, e_l, e_g, e_0, a_global_strategy, policy, a_ef, fensa);
    }

    @Override
    public AI clone() {
        return new BS3_NaiveMCTS(TIME_BUDGET, ITERATIONS_BUDGET, MAXSIMULATIONTIME, MAX_TREE_DEPTH, epsilon_l, discount_l, epsilon_g, discount_g, epsilon_0, discount_0, playoutPolicy, ef, forceExplorationOfNonSampledActions);
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
    
    
    @Override
    public int getMostVisitedActionIdx() {
        total_actions_issued++;
        if (getTree().children == null) return -1;

        List<Integer> bestIdxs = new ArrayList<>();
        int bestScore = -1;

        for (int i = 0; i < getTree().children.size(); i++) {
            NaiveMCTSNode child = (NaiveMCTSNode) getTree().children.get(i);

            if (child.visit_count > bestScore) {
                bestIdxs.clear();
                bestIdxs.add(i);
                bestScore = child.visit_count;
            } else if (child.visit_count > bestScore) {
                bestIdxs.add(i);
            }
        }

        if (bestIdxs.isEmpty()) return -1;
        if (bestIdxs.size() == 1) return bestIdxs.get(0);

        // otherwise we have multiple best actions, choose one randomly
        System.out.println("Random action from " + bestIdxs.size());
        return r.nextInt(bestIdxs.size());
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
        // set initial typeSeen
        typeSeen = new boolean[gs.getUnitTypeTable().getUnitTypes().size()]; // default value of false

        // save all enemy's units that are not visible
        for (Unit u : gs.getUnits()) {
            if (u.getPlayer() == opponent && !pogs.observable(u.getX(), u.getY())) {
//                System.out.println("First time seen " + u.getType().name);
                lastKnownPosition.add(u.clone());
                typeSeen[u.getType().ID] = true;
            }
        }
        // save initila observed game
        lastObservedGame = pogs.clone();
    }

    @Override
    public List<Unit> getBelieveUnits() {
        List<Unit> l = new LinkedList<Unit>();
        l.addAll(lastKnownPosition);
        l.addAll(inferedUnits);
        return l;
    }

    public GameState sampleWorld(int player, PartiallyObservableGameState gs) {
        GameState newWorld = gs.clone();

        List<Unit> toDelete = new ArrayList<>();
        // add last known units in our world sampler
        for (Unit u : lastKnownPosition) {
            boolean validPosition = true;
            if (gs.observable(u.getX(), u.getY())) {
                // infered position was wrong, update it
                validPosition = getClosestNotObservableLocationNear(u.getX(), u.getY(), gs, u);
            }
            if (validPosition) {
                try {
                    newWorld.getPhysicalGameState().addUnit(u);
                } catch (IllegalArgumentException e) {
                    System.err.println("IllegalArgumentException: " + e.getMessage());
                    System.err.println(newWorld.getPhysicalGameState());
                    System.err.println("adding unit: " + u);
                    System.err.println("Last known unit:");
                    System.err.println(lastKnownPosition);
                }
            } else {
                toDelete.add(u);
            }
        }
        lastKnownPosition.removeAll(toDelete);

        toDelete.clear();
        // add inferend units in our world sampler
        for (Unit u : inferedUnits) {
            boolean validPosition = true;
            if (gs.observable(u.getX(), u.getY())) {
                // infered position was wrong, update it
                getClosestNotObservableLocationNear(u.getX(), u.getY(), gs, u);
            }
            if (validPosition) {
                try {
                    newWorld.getPhysicalGameState().addUnit(u);
//                    System.out.println("Infered unit added: " + u.toString());
                } catch (IllegalArgumentException e) {
                    System.err.println("IllegalArgumentException: " + e.getMessage());
                    System.err.println(newWorld.getPhysicalGameState());
                    System.err.println("adding unit: " + u);
                    System.err.println("Infered units:");
                    System.err.println(inferedUnits);
                }
            } else {
                toDelete.add(u);
            }
        }
        inferedUnits.removeAll(toDelete);

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
//                        System.out.println("Enemy moved to fog of war!" + u.toString());
                        lastKnownPosition.add(u.clone());
                    }
                } else { // unit was static
                    if (!gs.observable(u.getX(), u.getY())) { // is location still observable?
                        if (!wasUnderAttack(u)) { // wasn't under attack (sometimes units kill each other simultaneously)
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

                // check if we have seen any new type of unit
                if (!typeSeen[u.getType().ID]) {
//                    System.out.println("First time seen " + u.getType().name);
                    typeSeen[u.getType().ID] = true;
                    // we assume only one unit type can produce this unit
                    UnitType ut = u.getType().producedBy.get(0);
                    // if producer not seen, add to inferedUnits
                    if (!typeSeen[ut.ID]) {
                        Unit newUnit = new Unit(opponent, ut, 0, 0, 0);
                        // Search possible location for the new unit
                        boolean validPosition = getClosestNotObservableLocationNear(u.getX(), u.getY(), gs, newUnit);
                        if (validPosition) {
//                            System.out.println("New Infered unit: " + newUnit.toString());
                            // if possible location found, add it
                            inferedUnits.add(newUnit);
                        }
                    }
                }

                // check if an infered unit now is visible
                List<Unit> toDelete = new ArrayList<>();
                for (Unit iu : inferedUnits) {
                    if (u.getType() == iu.getType()) {
//                        System.out.println("Infered unit found: " + u.toString());
                        toDelete.add(iu);
                    }
                }
                inferedUnits.removeAll(toDelete);

                // check if a new visible unit has a last known position
                // new visible opponent if: previous locations wasn't observable or no visible opponent was moving there
                if (!lastObservedGame.observable(u.getX(), u.getY()) || !wasVisibleOpponentMovingTo(opponent, u.getX(), u.getY())) {
//                    System.out.println("Opponent visible: " + u.toString());
                    Unit unitToRemove = null;
                    // look if unit was added to the lastKnownPosition
                    for (Unit observedUnit : lastKnownPosition) {
                        if (observedUnit.getID() == u.getID()) {
                            unitToRemove = observedUnit;
                            break;
                        }
                    }

                    if (unitToRemove != null) {
//                        System.out.println("Last known position removed: " + unitToRemove.toString());
                        lastKnownPosition.remove(unitToRemove);
                    }
                }

                // sometimes a visible unit start to move or produce a unit on top of a believe unit
                // for those cases we need to relocate the believe unit
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
//                            System.out.println("Updating conflict with move/produce");
                            boolean validPosition = getClosestNotObservableLocationNear(bu.getX(), bu.getY(), gs, bu);
                            if (!validPosition) lastKnownPosition.remove(bu);
                            break;
                        }
                    }
                    for (Unit bu : inferedUnits) {
                        if (bu.getX() == offsx && bu.getY() == offsy) {
//                            System.out.println("Updating conflict with move/produce");
                            boolean validPosition = getClosestNotObservableLocationNear(bu.getX(), bu.getY(), gs, bu);
                            if (!validPosition) inferedUnits.remove(bu);
                            break;
                        }
                    }
                }
            }
        }

        // at the end, update the last observed game state
        lastObservedGame = gs.clone();
    }

    public boolean wasVisibleOpponentMovingTo(int opponent, int x, int y) {
        if (!lastObservedGame.free(x, y)) return true; // unit was already there
        for (Unit u : lastObservedGame.getUnits()) {
            if (u.getPlayer() == opponent) {
                UnitActionAssignment uaa = lastObservedGame.getActionAssignment(u);
                if (uaa != null && uaa.action.getType() == UnitAction.TYPE_MOVE) {
                    int offsx = 0;
                    int offsy = 0;
                    if (uaa.action.getDirection() == UnitAction.DIRECTION_UP) offsy = -1;
                    if (uaa.action.getDirection() == UnitAction.DIRECTION_RIGHT) offsx = 1;
                    if (uaa.action.getDirection() == UnitAction.DIRECTION_DOWN) offsy = 1;
                    if (uaa.action.getDirection() == UnitAction.DIRECTION_LEFT) offsx = -1;
                    if ((u.getX() + offsx) == x && (u.getY() + offsy) == y) return true;
                }
            }
        }
        return false;
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

    // returns false if couldn't find a valid location
    public boolean getClosestNotObservableLocationNear(int startX, int startY, PartiallyObservableGameState gs, Unit u) {
        //searches outward in a spiral.
        int x = startX;
        int y = startY;
        int length = 1;
        int j = 0;
        boolean first = true;
        int dx = 0;
        int dy = 1;
        int maxLenght = Math.max(gs.getPhysicalGameState().getWidth(), gs.getPhysicalGameState().getHeight());
        while (length < maxLenght) {
            // look for a location that is not visible and free
            if (x >= 0 && x < gs.getPhysicalGameState().getWidth()
                    && y >= 0 && y < gs.getPhysicalGameState().getHeight()
                    && !gs.observable(x, y) && gs.free(x, y) && believeFree(x, y)) {
                u.setX(x);
                u.setY(y);
                return true;
            }

            //otherwise, move to another position
            x = x + dx;
            y = y + dy;
            j++; //count how many steps we take in this direction
            if (j == length) { //if we've reached the end, its time to turn
                j = 0;	//reset step counter
                if (!first) length++; //increment step counter if needed
                first = !first; //first=true for every other turn so we spiral out at the right rate

                //turn counter clockwise 90 degrees:
                if (dx == 0) {
                    dx = dy;
                    dy = 0;
                } else {
                    dy = -dx;
                    dx = 0;
                }
            }
        }
        return false;
    }

    public boolean believeFree(int x, int y) {
        for (Unit u : lastKnownPosition) {
            if (u.getX() == x && u.getY() == y) return false;
        }
        for (Unit u : inferedUnits) {
            if (u.getX() == x && u.getY() == y) return false;
        }
        return true;
    }

}
