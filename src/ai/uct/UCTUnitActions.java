/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.uct;

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
public class UCTUnitActions extends AI {
    public static int DEBUG = 0;
    EvaluationFunction ef = null;

    Random r = new Random();
    AI randomAI = new RandomAI();
    int max_actions_so_far = 0;
    
    int NSIMULATIONS = 1000;
    int MAXSIMULATIONTIME = 500;

    public UCTUnitActions(int simulations, int time, AI policy, EvaluationFunction a_ef) {
        super();
        NSIMULATIONS = simulations;
        MAXSIMULATIONTIME = time;        
        randomAI = policy;
        ef = a_ef;
    }
    
    
    public void reset() {
    }

    
    public AI clone() {
        return new UCTUnitActions(NSIMULATIONS, MAXSIMULATIONTIME, randomAI, ef);
    }  
        
    
    public PlayerAction getAction(int player, GameState gs) throws Exception {

        if (gs.canExecuteAnyAction(player) && gs.winner()==-1) {
            PlayerAction pa = UCT(player, 1-player, gs.clone(), NSIMULATIONS, gs.getTime()+MAXSIMULATIONTIME); 
            return pa;
        } else {
            return new PlayerAction();
        }        
    }
    
    
    public PlayerAction UCT(int maxplayer, int minplayer, GameState gs, int T, int cutOffTime) throws Exception {
        float evaluation_bound = SimpleEvaluationFunction.upperBound(gs);
        UCTUnitActionsNode tree = new UCTUnitActionsNode(maxplayer, minplayer, gs, null, evaluation_bound);
        
        if (DEBUG>=1) System.out.println(this.getClass().getSimpleName() + " started...");

        for(int i = 0;i<T;i++) {
            UCTUnitActionsNode leaf = tree.UCTSelectLeaf(maxplayer, minplayer);
            
            if (leaf!=null) {
                GameState gs2 = leaf.gs.clone();
                simulate(gs2,cutOffTime);
                
                int time = gs2.getTime() - gs.getTime();
                double evaluation = ef.evaluate(maxplayer, minplayer, gs2)*Math.pow(0.99,time/10.0);
            
                while(leaf!=null) {
                    leaf.accum_evaluation += evaluation;
                    leaf.visit_count++;
                    leaf = leaf.parent;
                }
            }

        }
        
        if (DEBUG>=1) {
            if (DEBUG>=2) {
                tree.showNode(0,1);
            } else {
                tree.showNode(0,0);                
            }
        }        
        
        PlayerAction a = getMostVisited(tree, gs.getTime());
        
        if (DEBUG>=1) System.out.println("Selected action:" + a);
        
        return a;
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
}
