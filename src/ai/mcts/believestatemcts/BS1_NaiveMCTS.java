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
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 * The believe state only stores the opponent building from the game state at frame 0.
 * There isn't new observations, and the buildings in the believe state are only removed
 * when the building is destroyed.
 *
 * @author albertouri
 */
public class BS1_NaiveMCTS extends NaiveMCTS implements AIWithBelieveState {

    GameState initialGameState = null;
    
    // list of units we "believe" exist (for now it's just "last seen" position)
    List<Unit> lastKnownPosition = new LinkedList<Unit>();

    public BS1_NaiveMCTS(UnitTypeTable utt) {
        super(utt);
    }

    public BS1_NaiveMCTS(int available_time, int max_playouts, int lookahead, int max_depth,
            float e_l, float discout_l, float e_g, float discout_g, float e_0, float discout_0, AI policy, EvaluationFunction a_ef, boolean fensa) {
        super(available_time, max_playouts, lookahead, max_depth, e_l, discout_l, e_g, discout_g, e_0, discout_0, policy, a_ef, fensa);
    }

    public BS1_NaiveMCTS(int available_time, int max_playouts, int lookahead, int max_depth,
            float e_l, float e_g, float e_0, AI policy, EvaluationFunction a_ef, boolean fensa) {
        super(available_time, max_playouts, lookahead, max_depth, e_l, e_g, e_0, policy, a_ef, fensa);
    }

    public BS1_NaiveMCTS(int available_time, int max_playouts, int lookahead, int max_depth,
            float e_l, float e_g, float e_0, int a_global_strategy, AI policy, EvaluationFunction a_ef, boolean fensa) {
        super(available_time, max_playouts, lookahead, max_depth, e_l, e_g, e_0, a_global_strategy, policy, a_ef, fensa);
    }

    @Override
    public AI clone() {
        return new BS1_NaiveMCTS(TIME_BUDGET, ITERATIONS_BUDGET, MAXSIMULATIONTIME, MAX_TREE_DEPTH, epsilon_l, discount_l, epsilon_g, discount_g, epsilon_0, discount_0, playoutPolicy, ef, forceExplorationOfNonSampledActions);
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
        // save list of enemy units that cannot move (buildings)
        for (Unit u : gs.getUnits()) {
            if (u.getPlayer() == opponent && !u.getType().canMove
                    && !pogs.observable(u.getX(), u.getY())) {
                lastKnownPosition.add(u);
            }
        }
    }

    @Override
    public List<Unit> getBelieveUnits() {
        List<Unit> l = new LinkedList<Unit>();
        l.addAll(lastKnownPosition);
        return l;
    }

    public GameState sampleWorld(int player, PartiallyObservableGameState gs) {
        GameState newWorld = gs.clone();
        // for each enemy building in our believe-state
        List<Unit> toDelete = new ArrayList<>();
        for (Unit u : lastKnownPosition) {
            // if location not visible add it
            if (!gs.observable(u.getX(), u.getY())) {
//                System.out.println("Unit added to world: " + u.toString());
                newWorld.getPhysicalGameState().addUnit(u);
            } else { // if visible and not present, remove from believe-state (i.e. it was killed)
                Unit observedUnit = newWorld.getPhysicalGameState().getUnitAt(u.getX(), u.getY());
                if (observedUnit == null || u.getType() != observedUnit.getType()) {
//                    System.out.println("Removing from believe-state: " + u.toString());
                    toDelete.add(u);
                }
            }
        }
        lastKnownPosition.removeAll(toDelete);

        return newWorld;
    }

}
