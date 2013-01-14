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
public class ContinuingUCTUnitActions extends AI {
    public static final int DEBUG = 0;
    EvaluationFunction ef = null;
       
    Random r = new Random();
    AI randomAI = new RandomBiasedAI();
    long max_actions_so_far = 0;
    
    GameState gs_to_start_from = null;
    UCTUnitActionsNode tree = null;
    int MAX_TREE_DEPTH = 10;
    
    // statistics:
    public long total_runs = 0;
    public long total_cycles_executed = 0;
    public long total_actions_issued = 0;
        
    int TIME_PER_CYCLE = 100;
    int MAXSIMULATIONTIME = 1024;
    
    
    public ContinuingUCTUnitActions(int available_time, int lookahead, int max_depth, AI policy, EvaluationFunction a_ef) {
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        TIME_PER_CYCLE = available_time;
        MAX_TREE_DEPTH = max_depth;
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
        return new ContinuingUCTUnitActions(TIME_PER_CYCLE, MAXSIMULATIONTIME, MAX_TREE_DEPTH, randomAI, ef);
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
                if (gs2.canExecuteAnyAction(player)) {
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
    
    public void startNewSearch(int player, GameState gs) {
        float evaluation_bound = SimpleEvaluationFunction.upperBound(gs);
        tree = new UCTUnitActionsNode(player, 1-player, gs, null, evaluation_bound);
        gs_to_start_from = gs;
//        System.out.println(evaluation_bound);
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
            UCTUnitActionsNode leaf = tree.UCTSelectLeaf(player, 1-player, MAX_TREE_DEPTH);
            
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
            } else {
                // no actions to choose from :)
                System.err.println(this.getClass().getSimpleName() + ": claims there are no more leafs to explore...");
                break;
            }
        }
        
        total_cycles_executed++;
    }
    
    
    public PlayerAction getBestAction() {
        return getMostVisited(tree, gs_to_start_from.getTime());
    }
    
    
    public PlayerAction getMostVisited(UCTUnitActionsNode current, int time) {
        if (current.type!=0 || current.gs.getTime()!=time) return null;
        
        int mostVisitedIdx = -1;
        
        UCTUnitActionsNode mostVisited = null;
        for(int i = 0;i<current.children.size();i++) {
            UCTUnitActionsNode child = current.children.get(i);
            if (mostVisited == null || child.visit_count>mostVisited.visit_count) {
                mostVisited = child;
                mostVisitedIdx = i;
            }
//            System.out.println(child.visit_count);
        }
        
        if (mostVisitedIdx==-1) return null;
        
        PlayerAction mostVisitedAction = current.actions.get(mostVisitedIdx);
        PlayerAction restOfAction = getMostVisited(mostVisited, time);
        
        if (restOfAction!=null) mostVisitedAction = mostVisitedAction.merge(restOfAction);
                
        return mostVisitedAction;
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
        return "ContinuingUCTUnitActions(" + MAXSIMULATIONTIME + ")";
    }
    
}
