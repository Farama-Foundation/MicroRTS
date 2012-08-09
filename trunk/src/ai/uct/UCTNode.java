/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.uct;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;

/**
 *
 * @author santi
 */
public class UCTNode {
    static Random r = new Random();
    
    public int type;    // 0 : max, 1 : min, -1: Game-over
    UCTNode parent = null;
    public GameState gs;
    
    boolean hasMoreActions = true;
    PlayerActionGenerator moveGenerator = null;
    public List<PlayerAction> actions = null;
    public List<UCTNode> children = null;
    float evaluation_bound = 0;
    float accum_evaluation = 0;
    int visit_count = 0;
    
    public UCTNode(int maxplayer, int minplayer, GameState a_gs, UCTNode a_parent, float bound) throws Exception {
        parent = a_parent;
        gs = a_gs;
        evaluation_bound = bound;

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
            actions = new ArrayList<PlayerAction>();
            children = new ArrayList<UCTNode>();
        } else if (gs.canExecuteAnyAction(minplayer)) {
            type = 1;
//            actions = gs.getPlayerActions(minplayer);
            moveGenerator = new PlayerActionGenerator(a_gs, minplayer);
            actions = new ArrayList<PlayerAction>();
            children = new ArrayList<UCTNode>();
        } else {
            type = -1;
            System.err.println("RTMCTSNode: This should not have happened...");
        }     
    }
    
    public UCTNode UCTSelectLeaf(int maxplayer, int minplayer, long cutOffTime) throws Exception {
        // if non visited children, visit:        
        if (hasMoreActions) {
            if (moveGenerator==null) return null;
            PlayerAction a = moveGenerator.getNextAction(cutOffTime);
            if (a!=null) {
                actions.add(a);
                GameState gs2 = gs.cloneIssue(a);                
                UCTNode node = new UCTNode(maxplayer, minplayer, gs2.clone(), this, evaluation_bound);
                children.add(node);
                return node;                
            } else {
                hasMoreActions = false;
            }
        }
        
        // Bandit policy:
        double best_score = 0;
        UCTNode best = null;
        for(int i = 0;i<children.size();i++) {
            UCTNode child = children.get(i);
            double exploitation = ((double)child.accum_evaluation) / child.visit_count;
            double exploration = Math.sqrt(Math.log(((double)visit_count)/child.visit_count));
            if (type==0) {
                // max node:
                exploitation = (exploitation + evaluation_bound)/(2*evaluation_bound);
            } else {
                exploitation = - (exploitation - evaluation_bound)/(2*evaluation_bound);
            }
//            System.out.println(exploitation + " + " + exploration);

//            double tmp = 140*exploitation + exploration;
            double tmp = 50*exploitation + exploration;
            if (best==null || tmp>best_score) {
                best = child;
                best_score = tmp;
            }
        } 
        
        if (best==null) return null;
        return best.UCTSelectLeaf(maxplayer, minplayer, cutOffTime);
//        return best;
    }    
    
    
    public void showNode(int depth, int maxdepth) {
        int mostVisitedIdx = -1;
        UCTNode mostVisited = null;
        for(int i = 0;i<children.size();i++) {
            UCTNode child = children.get(i);
            for(int j = 0;j<depth;j++) System.out.print("    ");
            System.out.println("child " + actions.get(i) + " explored " + child.visit_count + " Avg evaluation: " + (child.accum_evaluation/((double)child.visit_count)));
            if (depth<maxdepth) child.showNode(depth+1,maxdepth);
        }        
    }
}
