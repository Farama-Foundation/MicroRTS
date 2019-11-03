/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.mcts;

import ai.evaluation.EvaluationFunction;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PlayerAction;

/**
 *
 * @author santi
 */
public abstract class MCTSNode {
    public static Random r = new Random();

    public int type;    // 0 : max, 1 : min, -1: Game-over
    public MCTSNode parent = null;
    public GameState gs = null;
    public int depth = 0;  // the depth in the tree

    public List<PlayerAction> actions = null;
    public List<MCTSNode> children = null;

    public double accum_evaluation = 0;
    public int visit_count = 0;
    
    // These variables are just used to improve the efficiency of the algorithm, 
    // and avoid linear searches:
    // "creation_ID": Starts at 0 for the root of the tree, and increases by one
    //                in each node of the tree in order of creation.
    //                This is used in the abstaction AIs to quickly know if any child node
    //                is new or was there before.
    // "highest_children_creation_ID": holds the highest creation ID of all the children
    // "best_child_so_far": - This can be used to update the best child in constant time when using e-greedy strategies.
    //                      Since only one child's value is updated at each iteraiton, we only
    //                      have to compare the value of the newly updated child with 'best_child_so_far'
    //                      to determine which is the new best child in this iteration.
    //                      - best_child_so_far = -1 means that it has not been cached, so we need
    //                      to compute it from scratch.
    public int creation_ID = -1;
    public int highest_children_creation_ID = -1;
    public int best_child_so_far = -1;   
    
    
    public void showNode(int depth, int maxdepth, EvaluationFunction ef) {
        if (children!=null) {
            for(int i = 0;i<children.size();i++) {
                MCTSNode child = children.get(i);
                for(int j = 0;j<depth;j++) System.out.print("    ");
                System.out.println("child explored " + child.visit_count + " (EF: " + ef.evaluate(0, 1, child.gs) + ") Avg evaluation: " + (child.accum_evaluation/((double)child.visit_count)) + " : " + actions.get(i));
                if (depth<maxdepth) child.showNode(depth+1,maxdepth, ef);
            }        
        }
    }    
}
