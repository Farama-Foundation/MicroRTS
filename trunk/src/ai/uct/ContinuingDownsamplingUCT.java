/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.uct;

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
public class ContinuingDownsamplingUCT extends AI {
    public static final int DEBUG = 0;
    EvaluationFunction ef = null;
       
    Random r = new Random();
    AI randomAI = new RandomBiasedAI();
    long max_actions_so_far = 0;
    
    GameState gs_to_start_from = null;
    DownsamplingUCTNode tree = null;
    
    // statistics:
    public long total_runs = 0;
    public long total_cycles_executed = 0;
    public long total_actions_issued = 0;
        
    long MAXACTIONS = 100;
    int TIME_PER_CYCLE = 100;
    int MAXSIMULATIONTIME = 1024;
    
    
    public ContinuingDownsamplingUCT(int available_time, int lookahead, long maxactions, AI policy, EvaluationFunction a_ef) {
        MAXACTIONS = maxactions;
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        TIME_PER_CYCLE = available_time;
        ef = a_ef;
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
    }
        
    
    public AI clone() {
        return new ContinuingDownsamplingUCT(TIME_PER_CYCLE, MAXSIMULATIONTIME, MAXACTIONS, randomAI, ef);
    }  
    
    
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (DEBUG>=1) {
            System.out.println("ContinuingDownsamplingUCT: getAction started...");
            System.out.flush();
        }
        if (gs.winner()!=-1) {
            if (DEBUG>=1) {
                System.out.println("ContinuingDownsamplingUCT: getAction finished");
                System.out.flush();
            }
            return new PlayerAction();
        }
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
            if (DEBUG>=1) {
                System.out.println("ContinuingDownsamplingUCT: getAction finished");
                System.out.flush();
            }
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
                if (gs2.canExecuteAnyAction(player)) {
                    // start a new search:
                    startNewSearch(player,gs2);
                    search(player, TIME_PER_CYCLE);
                    if (DEBUG>=1) {
                        System.out.println("ContinuingDownsamplingUCT: getAction finished");
                        System.out.flush();
                    }
                    return new PlayerAction();
                } else {
                    if (DEBUG>=1) {
                        System.out.println("ContinuingDownsamplingUCT: getAction finished");
                        System.out.flush();
                    }
                    return new PlayerAction();
                }
            }
        }
        
        if (DEBUG>=1) {
            System.out.println("ContinuingDownsamplingUCT: getAction finished");
            System.out.flush();
        }
        return new PlayerAction();
    }    
    
    public void startNewSearch(int player, GameState gs) throws Exception {
        float evaluation_bound = SimpleEvaluationFunction.upperBound(gs);
        tree = new DownsamplingUCTNode(player, 1-player, gs, null, MAXACTIONS, evaluation_bound);
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
        long cutOffTime = start + available_time;
        
        while(System.currentTimeMillis() < cutOffTime) {
            DownsamplingUCTNode leaf = tree.UCTSelectLeaf(player, 1-player, MAXACTIONS, cutOffTime);
            
            if (leaf!=null) {
                GameState gs2 = leaf.gs.clone();
                simulate(gs2, gs2.getTime() + MAXSIMULATIONTIME);
                
                int time = gs2.getTime() - gs_to_start_from.getTime();
                double evaluation = ef.evaluate(player, 1-player, gs2)*Math.pow(0.99,time/10.0);
            
                while(leaf!=null) {
                    leaf.accum_evaluation += evaluation;
                    leaf.visit_count++;
                    leaf = leaf.parent;
                }
                total_runs++;
            } else {
                // no actions to choose from :)
                break;
            }
        }
        
        total_cycles_executed++;
    }
    
    
    public PlayerAction getBestAction() {
        total_actions_issued++;
                
        int mostVisitedIdx = -1;
        DownsamplingUCTNode mostVisited = null;
        for(int i = 0;i<tree.children.size();i++) {
            DownsamplingUCTNode child = tree.children.get(i);
            if (mostVisited == null || child.visit_count>mostVisited.visit_count) {
                mostVisited = child;
                mostVisitedIdx = i;
            }
        }
        
        if (DEBUG>=2) tree.showNode(0,1);        
        if (DEBUG>=1) System.out.println(this.getClass().getSimpleName() + " selected children " + tree.actions.get(mostVisitedIdx) + " explored " + mostVisited.visit_count + " Avg evaluation: " + (mostVisited.accum_evaluation/((double)mostVisited.visit_count)));
        
//        printStats();        
        
        return tree.actions.get(mostVisitedIdx);
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
        return "ContinuingDownsamplingUCT(" + MAXSIMULATIONTIME + ")";
    }
    
}
