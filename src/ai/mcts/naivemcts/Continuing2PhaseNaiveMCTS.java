/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.mcts.naivemcts;

import ai.*;
import ai.core.AI;
import ai.evaluation.EvaluationFunction;
import java.util.Random;
import rts.GameState;
import rts.PlayerAction;

/**
 *
 * @author santi
 */
public class Continuing2PhaseNaiveMCTS extends AI {
    public static int DEBUG = 0;
    public EvaluationFunction ef = null;
       
    Random r = new Random();
    public AI randomAI = new RandomBiasedAI();
    long max_actions_so_far = 0;
    
    GameState gs_to_start_from = null;
    NaiveMCTSNode tree = null;
    int node_creation_ID = 0;
    int n_phase1_iterations_left = -1;      // this is set in the first cycle of execution for each action
    int n_phase1_milliseconds_left = -1;      // this is set in the first cycle of execution for each action
            
    public int AVAILABLE_TIME = 100;
    public long MAX_PLAYOUTS = 1000;
    public int MAXSIMULATIONTIME = 1024;
    public int MAX_TREE_DEPTH = 10;
    
    public float phase1_epsilon_l = 0.3f;
    public float phase1_epsilon_g = 0.0f;
    public float phase1_epsilon_0 = 1.0f;
    public float phase2_epsilon_l = 0.3f;
    public float phase2_epsilon_g = 0.0f;
    public float phase2_epsilon_0 = 0.0f;
    public float phase1_ratio = 0.5f;
    
    // statistics:
    public long total_runs = 0;
    public long total_cycles_executed = 0;
    public long total_actions_issued = 0;
    public long total_time = 0;
    
    
    public Continuing2PhaseNaiveMCTS(int available_time, long max_playouts, int lookahead, int max_depth, 
                               float el1, float eg1, float e01,
                               float el2, float eg2, float e02,
                               float p1_ratio,
                               AI policy, EvaluationFunction a_ef) {
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        AVAILABLE_TIME = available_time;
        MAX_PLAYOUTS = max_playouts;
        MAX_TREE_DEPTH = max_depth;
        phase1_epsilon_l = el1;
        phase1_epsilon_g = eg1;
        phase1_epsilon_0 = e01;
        phase2_epsilon_l = el2;
        phase2_epsilon_g = eg2;
        phase2_epsilon_0 = e02;
        phase1_ratio = p1_ratio;
        ef = a_ef;
    }    

