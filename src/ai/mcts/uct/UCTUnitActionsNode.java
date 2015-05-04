/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.mcts.uct;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import rts.*;
import rts.units.Unit;

/**
 *
 * @author santi
 */
public class UCTUnitActionsNode {
    static Random r = new Random();
//    static float C = 50;   // this is the constant that regulates exploration vs exploitation, it must be tuned for each domain
//    static float C = 5;   // this is the constant that regulates exploration vs exploitation, it must be tuned for each domain
    static float C = 0.05f;   // this is the constant that regulates exploration vs exploitation, it must be tuned for each domain
    
    public int type;    // 0 : max, 1 : min, -1: Game-over
    UCTUnitActionsNode parent = null;
    public GameState gs;
    int depth = 0;
    
    public List<PlayerAction> actions = null;
    public List<UCTUnitActionsNode> children = null;
    float evaluation_bound = 0;
    float accum_evaluation = 0;
    int visit_count = 0;
    
    public UCTUnitActionsNode(int maxplayer, int minplayer, GameState a_gs, UCTUnitActionsNode a_parent, float bound) {
        parent = a_parent;
        if (parent==null) depth = 0;
                     else depth = parent.depth+1;
        gs = a_gs;
        evaluation_bound = bound;
        PhysicalGameState pgs = a_gs.getPhysicalGameState();

        while(gs.winner()==-1 && 
              !gs.gameover() &&
              !gs.canExecuteAnyAction(maxplayer) && 
              !gs.canExecuteAnyAction(minplayer)) gs.cycle();        
        if (gs.winner()!=-1 || gs.gameover()) {
            type = -1;
        } else if (gs.canExecuteAnyAction(maxplayer)) {
            type = 0;
            actions = null;
            for(Unit u:pgs.getUnits()) {
                if (u.getPlayer()==maxplayer) {
//                    if (a_gs.getTime()==1) {
//                        System.out.println(u + " -> " + a_gs.getActionAssignment(u));
//                    }
                    if (a_gs.getActionAssignment(u)==null) {
//                        System.out.println(u);;
                        actions = a_gs.getPlayerActionsSingleUnit(maxplayer, u);
                        break;
                    }
                }
            }            
            if (actions==null) System.err.println("UCTUnitActionNode: error when generating maxplayer node!");
            children = new ArrayList<UCTUnitActionsNode>();
        } else if (gs.canExecuteAnyAction(minplayer)) {
            type = 1;
            actions = null;
            for(Unit u:pgs.getUnits()) {
                if (u.getPlayer()==minplayer) {
                    if (a_gs.getActionAssignment(u)==null) {
                        actions = a_gs.getPlayerActionsSingleUnit(minplayer, u);
                        break;
                    }
                }
            }            
            if (actions==null) System.err.println("UCTUnitActionNode: error when generating minplayer node!");
            children = new ArrayList<UCTUnitActionsNode>();
        } else {
            type = -1;
            System.err.println("RTMCTSNode: This should not have happened...");
        }     
    }
    
    public UCTUnitActionsNode UCTSelectLeaf(int maxplayer, int minplayer, int max_depth) {
        // Cut the tree policy at a predefined depth
        if (depth>=max_depth) return this;        
        
        // if non visited children, visit:     
        if (children==null || actions==null) return this;
        if (children.size()<actions.size()) {
            PlayerAction a = actions.get(children.size());
            if (a!=null) {
                GameState gs2 = gs.cloneIssue(a);                
                UCTUnitActionsNode node = new UCTUnitActionsNode(maxplayer, minplayer, gs2.clone(), this, evaluation_bound);
                children.add(node);
                return node;                
            }
        }
        
        // Bandit policy:
        double best_score = 0;
        UCTUnitActionsNode best = null;
        for(int i = 0;i<children.size();i++) {
            UCTUnitActionsNode child = children.get(i);
            double exploitation = ((double)child.accum_evaluation) / child.visit_count;
            double exploration = Math.sqrt(Math.log(((double)visit_count)/child.visit_count));
            if (type==0) {
                // max node:
                exploitation = (exploitation + evaluation_bound)/(2*evaluation_bound);
            } else {
                exploitation = - (exploitation - evaluation_bound)/(2*evaluation_bound);
            }
//            System.out.println(exploitation + " + " + exploration);

            double tmp = C*exploitation + exploration;
            if (best==null || tmp>best_score) {
                best = child;
                best_score = tmp;
            }
        } 
        
        if (best==null) return this;
        return best.UCTSelectLeaf(maxplayer, minplayer, max_depth);
    }    
    
    
    public void showNode(int depth, int maxdepth) {
        int mostVisitedIdx = -1;
        UCTUnitActionsNode mostVisited = null;
        for(int i = 0;i<children.size();i++) {
            UCTUnitActionsNode child = children.get(i);
            for(int j = 0;j<depth;j++) System.out.print("    ");
            System.out.println("child " + actions.get(i) + " explored " + child.visit_count + " Avg evaluation: " + (child.accum_evaluation/((double)child.visit_count)));
            if (depth<maxdepth) child.showNode(depth+1,maxdepth);
        }        
    }
}
