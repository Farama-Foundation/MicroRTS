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
public class DownsamplingUCTNode {
    public static int DEBUG = 0;

    static Random r = new Random();
//    static float C = 50;   // this is the constant that regulates exploration vs exploitation, it must be tuned for each domain
//    static float C = 5;   // this is the constant that regulates exploration vs exploitation, it must be tuned for each domain
    static float C = 0.05f;   // this is the constant that regulates exploration vs exploitation, it must be tuned for each domain
    
    public int type;    // 0 : max, 1 : min, -1: Game-over
    DownsamplingUCTNode parent = null;
    public GameState gs;
    int depth = 0;  // the depth in the tree
    
    boolean hasMoreActions = true;
    PlayerActionGenerator moveGenerator = null;
    public List<PlayerAction> actions = null;
    public List<DownsamplingUCTNode> children = null;
    float evaluation_bound = 0;
    float accum_evaluation = 0;
    int visit_count = 0;
    
    public DownsamplingUCTNode(int maxplayer, int minplayer, GameState a_gs, DownsamplingUCTNode a_parent, long MAXACTIONS, float bound) throws Exception {
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
            moveGenerator = new PlayerActionGenerator(a_gs, maxplayer);
            moveGenerator.randomizeOrder();
        } else if (gs.canExecuteAnyAction(minplayer)) {
            type = 1;
            moveGenerator = new PlayerActionGenerator(a_gs, minplayer);
            moveGenerator.randomizeOrder();
        } else {
            type = -1;
            System.err.println("RTMCTSNode: This should not have happened...");
        }             
    }
    
    public DownsamplingUCTNode UCTSelectLeaf(int maxplayer, int minplayer, long MAXACTIONS, long cutOffTime, int max_depth) throws Exception {
        // Cut the tree policy at a predefined depth
        if (depth>=max_depth) return this;        

        // Downsample the number of actions:
        if (moveGenerator!=null && actions==null) {
            actions = new ArrayList<PlayerAction>();
            children = new ArrayList<DownsamplingUCTNode>();
            if (moveGenerator.getSize()>2*MAXACTIONS) {
                for(int i = 0;i<MAXACTIONS;i++) {
                    actions.add(moveGenerator.getRandom());
                }
            } else {      
                PlayerAction pa = null;
                long count = 0;
                do{
                    pa = moveGenerator.getNextAction(cutOffTime);
                    if (pa!=null) {
                        actions.add(pa);
                        count++;
                        if (count>=2*MAXACTIONS) break; // this is needed since some times, moveGenerator.size() overflows
                    }
                }while(pa!=null);
                while(actions.size()>MAXACTIONS) actions.remove(r.nextInt(actions.size()));
            }            
        }
        
        if (hasMoreActions) {
            if (moveGenerator==null) return this;
            if (children.size()>=actions.size()) {
                hasMoreActions = false;
            } else {
                PlayerAction a = actions.get(children.size());
                GameState gs2 = gs.cloneIssue(a);                
                DownsamplingUCTNode node = new DownsamplingUCTNode(maxplayer, minplayer, gs2.clone(), this, MAXACTIONS, evaluation_bound);
                children.add(node);
                return node;                
            }            
        }
        
        // Bandit policy:
        double best_score = 0;
        DownsamplingUCTNode best = null;
        for(int i = 0;i<children.size();i++) {
            DownsamplingUCTNode child = children.get(i);
            double exploitation = ((double)child.accum_evaluation) / child.visit_count;
            double exploration = Math.sqrt(Math.log((double)visit_count)/child.visit_count);
            if (type==0) {
                // max node:
                exploitation = (exploitation + evaluation_bound)/(2*evaluation_bound);
            } else {
                exploitation = - (exploitation - evaluation_bound)/(2*evaluation_bound);                
            }
            double tmp = C*exploitation + exploration;
            if (best==null || tmp>best_score) {
                best = child;
                best_score = tmp;
            }
        } 
        
        if (best==null) return this;
        return best.UCTSelectLeaf(maxplayer, minplayer, MAXACTIONS, cutOffTime, max_depth);
    }    
    
    
    public void showNode(int depth, int maxdepth) {
        if (children!=null) {
            for(int i = 0;i<children.size();i++) {
                DownsamplingUCTNode child = children.get(i);
                for(int j = 0;j<depth;j++) System.out.print("    ");
                System.out.println("child " + actions.get(i) + " explored " + child.visit_count + " Avg evaluation: " + (child.accum_evaluation/((double)child.visit_count)));
                if (depth<maxdepth) child.showNode(depth+1,maxdepth);
            }        
        }
    }
}