    public void reset() {
        tree = null;
        gs_to_start_from = null;
        total_runs = 0;
        total_cycles_executed = 0;
        total_actions_issued = 0;
        total_time = 0;
        node_creation_ID = 0;
        n_phase1_iterations_left = -1;
        n_phase1_milliseconds_left = -1;
    }    
        
    
    public AI clone() {
        return new Continuing2PhaseNaiveMCTS(AVAILABLE_TIME, MAX_PLAYOUTS, MAXSIMULATIONTIME, MAX_TREE_DEPTH, 
                                             phase1_epsilon_l, phase1_epsilon_g, phase1_epsilon_0,
                                             phase2_epsilon_l, phase2_epsilon_g, phase2_epsilon_0,
                                             phase1_ratio, randomAI, ef);
    }    
    
    
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.winner()!=-1) return new PlayerAction();
        if (gs.canExecuteAnyAction(player)) {
            // continue or start a search:
            if (tree==null) {
                startNewSearch(player,gs);
                if (MAX_PLAYOUTS>0) n_phase1_iterations_left = (int)(MAX_PLAYOUTS*phase1_ratio);
                if (AVAILABLE_TIME>0) n_phase1_milliseconds_left = (int)(AVAILABLE_TIME*phase1_ratio);
            } else {
                if (!gs.getPhysicalGameState().equivalents(gs_to_start_from.getPhysicalGameState())) {
                    System.err.println("Game state used for search NOT equivalent to the actual one!!!");
                    System.err.println("gs:");
                    System.err.println(gs);
                    System.err.println("gs_to_start_from:");
                    System.err.println(gs_to_start_from);
                }
            }
            search(player, AVAILABLE_TIME, MAX_PLAYOUTS);
            PlayerAction best = getBestAction();
            resetSearch();
            return best;
        } else {
            if (tree!=null) {
                // continue previous search:
                search(player, AVAILABLE_TIME, MAX_PLAYOUTS);
            } else {
                // determine who will be the next player:
                GameState gs2 = gs.clone();
                int n = 1;
                while(gs2.winner()==-1 && 
                      !gs2.gameover() &&  
                    !gs2.canExecuteAnyAction(0) && 
                    !gs2.canExecuteAnyAction(1)) {
                    gs2.cycle();
                    n++;
                }
                if ((gs2.winner() == -1 && !gs2.gameover()) && 
                    gs2.canExecuteAnyAction(player)) {
                    // start a new search:
                    startNewSearch(player,gs2);
                    if (MAX_PLAYOUTS>0) n_phase1_iterations_left = (int)(MAX_PLAYOUTS*n*phase1_ratio);
                    if (AVAILABLE_TIME>0) n_phase1_milliseconds_left = (int)(AVAILABLE_TIME*n*phase1_ratio);                    
                    search(player, AVAILABLE_TIME, MAX_PLAYOUTS);
                    return new PlayerAction();
                } else {
                    return new PlayerAction();
                }
            }
        }
        
        return new PlayerAction();
    }    
    
    public void startNewSearch(int player, GameState gs) throws Exception {
        node_creation_ID = 0;
        tree = new NaiveMCTSNode(player, 1-player, gs, null, node_creation_ID++);
        
        max_actions_so_far = Math.max(tree.moveGenerator.getSize(),max_actions_so_far);
        gs_to_start_from = gs;

        n_phase1_iterations_left = -1;
        n_phase1_milliseconds_left = -1;
    }    
    
    
    public void resetSearch() {
        if (DEBUG>=2) System.out.println("Resetting search...");
        tree = null;
        gs_to_start_from = null;
        n_phase1_iterations_left = -1;
        n_phase1_milliseconds_left = -1;
    }
    

    public void search(int player, long available_time, long max_playouts) throws Exception {        
        if (DEBUG>=2) System.out.println("Search...");
        long start = System.currentTimeMillis();
        long end = start;
        long count = 0;
        int n_phase1_milliseconds_left_initial = n_phase1_milliseconds_left;
        while(true) {
            n_phase1_milliseconds_left = n_phase1_milliseconds_left_initial - (int)(end - start);
            if (!iteration(player)) break;
            count++;
            end = System.currentTimeMillis();
            if (available_time>=0 && (end - start)>=available_time) break; 
            if (max_playouts>=0 && count>=max_playouts) break;             
        }
        n_phase1_milliseconds_left = n_phase1_milliseconds_left_initial - (int)(end - start);
//        System.out.println("HL: " + count + " time: " + (System.currentTimeMillis() - start) + " (" + available_time + "," + max_playouts + ")");
        total_time += (end - start);
        total_cycles_executed++;
    }
    
    
    public boolean iteration(int player) throws Exception {
        NaiveMCTSNode leaf;
        if (n_phase1_iterations_left>0 || n_phase1_milliseconds_left>0) {
            leaf = tree.selectLeaf(player, 1-player, phase1_epsilon_l, phase1_epsilon_g, phase1_epsilon_0, MAX_TREE_DEPTH, node_creation_ID++);
            n_phase1_iterations_left--;
        } else {
            leaf = tree.selectLeaf(player, 1-player, phase2_epsilon_l, phase2_epsilon_g, phase2_epsilon_0, MAX_TREE_DEPTH, node_creation_ID++);
        }

        if (leaf!=null) {            
            GameState gs2 = leaf.gs.clone();
            simulate(gs2, gs2.getTime() + MAXSIMULATIONTIME);

            int time = gs2.getTime() - gs_to_start_from.getTime();
            double evaluation = ef.evaluate(player, 1-player, gs2)*Math.pow(0.99,time/10.0);

            leaf.propagateEvaluation((float)evaluation,null);            

            total_runs++;
            
//            System.out.println(total_runs + " - " + epsilon_0 + ", " + epsilon_l + ", " + epsilon_g);
            
        } else {
            // no actions to choose from :)
            System.err.println(this.getClass().getSimpleName() + ": claims there are no more leafs to explore...");
            return false;
        }
        return true;
    }
    
    public PlayerAction getBestAction() {
        int idx = getMostVisitedActionIdx();
        if (idx==-1) {
            if (DEBUG>=1) System.out.println("ContinuingNaiveMCTS no children selected. Returning an empty asction");
            return new PlayerAction();
        }
        if (DEBUG>=2) tree.showNode(0,1,ef);
        if (DEBUG>=1) {
            NaiveMCTSNode best = (NaiveMCTSNode) tree.children.get(idx);
            System.out.println("ContinuingNaiveMCTS selected children " + tree.actions.get(idx) + " explored " + best.visit_count + " Avg evaluation: " + (best.accum_evaluation/((double)best.visit_count)));
        }
        return tree.actions.get(idx);
    }
    
    
    public int getMostVisitedActionIdx() {
        total_actions_issued++;
            
        int bestIdx = -1;
        NaiveMCTSNode best = null;
        if (DEBUG>=2) {
//            for(Player p:gs_to_start_from.getPlayers()) {
//                System.out.println("Resources P" + p.getID() + ": " + p.getResources());
//            }
            System.out.println("Number of playouts: " + tree.visit_count);
            tree.printUnitActionTable();
        }
        for(int i = 0;i<tree.children.size();i++) {
            NaiveMCTSNode child = (NaiveMCTSNode)tree.children.get(i);
            if (DEBUG>=2) {
                System.out.println("child " + tree.actions.get(i) + " explored " + child.visit_count + " Avg evaluation: " + (child.accum_evaluation/((double)child.visit_count)));
            }
//            if (best == null || (child.accum_evaluation/child.visit_count)>(best.accum_evaluation/best.visit_count)) {
            if (best == null || child.visit_count>best.visit_count) {
                best = child;
                bestIdx = i;
            }
        }
        
        return bestIdx;
    }
    
    
    public int getHighestEvaluationActionIdx() {
        total_actions_issued++;
            
        int bestIdx = -1;
        NaiveMCTSNode best = null;
        if (DEBUG>=2) {
//            for(Player p:gs_to_start_from.getPlayers()) {
//                System.out.println("Resources P" + p.getID() + ": " + p.getResources());
//            }
            System.out.println("Number of playouts: " + tree.visit_count);
            tree.printUnitActionTable();
        }
        for(int i = 0;i<tree.children.size();i++) {
            NaiveMCTSNode child = (NaiveMCTSNode)tree.children.get(i);
            if (DEBUG>=2) {
                System.out.println("child " + tree.actions.get(i) + " explored " + child.visit_count + " Avg evaluation: " + (child.accum_evaluation/((double)child.visit_count)));
            }
//            if (best == null || (child.accum_evaluation/child.visit_count)>(best.accum_evaluation/best.visit_count)) {
            if (best == null || (child.accum_evaluation/((double)child.visit_count))>(best.accum_evaluation/((double)best.visit_count))) {
                best = child;
                bestIdx = i;
            }
        }
        
        return bestIdx;
    }
    
        
    public void simulate(GameState gs, int time) throws Exception {
        boolean gameover = false;

        do{
            if (gs.isComplete()) {
                gameover = gs.cycle();
            } else {
                gs.issue(randomAI.getAction(0, gs));
                gs.issue(randomAI.getAction(1, gs));
            }
        }while(!gameover && gs.getTime()<time);   
    }
    
    public NaiveMCTSNode getTree() {
        return tree;
    }
    
    public GameState getGameStateToStartFrom() {
        return gs_to_start_from;
    }
    
    
    public String toString() {
        return "Continuing2PhaseNaiveMCTS(" + MAXSIMULATIONTIME + "," + MAX_PLAYOUTS + "," + MAX_TREE_DEPTH + "," + phase1_epsilon_l +","+ phase1_epsilon_g +","+phase1_epsilon_0+","+phase2_epsilon_l+","+phase2_epsilon_g+","+phase2_epsilon_0+","+phase1_ratio + ")";
    }
    
    public String statisticsString() {
        return "Total runs: " + total_runs + 
               ", runs per action: " + (total_runs/(float)total_actions_issued) + 
               ", runs per cycle: " + (total_runs/(float)total_cycles_executed) + 
               ", averate time per cycle: " + (total_time/(float)total_cycles_executed) + 
               ", max branching factor: " + max_actions_so_far;
    }
    
}
