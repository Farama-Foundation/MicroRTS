/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.mcts.uct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;

/**
 *
 * @author santi
 */
public class UCTNodeFirstPlayUrgency {
    public static int DEBUG = 0;
    
    static Random r = new Random();
    public static float C = 0.05f;   // this is the constant that regulates exploration vs exploitation, it must be tuned for each domain
//    static float C = 1;   // this is the constant that regulates exploration vs exploitation, it must be tuned for each domain
    
    public int type;    // 0 : max, 1 : min, -1: Game-over
    UCTNodeFirstPlayUrgency parent = null;
    public GameState gs;
    int depth = 0;  // the depth in the tree
    
    boolean hasMoreActions = true;
    PlayerActionGenerator moveGenerator = null;
    public List<PlayerAction> actions = null;
    HashMap<Long,UCTNodeFirstPlayUrgency> childrenMap = new LinkedHashMap<Long,UCTNodeFirstPlayUrgency>();    // associates action codes with children
    public List<UCTNodeFirstPlayUrgency> children = null;
    float evaluation_bound = 0;
    float accum_evaluation = 0;
    int visit_count = 0;
    double FPUvalue = 0;
    
    
    public UCTNodeFirstPlayUrgency(int maxplayer, int minplayer, GameState a_gs, UCTNodeFirstPlayUrgency a_parent, float bound, double a_FPUValue) throws Exception {
        parent = a_parent;
        gs = a_gs;
        if (parent==null) depth = 0;
                     else depth = parent.depth+1;        
        evaluation_bound = bound;
        FPUvalue = a_FPUValue;

        while(gs.winner()==-1 && 
              !gs.gameover() &&
              !gs.canExecuteAnyAction(maxplayer) && 
              !gs.canExecuteAnyAction(minplayer)) gs.cycle();        
        if (gs.winner()!=-1 || gs.gameover()) {
            type = -1;
        } else if (gs.canExecuteAnyAction(maxplayer)) {
            type = 0;
//            actions = gs.getPlayerActions(maxplayer);
            moveGenerator = new PlayerActionGenerator(a_gs, maxplayer);
            moveGenerator.randomizeOrder();
            actions = new ArrayList<PlayerAction>();
            children = new ArrayList<UCTNodeFirstPlayUrgency>();
        } else if (gs.canExecuteAnyAction(minplayer)) {
            type = 1;
//            actions = gs.getPlayerActions(minplayer);
            moveGenerator = new PlayerActionGenerator(a_gs, minplayer);
            moveGenerator.randomizeOrder();
            actions = new ArrayList<PlayerAction>();
            children = new ArrayList<UCTNodeFirstPlayUrgency>();
        } else {
            type = -1;
            System.err.println("RTMCTSNode: This should not have happened...");
        }     
    }
    
    public UCTNodeFirstPlayUrgency UCTSelectLeaf(int maxplayer, int minplayer, long cutOffTime, int max_depth) throws Exception {
        
        // Cut the tree policy at a predefined depth
        if (depth>=max_depth) return this;   
        if (children==null) return null;
        
        // Bandit policy:
        double best_score = 0;
        UCTNodeFirstPlayUrgency best = null;
        if (DEBUG>=1) System.out.println("UCTNodeFirstPlayUrgency.UCTSelectLeaf:");
        for(int i = 0;i<children.size();i++) {
            UCTNodeFirstPlayUrgency child = children.get(i);
            double tmp = childValue(child);
            if (DEBUG>=1) System.out.println("  " + tmp);
            if (best==null || tmp>best_score) {
                best = child;
                best_score = tmp;
            }
        } 

        // First Play Urgency:
        if (best!=null && best_score>FPUvalue) return best.UCTSelectLeaf(maxplayer, minplayer, cutOffTime, max_depth);
        
        // if none of the already visited children have an urgency above the threshold, 
        // choose one at random:
        // TODO: here I should try not to repeat previously selected nodes. But this should work for now
        if (moveGenerator!=null) {
            PlayerAction a = moveGenerator.getRandom();
            long index = moveGenerator.getActionIndex(a);
            int attemptsLeft = 50;
            while(childrenMap.containsKey(index) && attemptsLeft>0) {
                a = moveGenerator.getRandom();
                index = moveGenerator.getActionIndex(a);
                attemptsLeft--;
            }

            if (attemptsLeft>0) {
                actions.add(a);
                GameState gs2 = gs.cloneIssue(a);                
                UCTNodeFirstPlayUrgency node = new UCTNodeFirstPlayUrgency(maxplayer, minplayer, gs2.clone(), this, evaluation_bound, FPUvalue);
                children.add(node);
                childrenMap.put(index, node);
                return node;                
            }
        }
            
        if (best==null) return this;
        return best.UCTSelectLeaf(maxplayer, minplayer, cutOffTime, max_depth);
    }    
    
        
    public double childValue(UCTNodeFirstPlayUrgency child) {
        double exploitation = ((double)child.accum_evaluation) / child.visit_count;
        double exploration = Math.sqrt(Math.log((double)visit_count)/child.visit_count);
        if (type==0) {
            // max node:
            exploitation = (exploitation + evaluation_bound)/(2*evaluation_bound);
        } else {
            exploitation = - (exploitation - evaluation_bound)/(2*evaluation_bound);
        }

        //System.out.println("    " + exploitation + " + " + exploration);
        double tmp = exploitation + C*exploration;
        return tmp;
    }
    
    
    public void showNode(int depth, int maxdepth) {
        int mostVisitedIdx = -1;
        UCTNodeFirstPlayUrgency mostVisited = null;
        for(int i = 0;i<children.size();i++) {
            UCTNodeFirstPlayUrgency child = children.get(i);
            for(int j = 0;j<depth;j++) System.out.print("    ");
            System.out.println("child explored " + child.visit_count + " Avg evaluation: " + (child.accum_evaluation/((double)child.visit_count)) + " : " + actions.get(i));
            if (depth<maxdepth) child.showNode(depth+1,maxdepth);
        }        
    }
}
