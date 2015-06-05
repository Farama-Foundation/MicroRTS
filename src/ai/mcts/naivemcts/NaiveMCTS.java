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
public class NaiveMCTS extends InterruptibleAIWithComputationBudget {
    public static int DEBUG = 0;
    public EvaluationFunction ef = null;
       
    Random r = new Random();
    public AI randomAI = new RandomBiasedAI();
    long max_actions_so_far = 0;
    
    GameState gs_to_start_from = null;
    NaiveMCTSNode tree = null;
    int current_iteration = 0;
            
    public int MAXSIMULATIONTIME = 1024;
    public int MAX_TREE_DEPTH = 10;
    
    int player;
    
    public float epsilon_0 = 0.2f;
    public float epsilon_l = 0.25f;
    public float epsilon_g = 0.0f;

    // these variables are for using a discount factor on the epsilon values above. My experiments indicate that things work better without discount
    // So, they are just maintained here for completeness:
    public float initial_epsilon_0 = 0.2f;
    public float initial_epsilon_l = 0.25f;
    public float initial_epsilon_g = 0.0f;
    public float discount_0 = 0.999f;
    public float discount_l = 0.999f;
    public float discount_g = 0.999f;
    
    public int global_strategy = NaiveMCTSNode.E_GREEDY;
    
    // statistics:
    public long total_runs = 0;
    public long total_cycles_executed = 0;
    public long total_actions_issued = 0;
    public long total_time = 0;
    
    
    public NaiveMCTS(int available_time, int max_playouts, int lookahead, int max_depth, 
                               float e1, float discout1,
                               float e2, float discout2, 
                               float e3, float discout3, 
                               AI policy, EvaluationFunction a_ef) {
        super(available_time, max_playouts);
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        MAX_TREE_DEPTH = max_depth;
        initial_epsilon_l = epsilon_l = e1;
        initial_epsilon_g = epsilon_g = e2;
        initial_epsilon_0 = epsilon_0 = e3;
        discount_l = discout1;
        discount_g = discout2;
        discount_0 = discout3;
        ef = a_ef;
    }    

    public NaiveMCTS(int available_time, int max_playouts, int lookahead, int max_depth, float e1, float e2, float e3, AI policy, EvaluationFunction a_ef) {
        super(available_time, max_playouts);
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        MAX_TREE_DEPTH = max_depth;
        initial_epsilon_l = epsilon_l = e1;
        initial_epsilon_g = epsilon_g = e2;
        initial_epsilon_0 = epsilon_0 = e3;
        discount_l = 1.0f;
        discount_g = 1.0f;
        discount_0 = 1.0f;
        ef = a_ef;
    }    
    
    public NaiveMCTS(int available_time, int max_playouts, int lookahead, int max_depth, float e1, float e2, float e3, int a_global_strategy, AI policy, EvaluationFunction a_ef) {
        super(available_time, max_playouts);
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        MAX_TREE_DEPTH = max_depth;
        initial_epsilon_l = epsilon_l = e1;
        initial_epsilon_g = epsilon_g = e2;
        initial_epsilon_0 = epsilon_0 = e3;
        discount_l = 1.0f;
        discount_g = 1.0f;
        discount_0 = 1.0f;
        global_strategy = a_global_strategy;
        ef = a_ef;
    }        
    
    public void reset() {
        tree = null;
        gs_to_start_from = null;
        total_runs = 0;
        total_cycles_executed = 0;
        total_actions_issued = 0;
        total_time = 0;
        current_iteration = 0;
    }    
        
    
    public AI clone() {
        return new NaiveMCTS(MAX_TIME, MAX_ITERATIONS, MAXSIMULATIONTIME, MAX_TREE_DEPTH, epsilon_l, discount_l, epsilon_g, discount_g, epsilon_0, discount_0, randomAI, ef);
    }    
    
    
    public void startNewComputation(int a_player, GameState gs) throws Exception {
        player = a_player;
        current_iteration = 0;
        tree = new NaiveMCTSNode(player, 1-player, gs, null, ef.upperBound(gs), current_iteration++);
        
        max_actions_so_far = Math.max(tree.moveGenerator.getSize(),max_actions_so_far);
        gs_to_start_from = gs;
        
        epsilon_l = initial_epsilon_l;
        epsilon_g = initial_epsilon_g;
        epsilon_0 = initial_epsilon_0;        
    }    
    
    
    public void resetSearch() {
        if (DEBUG>=2) System.out.println("Resetting search...");
        tree = null;
        gs_to_start_from = null;
    }
    

    public void computeDuringOneGameFrame() throws Exception {        
        if (DEBUG>=2) System.out.println("Search...");
        long start = System.currentTimeMillis();
        long end = start;
        long count = 0;
        while(true) {
            if (!iteration(player)) break;
            count++;
            end = System.currentTimeMillis();
            if (MAX_TIME>=0 && (end - start)>=MAX_TIME) break; 
            if (MAX_ITERATIONS>=0 && count>=MAX_ITERATIONS) break;             
        }
//        System.out.println("HL: " + count + " time: " + (System.currentTimeMillis() - start) + " (" + available_time + "," + max_playouts + ")");
        total_time += (end - start);
        total_cycles_executed++;
    }
    
    
    public boolean iteration(int player) throws Exception {
        
        NaiveMCTSNode leaf = tree.selectLeaf(player, 1-player, epsilon_l, epsilon_g, epsilon_0, global_strategy, MAX_TREE_DEPTH, current_iteration++);

        if (leaf!=null) {            
            GameState gs2 = leaf.gs.clone();
            simulate(gs2, gs2.getTime() + MAXSIMULATIONTIME);

            int time = gs2.getTime() - gs_to_start_from.getTime();
            double evaluation = ef.evaluate(player, 1-player, gs2)*Math.pow(0.99,time/10.0);

            leaf.propagateEvaluation(evaluation,null);            

            // update the epsilon values:
            epsilon_0*=discount_0;
            epsilon_l*=discount_l;
            epsilon_g*=discount_g;
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
            if (DEBUG>=1) System.out.println("NaiveMCTS no children selected. Returning an empty asction");
            return new PlayerAction();
        }
        if (DEBUG>=2) tree.showNode(0,1,ef);
        if (DEBUG>=1) {
            NaiveMCTSNode best = (NaiveMCTSNode) tree.children.get(idx);
            System.out.println("NaiveMCTS selected children " + tree.actions.get(idx) + " explored " + best.visit_count + " Avg evaluation: " + (best.accum_evaluation/((double)best.visit_count)));
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
        return "NaiveMCTS(" + MAXSIMULATIONTIME + "," + MAX_TIME + "," + MAX_ITERATIONS + "," + MAX_TREE_DEPTH + "," + epsilon_l + "," + epsilon_g + "," + epsilon_0 + ")";
    }
    
    public String statisticsString() {
        return "Total runs: " + total_runs + 
               ", runs per action: " + (total_runs/(float)total_actions_issued) + 
               ", runs per cycle: " + (total_runs/(float)total_cycles_executed) + 
               ", averate time per cycle: " + (total_time/(float)total_cycles_executed) + 
               ", max branching factor: " + max_actions_so_far;
    }
    
}
