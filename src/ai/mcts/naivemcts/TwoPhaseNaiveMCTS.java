/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.mcts.naivemcts;

import ai.*;
import ai.core.AI;
import ai.core.InterruptibleAIWithComputationBudget;
import ai.evaluation.EvaluationFunction;
import java.util.Random;
import rts.GameState;
import rts.PlayerAction;

/**
 *
 * @author santi
 */
public class TwoPhaseNaiveMCTS extends InterruptibleAIWithComputationBudget {
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
            
    public int MAXSIMULATIONTIME = 1024;
    public int MAX_TREE_DEPTH = 10;
    
    int playerForThisComputation;
    
    public float phase1_epsilon_l = 0.3f;
    public float phase1_epsilon_g = 0.0f;
    public float phase1_epsilon_0 = 1.0f;
    public float phase2_epsilon_l = 0.3f;
    public float phase2_epsilon_g = 0.0f;
    public float phase2_epsilon_0 = 0.0f;
    public float phase1_ratio = 0.5f;
    
    public int phase1_global_strategy = NaiveMCTSNode.E_GREEDY;
    public int phase2_global_strategy = NaiveMCTSNode.E_GREEDY;
    
    // statistics:
    public long total_runs = 0;
    public long total_cycles_executed = 0;
    public long total_actions_issued = 0;
    public long total_time = 0;
    
    
    public TwoPhaseNaiveMCTS(int available_time, int max_playouts, int lookahead, int max_depth, 
                               float el1, float eg1, float e01,
                               float el2, float eg2, float e02,
                               float p1_ratio,
                               AI policy, EvaluationFunction a_ef) {
        super(available_time, max_playouts);
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
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
    
    public TwoPhaseNaiveMCTS(int available_time, int max_playouts, int lookahead, int max_depth, 
                               float el1, float eg1, float e01, int a_gs1,
                               float el2, float eg2, float e02, int a_gs2,
                               float p1_ratio,
                               AI policy, EvaluationFunction a_ef) {
        super(available_time, max_playouts);
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        MAX_TREE_DEPTH = max_depth;
        phase1_epsilon_l = el1;
        phase1_epsilon_g = eg1;
        phase1_epsilon_0 = e01;
        phase1_global_strategy = a_gs1;
        
        phase2_epsilon_l = el2;
        phase2_epsilon_g = eg2;
        phase2_epsilon_0 = e02;
        phase2_global_strategy = a_gs2;
        
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
        return new TwoPhaseNaiveMCTS(MAX_TIME, MAX_ITERATIONS, MAXSIMULATIONTIME, MAX_TREE_DEPTH, 
                                             phase1_epsilon_l, phase1_epsilon_g, phase1_epsilon_0,
                                             phase2_epsilon_l, phase2_epsilon_g, phase2_epsilon_0,
                                             phase1_ratio, randomAI, ef);
    }    
    
    
    public void startNewComputation(int a_player, GameState gs) throws Exception {
    	playerForThisComputation = a_player;
        node_creation_ID = 0;
        tree = new NaiveMCTSNode(playerForThisComputation, 1-playerForThisComputation, gs, null, ef.upperBound(gs), node_creation_ID++);
        
        max_actions_so_far = Math.max(tree.moveGenerator.getSize(),max_actions_so_far);
        gs_to_start_from = gs;

        n_phase1_iterations_left = -1;
        n_phase1_milliseconds_left = -1;
        if (MAX_ITERATIONS>0) n_phase1_iterations_left = (int)(phase1_ratio * MAX_ITERATIONS);
        if (MAX_TIME>0) n_phase1_milliseconds_left = (int)(phase1_ratio * MAX_TIME);
    }    
    
    
    public void resetSearch() {
        if (DEBUG>=2) System.out.println("Resetting search...");
        tree = null;
        gs_to_start_from = null;
        n_phase1_iterations_left = -1;
        n_phase1_milliseconds_left = -1;
    }
    

    public void computeDuringOneGameFrame() throws Exception {        
        if (DEBUG>=2) System.out.println("Search...");
        long start = System.currentTimeMillis();
        long end = start;
        long count = 0;
        int n_phase1_milliseconds_left_initial = n_phase1_milliseconds_left;
        while(true) {
            if (n_phase1_milliseconds_left>0) n_phase1_milliseconds_left = n_phase1_milliseconds_left_initial - (int)(end - start);
            if (!iteration(playerForThisComputation)) break;
            count++;
            end = System.currentTimeMillis();
            if (MAX_TIME>=0 && (end - start)>=MAX_TIME) break; 
            if (MAX_ITERATIONS>=0 && count>=MAX_ITERATIONS) break;             
        }
            if (n_phase1_milliseconds_left>0) n_phase1_milliseconds_left = n_phase1_milliseconds_left_initial - (int)(end - start);
//        System.out.println("HL: " + count + " time: " + (System.currentTimeMillis() - start) + " (" + available_time + "," + max_playouts + ")");
        total_time += (end - start);
        total_cycles_executed++;
    }
    
    
    public boolean iteration(int player) throws Exception {
        NaiveMCTSNode leaf;
//        System.out.println("  " + n_phase1_iterations_left);
        if (n_phase1_iterations_left>0 || n_phase1_milliseconds_left>0) {
            leaf = tree.selectLeaf(player, 1-player, phase1_epsilon_l, phase1_epsilon_g, phase1_epsilon_0, phase1_global_strategy, MAX_TREE_DEPTH, node_creation_ID++);
            n_phase1_iterations_left--;
        } else {
            leaf = tree.selectLeaf(player, 1-player, phase2_epsilon_l, phase2_epsilon_g, phase2_epsilon_0, phase2_global_strategy, MAX_TREE_DEPTH, node_creation_ID++);
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
    
    public PlayerAction getBestActionSoFar() {
        int idx = getMostVisitedActionIdx();
        if (idx==-1) {
            if (DEBUG>=1) System.out.println("TwoPhaseNaiveMCTS no children selected. Returning an empty asction");
            return new PlayerAction();
        }
        if (DEBUG>=2) tree.showNode(0,1,ef);
        if (DEBUG>=1) {
            NaiveMCTSNode best = (NaiveMCTSNode) tree.children.get(idx);
            System.out.println("TwoPhaseNaiveMCTS selected children " + tree.actions.get(idx) + " explored " + best.visit_count + " Avg evaluation: " + (best.accum_evaluation/((double)best.visit_count)));
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
        return "TwoPhaseNaiveMCTS(" + MAXSIMULATIONTIME + "," + MAX_TIME + "," + MAX_ITERATIONS + "," + MAX_TREE_DEPTH + "," + phase1_epsilon_l +","+ phase1_epsilon_g +","+phase1_epsilon_0+","+phase2_epsilon_l+","+phase2_epsilon_g+","+phase2_epsilon_0+","+phase1_ratio + ")";
    }
    
    public String statisticsString() {
        return "Total runs: " + total_runs + 
               ", runs per action: " + (total_runs/(float)total_actions_issued) + 
               ", runs per cycle: " + (total_runs/(float)total_cycles_executed) + 
               ", averate time per cycle: " + (total_time/(float)total_cycles_executed) + 
               ", max branching factor: " + max_actions_so_far;
    }
    
}
