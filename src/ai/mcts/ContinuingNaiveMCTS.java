/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.mcts;

import ai.*;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleEvaluationFunction;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PlayerAction;

/**
 *
 * @author santi
 */
public class ContinuingNaiveMCTS extends AI {
    public static int DEBUG = 0;
    EvaluationFunction ef = null;
       
    Random r = new Random();
    AI randomAI = new RandomBiasedAI();
    long max_actions_so_far = 0;
    
    GameState gs_to_start_from = null;
    NaiveMCTSNode tree = null;
    
    // statistics:
    public long total_runs = 0;
    public long total_cycles_executed = 0;
    public long total_actions_issued = 0;
        
    int TIME_PER_CYCLE = 100;
    int MAXSIMULATIONTIME = 1024;
    int MAX_TREE_DEPTH = 10;
    
    float epsilon1 = 0.25f;
    float epsilon2 = 0.2f;
    
    
    public ContinuingNaiveMCTS(int available_time, int lookahead, int max_depth, float e1, float e2, AI policy, EvaluationFunction a_ef) {
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        TIME_PER_CYCLE = available_time;
        MAX_TREE_DEPTH = max_depth;
        epsilon1 = e1;
        epsilon2 = e2;
        ef = a_ef;
    }    
    
    public void reset() {
        tree = null;
        gs_to_start_from = null;
    }    
    
    
    public AI clone() {
        return new ContinuingNaiveMCTS(TIME_PER_CYCLE, MAXSIMULATIONTIME, MAX_TREE_DEPTH, epsilon1, epsilon2, randomAI, ef);
    }    
    
    
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.winner()!=-1) return new PlayerAction();
        if (gs.canExecuteAnyAction(player)) {
            // continue or start a search:
            if (tree==null) {
                startNewSearch(player,gs);
            } else {
                if (!gs.getPhysicalGameState().equivalents(gs_to_start_from.getPhysicalGameState())) {
                    System.err.println("Game state used for search NOT equivalent to the actual one!!!");
                    System.err.println("gs:");
                    System.err.println(gs);
                    System.err.println("gs_to_start_from:");
                    System.err.println(gs_to_start_from);
                }
            }
            search(player, TIME_PER_CYCLE);
            PlayerAction best = getBestAction();
            resetSearch();
            return best;
        } else {
            if (tree!=null) {
                // continue previous search:
                search(player, TIME_PER_CYCLE);
            } else {
                // determine who will be the next player:
                GameState gs2 = gs.clone();
                while(gs2.winner()==-1 && 
                      !gs2.gameover() &&  
                    !gs2.canExecuteAnyAction(0) && 
                    !gs2.canExecuteAnyAction(1)) gs2.cycle();
                if ((gs2.winner() == -1 && !gs2.gameover()) && 
                    gs2.canExecuteAnyAction(player)) {
                    // start a new search:
                    startNewSearch(player,gs2);
                    search(player, TIME_PER_CYCLE);
                    return new PlayerAction();
                } else {
                    return new PlayerAction();
                }
            }
        }
        
        return new PlayerAction();
    }    
    
    public void startNewSearch(int player, GameState gs) throws Exception {
        tree = new NaiveMCTSNode(player, 1-player, gs, null);
        
        max_actions_so_far = Math.max(tree.moveGenerator.getSize(),max_actions_so_far);
        gs_to_start_from = gs;
    }    
    
    
    public void resetSearch() {
        if (DEBUG>=2) System.out.println("Resetting search...");
        tree = null;
        gs_to_start_from = null;
    }
    

    public void search(int player, long available_time) throws Exception {
        if (DEBUG>=2) System.out.println("Search...");
        long start = System.currentTimeMillis();
        
        while((System.currentTimeMillis() - start)<available_time) {
            NaiveMCTSNode leaf = tree.selectLeaf(player, 1-player, epsilon1, epsilon2, MAX_TREE_DEPTH);
            
            if (leaf!=null) {
                GameState gs2 = leaf.gs.clone();
                simulate(gs2, gs2.getTime() + MAXSIMULATIONTIME);
                
                int time = gs2.getTime() - gs_to_start_from.getTime();
                double evaluation = ef.evaluate(player, 1-player, gs2)*Math.pow(0.99,time/10.0);
            
                leaf.propagateEvaluation((float)evaluation,null);            

                total_runs++;
            } else {
                // no actions to choose from :)
                System.err.println(this.getClass().getSimpleName() + ": claims there are no more leafs to explore...");
                break;
            }
        }
        
        total_cycles_executed++;
    }
    
    
    public PlayerAction getBestAction() {
        total_actions_issued++;
            
        int bestIdx = -1;
        NaiveMCTSNode best = null;
        if (DEBUG>=2) tree.printUnitActionTable();
        for(int i = 0;i<tree.children.size();i++) {
            NaiveMCTSNode child = tree.children.get(i);
            if (DEBUG>=2) System.out.println("child " + tree.actions.get(i) + " explored " + child.visit_count + " Avg evaluation: " + (child.accum_evaluation/((double)child.visit_count)));
//            if (best == null || (child.accum_evaluation/child.visit_count)>(best.accum_evaluation/best.visit_count)) {
            if (best == null || child.visit_count>best.visit_count) {
                best = child;
                bestIdx = i;
            }
        }
        
        if (bestIdx==-1) {
            if (DEBUG>=1) System.out.println("ContinuingNaiveMCTS no children selected. Weturning an empty asction");
            return new PlayerAction();
        }
        
        if (DEBUG>=2) tree.showNode(0,1);
        if (DEBUG>=1) System.out.println("ContinuingNaiveMCTS selected children " + tree.actions.get(bestIdx) + " explored " + best.visit_count + " Avg evaluation: " + (best.accum_evaluation/((double)best.visit_count)));
                
        return tree.actions.get(bestIdx);
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
    
    public String toString() {
        return "ContinuingNaiveMCTS(" + MAXSIMULATIONTIME + "," + epsilon1 + "," + epsilon2 + ")";
    }
    
    public String statisticsString() {
        return "Total runs: " + total_runs + 
               " , runs per action: " + (total_runs/(float)total_actions_issued) + 
               " , runs per cycle: " + (total_runs/(float)total_cycles_executed) + 
               " , max branching factor: " + max_actions_so_far;
    }
    
}
