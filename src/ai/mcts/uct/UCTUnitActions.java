/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.mcts.uct;

import ai.core.AI;
import ai.RandomBiasedAI;
import ai.core.InterruptibleAIWithComputationBudget;
import ai.evaluation.EvaluationFunction;
import java.util.Random;
import rts.GameState;
import rts.PlayerAction;

/**
 *
 * @author santi
 */
public class UCTUnitActions extends InterruptibleAIWithComputationBudget {
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
        
    int MAXSIMULATIONTIME = 1024;
    
    int playerForThisComputation;
    
    
    public UCTUnitActions(int available_time, int lookahead, int max_depth, AI policy, EvaluationFunction a_ef) {
        super(available_time, -1);
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
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
        return new UCTUnitActions(MAX_TIME, MAXSIMULATIONTIME, MAX_TREE_DEPTH, randomAI, ef);
    }  
    
    
    public void startNewComputation(int a_player, GameState gs) {
    	playerForThisComputation = a_player;
        float evaluation_bound = ef.upperBound(gs);
        tree = new UCTUnitActionsNode(playerForThisComputation, 1-playerForThisComputation, gs, null, evaluation_bound);
        gs_to_start_from = gs;
//        System.out.println(evaluation_bound);
    }    
    
    
    public void resetSearch() {
        if (DEBUG>=2) System.out.println("Resetting search...");
        tree = null;
        gs_to_start_from = null;
    }
    

    public void computeDuringOneGameFrame() throws Exception {
        if (DEBUG>=2) System.out.println("Search...");
        long start = System.currentTimeMillis();
        
        while((System.currentTimeMillis() - start)<MAX_TIME) {
            UCTUnitActionsNode leaf = tree.UCTSelectLeaf(playerForThisComputation, 1-playerForThisComputation, MAX_TREE_DEPTH);
            
            if (leaf!=null) {
                GameState gs2 = leaf.gs.clone();
                simulate(gs2, gs2.getTime() + MAXSIMULATIONTIME);
                
                int time = gs2.getTime() - gs_to_start_from.getTime();
                double evaluation = ef.evaluate(playerForThisComputation, 1-playerForThisComputation, gs2)*Math.pow(0.99,time/10.0);
            
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
    
    
    public PlayerAction getBestActionSoFar() {
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
        return "UCTUnitActions(" + MAXSIMULATIONTIME + ")";
    }
    
}
