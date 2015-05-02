/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.mcts.uct;

import ai.montecarlo.*;
import ai.AI;
import ai.RandomBiasedAI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleEvaluationFunction;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;

/**
 *
 * @author santi
 */
public class ContinuingUCTFirstPlayUrgency extends AI {
    public static int DEBUG = 0;
    EvaluationFunction ef = null;
       
    Random r = new Random();
    AI randomAI = new RandomBiasedAI();
    long max_actions_so_far = 0;
    
    GameState gs_to_start_from = null;
    public UCTNodeFirstPlayUrgency tree = null;
    
    // statistics:
    public long total_runs = 0;
    public long total_cycles_executed = 0;
    public long total_actions_issued = 0;
    
    long total_runs_this_move = 0;
        
    int TIME_PER_CYCLE = 100;
    int MAX_PLAYOUTS = 200;
    int MAXSIMULATIONTIME = 1024;
    int MAX_TREE_DEPTH = 10;
    
    double FPUvalue = 0;
    
    
    public ContinuingUCTFirstPlayUrgency(int available_time, int max_playouts, int lookahead, int max_depth, AI policy, EvaluationFunction a_ef, double a_FPUvalue) {
        MAXSIMULATIONTIME = lookahead;
        MAX_PLAYOUTS = max_playouts;
        randomAI = policy;
        TIME_PER_CYCLE = available_time;
        MAX_TREE_DEPTH = max_depth;
        ef = a_ef;
        FPUvalue = a_FPUvalue;
    }
    
    
    public void printStats() {
        if (total_cycles_executed>0 && total_actions_issued>0) {
            System.out.println("Average runs per cycle: " + ((double)total_runs)/total_cycles_executed);
            System.out.println("Average runs per action: " + ((double)total_runs)/total_actions_issued);
        }
    }
    
    
    public void reset() {
        gs_to_start_from = null;
        tree = null;
        total_runs_this_move = 0;
    }
    
    
    public AI clone() {
        return new ContinuingUCTFirstPlayUrgency(TIME_PER_CYCLE, MAX_PLAYOUTS, MAXSIMULATIONTIME, MAX_TREE_DEPTH, randomAI, ef, FPUvalue);
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
            search(player, TIME_PER_CYCLE, MAX_PLAYOUTS);
            PlayerAction best = getBestAction();
            resetSearch();
            return best;
        } else {
            if (tree!=null) {
                // continue previous search:
                search(player, TIME_PER_CYCLE, MAX_PLAYOUTS);
            } else {
                // determine who will be the next player:
                GameState gs2 = gs.clone();
                while(gs2.winner()==-1 && 
                      !gs2.gameover() &&  
                    !gs2.canExecuteAnyAction(0) && 
                    !gs2.canExecuteAnyAction(1)) gs2.cycle();
                if (gs2.canExecuteAnyAction(player)) {
                    // start a new search:
                    startNewSearch(player,gs2);
                    search(player, TIME_PER_CYCLE, MAX_PLAYOUTS);
                    return new PlayerAction();
                } else {
                    return new PlayerAction();
                }
            }
        }
        
        return new PlayerAction();
    }    
    
    public void startNewSearch(int player, GameState gs) throws Exception {
        float evaluation_bound = SimpleEvaluationFunction.upperBound(gs);
        tree = new UCTNodeFirstPlayUrgency(player, 1-player, gs, null, evaluation_bound, FPUvalue);
        gs_to_start_from = gs;
        total_runs_this_move = 0;
//        System.out.println(evaluation_bound);
    }    
    
    
    public void resetSearch() {
        if (DEBUG>=2) System.out.println("Resetting search...");
        tree = null;
        gs_to_start_from = null;
        total_runs_this_move = 0;
    }
    

    public void search(int player, long available_time, int maxPlayouts) throws Exception {
        if (DEBUG>=2) System.out.println("Search...");
        long start = System.currentTimeMillis();
        int nPlayouts = 0;
        long cutOffTime = start + available_time;
        if (available_time<=0) cutOffTime = 0;

//        System.out.println(start + " + " + available_time + " = " + cutOffTime);

        while(true) {
            if (cutOffTime>0 && System.currentTimeMillis() < cutOffTime) break;
            if (maxPlayouts>0 && nPlayouts>maxPlayouts) break;
            monteCarloRun(player, cutOffTime);
            nPlayouts++;
        }
        
        total_cycles_executed++;
    }
    

    public double monteCarloRun(int player, long cutOffTime) throws Exception {
        UCTNodeFirstPlayUrgency leaf = tree.UCTSelectLeaf(player, 1-player, cutOffTime, MAX_TREE_DEPTH);

        if (leaf!=null) {
            GameState gs2 = leaf.gs.clone();
            simulate(gs2, gs2.getTime() + MAXSIMULATIONTIME);

            int time = gs2.getTime() - gs_to_start_from.getTime();
            double evaluation = ef.evaluate(player, 1-player, gs2)*Math.pow(0.99,time/10.0);

//                System.out.println(evaluation_bound + " -> " + evaluation + " -> " + (evaluation+evaluation_bound)/(evaluation_bound*2));

            while(leaf!=null) {
                leaf.accum_evaluation += evaluation;
                leaf.visit_count++;
                leaf = leaf.parent;
            }
            total_runs++;
            total_runs_this_move++;
            return evaluation;
        } else {
            // no actions to choose from :)
            System.err.println(this.getClass().getSimpleName() + ": claims there are no more leafs to explore...");
            return 0;
        }
    }
    
    
    public PlayerAction getBestAction() {
        total_actions_issued++;
                
        int mostVisitedIdx = -1;
        UCTNodeFirstPlayUrgency mostVisited = null;
        for(int i = 0;i<tree.children.size();i++) {
            UCTNodeFirstPlayUrgency child = tree.children.get(i);
            if (mostVisited == null || child.visit_count>mostVisited.visit_count) {
                mostVisited = child;
                mostVisitedIdx = i;
            }
        }
        
//        tree.showNode(0,0);
        if (DEBUG>=2) tree.showNode(0,1);
        if (DEBUG>=1) System.out.println(this.getClass().getSimpleName() + " performed " + total_runs_this_move + " playouts.");
        if (DEBUG>=1) System.out.println(this.getClass().getSimpleName() + " selected children " + tree.actions.get(mostVisitedIdx) + " explored " + mostVisited.visit_count + " Avg evaluation: " + (mostVisited.accum_evaluation/((double)mostVisited.visit_count)));
        
//        printStats();   
        
        if (mostVisitedIdx==-1) return new PlayerAction();
        
        return tree.actions.get(mostVisitedIdx);
    }
    
    
    // gets the best action, evaluates it for 'N' times using a simulation, and returns the average obtained value:
    public float getBestActionEvaluation(GameState gs, int player, int N) throws Exception {
        PlayerAction pa = getBestAction();
        
        if (pa==null) return 0;

        float accum = 0;
        for(int i = 0;i<N;i++) {
            GameState gs2 = gs.cloneIssue(pa);
            GameState gs3 = gs2.clone();
            simulate(gs3,gs3.getTime() + MAXSIMULATIONTIME);
            int time = gs3.getTime() - gs2.getTime();
            // Discount factor:
            accum += (float)(ef.evaluate(player, 1-player, gs3)*Math.pow(0.99,time/10.0));
        }
            
        return accum/N;
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
        return "ContinuingUCT(" + MAXSIMULATIONTIME + ")";
    }
    
}
