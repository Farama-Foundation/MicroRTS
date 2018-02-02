/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.mcts.uct;

import ai.core.AI;
import ai.RandomBiasedAI;
import ai.core.AIWithComputationBudget;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import ai.core.InterruptibleAI;
import static ai.mcts.uct.UCT.DEBUG;

/**
 *
 * @author santi
 */
public class UCTUnitActions extends AIWithComputationBudget implements InterruptibleAI {
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
    
    
    public UCTUnitActions(UnitTypeTable utt) {
        this(100,-1,100,10,
             new RandomBiasedAI(),
             new SimpleSqrtEvaluationFunction3());
    }       
    
    
    public UCTUnitActions(int available_time, int available_playouts, int lookahead, int max_depth, AI policy, EvaluationFunction a_ef) {
        super(available_time, available_playouts);
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
        return new UCTUnitActions(TIME_BUDGET, ITERATIONS_BUDGET, MAXSIMULATIONTIME, MAX_TREE_DEPTH, randomAI, ef);
    }  
    
    
    public PlayerAction getAction(int player, GameState gs) throws Exception
    {
        if (gs.canExecuteAnyAction(player)) {
            startNewComputation(player,gs.clone());
            computeDuringOneGameFrame();
            return getBestActionSoFar();
        } else {
            return new PlayerAction();        
        }       
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
//        long cutOffTime = (TIME_BUDGET>0 ? start + TIME_BUDGET:0);
        long end = start;
        long count = 0;
        
        while(true) {
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
            count++;
            end = System.currentTimeMillis();
            if (TIME_BUDGET>=0 && (end - start)>=TIME_BUDGET) break; 
            if (ITERATIONS_BUDGET>=0 && count>=ITERATIONS_BUDGET) break;                        
        }
        
        total_cycles_executed++;
    }
    
    
    public PlayerAction getBestActionSoFar() {
        if (tree.children==null) {
            if (DEBUG>=1) System.out.println(this.getClass().getSimpleName() + " no children selected. Returning an empty asction");
            return new PlayerAction();
        }
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
    
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + TIME_BUDGET + ", " + ITERATIONS_BUDGET + ", " + MAXSIMULATIONTIME + ", " + MAX_TREE_DEPTH + ", " + randomAI + ", " + ef + ")";
    }
    
    
    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("TimeBudget",int.class,100));
        parameters.add(new ParameterSpecification("IterationsBudget",int.class,-1));
        parameters.add(new ParameterSpecification("PlayoutLookahead",int.class,100));
        parameters.add(new ParameterSpecification("MaxTreeDepth",int.class,10));
        
        parameters.add(new ParameterSpecification("DefaultPolicy",AI.class, randomAI));
        parameters.add(new ParameterSpecification("EvaluationFunction", EvaluationFunction.class, new SimpleSqrtEvaluationFunction3()));

        return parameters;
    }    
    
    
    public int getPlayoutLookahead() {
        return MAXSIMULATIONTIME;
    }
    
    
    public void setPlayoutLookahead(int a_pola) {
        MAXSIMULATIONTIME = a_pola;
    }


    public int getMaxTreeDepth() {
        return MAX_TREE_DEPTH;
    }
    
    
    public void setMaxTreeDepth(int a_mtd) {
        MAX_TREE_DEPTH = a_mtd;
    }
    
    
    public AI getDefaultPolicy() {
        return randomAI;
    }
    
    
    public void setDefaultPolicy(AI a_dp) {
        randomAI = a_dp;
    }
    
    
    public EvaluationFunction getEvaluationFunction() {
        return ef;
    }
    
    
    public void setEvaluationFunction(EvaluationFunction a_ef) {
        ef = a_ef;
    }
}
