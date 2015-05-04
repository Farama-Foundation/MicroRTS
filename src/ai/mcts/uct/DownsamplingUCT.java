/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.mcts.uct;

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
public class DownsamplingUCT extends AI {
    public static final int DEBUG = 0;
    EvaluationFunction ef = null;

    Random r = new Random();
    AI randomAI = new RandomAI();
    int max_actions_so_far = 0;
    
    long MAXACTIONS = 100;
    int NSIMULATIONS = 1000;
    int MAXSIMULATIONTIME = 500;
    int MAX_TREE_DEPTH = 10;

    public DownsamplingUCT(int simulations, int time, long maxactions, int max_depth,AI policy, EvaluationFunction a_ef) {
        super();
        NSIMULATIONS = simulations;
        MAXSIMULATIONTIME = time;        
        MAXACTIONS = maxactions;
        MAX_TREE_DEPTH = max_depth;
        randomAI = policy;
        ef = a_ef;
    }
    
    
    public void reset() {
    }

    
    public AI clone() {
        return new DownsamplingUCT(NSIMULATIONS, MAXSIMULATIONTIME, MAXACTIONS, MAX_TREE_DEPTH, randomAI, ef);
    }  
    
    
    public PlayerAction getAction(int player, GameState gs) throws Exception {

        if (gs.canExecuteAnyAction(player) && gs.winner()==-1) {
            PlayerAction pa = UCT(player, 1-player, gs, NSIMULATIONS, gs.getTime()+MAXSIMULATIONTIME); 
            return pa;
        } else {
            return new PlayerAction();
        }        
    }
    
    
    public PlayerAction UCT(int maxplayer, int minplayer, GameState gs, int T, int cutOffTime) throws Exception {
        float evaluation_bound = ef.upperBound(gs);
        DownsamplingUCTNode tree = new DownsamplingUCTNode(maxplayer, minplayer, gs, null, MAXACTIONS, evaluation_bound);
        
        if (DEBUG>=1) System.out.println(this.getClass().getSimpleName() + " started...");

        for(int i = 0;i<T;i++) {
            DownsamplingUCTNode leaf = tree.UCTSelectLeaf(maxplayer, minplayer, MAXACTIONS, -1, MAX_TREE_DEPTH);
            
            if (leaf!=null) {
                GameState gs2 = leaf.gs.clone();
                simulate(gs2,cutOffTime);
                
                int time = gs2.getTime() - gs.getTime();
                // Discount factor:
                double evaluation = ef.evaluate(maxplayer, minplayer, gs2)*Math.pow(0.99,time/10.0);
/*
                int winner = gs2.winner();
                if (winner==-1) {
                    evaluation = SimpleEvaluationFunction.evaluate(maxplayer, minplayer, gs2);
                } else if (winner==maxplayer) {
                    evaluation = SimpleEvaluationFunction.VICTORY;
                } else {
                    evaluation = -SimpleEvaluationFunction.VICTORY;
                }
*/            
//            System.out.println("Evaluation: " + evaluation);
            
                while(leaf!=null) {
                    leaf.accum_evaluation += evaluation;
                    leaf.visit_count++;
                    leaf = leaf.parent;
                }
            }

        }
        
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
}
