/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.naivemcts;

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
public class NaiveMCTS extends AI {
    public static final int DEBUG = 0;
    EvaluationFunction ef = null;
    
    Random r = new Random();
    AI randomAI = new RandomBiasedAI();
    int max_actions_so_far = 0;
    
    int NSIMULATIONS = 500;
    int MAXSIMULATIONTIME = 10000;
    
    float epsilon1 = 0.25f;

    public NaiveMCTS(int simulations, float e, int time, AI policy, EvaluationFunction a_ef) {
        super();
        NSIMULATIONS = simulations;
        MAXSIMULATIONTIME = time;     
        randomAI = policy;
        epsilon1 = e;
        ef = a_ef;
    }
    
    
    public void reset() {        
    }    

    
    public AI clone() {
        return new NaiveMCTS(NSIMULATIONS, epsilon1, MAXSIMULATIONTIME, randomAI, ef);
    }    
    
    
    public PlayerAction getAction(int player, GameState gs) throws Exception {

        if (gs.canExecuteAnyAction(player) && gs.winner()==-1) {
            PlayerAction pa = MCTS(player, 1-player, gs, NSIMULATIONS, gs.getTime()+MAXSIMULATIONTIME); 
            return pa;
        } else {
            return new PlayerAction();
        }        
    }
    
    
    public PlayerAction MCTS(int maxplayer, int minplayer, GameState gs, int T, int cutOffTime) throws Exception {
        NaiveMCTSNode tree = new NaiveMCTSNode(maxplayer, minplayer, gs, null);
        
        if (DEBUG>=1) System.out.println("RTNaiveUCT started...");

        for(int i = 0;i<T;i++) {
            NaiveMCTSNode leaf = tree.selectLeaf(maxplayer, minplayer, epsilon1, 5);
            
            if (leaf!=null) {
                GameState gs2 = leaf.gs.clone();
                simulate(gs2,cutOffTime);
                int time = gs2.getTime() - gs.getTime();
                // Discount factor:
                double evaluation = ef.evaluate(maxplayer, minplayer, gs2)*Math.pow(0.99,time/10.0);
    //            System.out.println("Evaluation: " + evaluation);

                leaf.propagateEvaluation((float)evaluation,null);            
            }
        }
        
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
        
        if (DEBUG>=1) System.out.println("RTNaiveUCT selected children " + tree.actions.get(bestIdx) + " explored " + best.visit_count + " Avg evaluation: " + (best.accum_evaluation/((double)best.visit_count)));
        
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
        return "NaiveMCTS(" + MAXSIMULATIONTIME + ")";
    }
    
}
