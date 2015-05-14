/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.mcts.uct;

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
    public static float C = 0.05f;   // this is the constant that regulates exploration vs exploitation, it must be tuned for each domain
//    public static float C = 1;   // this is the constant that regulates exploration vs exploitation, it must be tuned for each domain
    
    public int type;    // 0 : max, 1 : min, -1: Game-over
    UCTNode parent = null;
    public GameState gs;
    int depth = 0;  // the depth in the tree
    
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
        if (parent==null) depth = 0;
                     else depth = parent.depth+1;        
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
            moveGenerator.randomizeOrder();
            actions = new ArrayList<>();
            children = new ArrayList<>();
        } else if (gs.canExecuteAnyAction(minplayer)) {
            type = 1;
//            actions = gs.getPlayerActions(minplayer);
            moveGenerator = new PlayerActionGenerator(a_gs, minplayer);
            moveGenerator.randomizeOrder();
            actions = new ArrayList<>();
            children = new ArrayList<>();
        } else {
            type = -1;
            System.err.println("RTMCTSNode: This should not have happened...");
        }     
    }
    
    public UCTNode UCTSelectLeaf(int maxplayer, int minplayer, long cutOffTime, int max_depth) throws Exception {
        
        // Cut the tree policy at a predefined depth
        if (depth>=max_depth) return this;        
        
        // if non visited children, visit:        
        if (hasMoreActions) {
            if (moveGenerator==null) {
//                System.out.println("No more leafs because moveGenerator = null!");
                return this;
            }
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
        for (UCTNode child : children) {
            double tmp = childValue(child);
            if (best==null || tmp>best_score) {
                best = child;
                best_score = tmp;
            }
        } 
        
        if (best==null) {
//            System.out.println("No more leafs because this node has no children!");
//            return null;
            return this;
        }
        return best.UCTSelectLeaf(maxplayer, minplayer, cutOffTime, max_depth);
//        return best;
    }    
    
        
    public double childValue(UCTNode child) {
        double exploitation = ((double)child.accum_evaluation) / child.visit_count;
        double exploration = Math.sqrt(Math.log((double)visit_count)/child.visit_count);
        if (type==0) {
            // max node:
            exploitation = (evaluation_bound + exploitation)/(2*evaluation_bound);
        } else {
            exploitation = (evaluation_bound - exploitation)/(2*evaluation_bound);
        }
//            System.out.println(exploitation + " + " + exploration);

        double tmp = C*exploitation + exploration;
        return tmp;
    }
    
    
    public void showNode(int depth, int maxdepth) {
        int mostVisitedIdx = -1;
        UCTNode mostVisited = null;
        for(int i = 0;i<children.size();i++) {
            UCTNode child = children.get(i);
            for(int j = 0;j<depth;j++) System.out.print("    ");
            System.out.println("child explored " + child.visit_count + " Avg evaluation: " + (child.accum_evaluation/((double)child.visit_count)) + " : " + actions.get(i));
            if (depth<maxdepth) child.showNode(depth+1,maxdepth);
        }        
    }
}
