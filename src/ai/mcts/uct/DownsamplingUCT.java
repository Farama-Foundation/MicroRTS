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

/**
 *
 * @author santi
 */
public class DownsamplingUCT extends AIWithComputationBudget implements InterruptibleAI {
    public static final int DEBUG = 0;
    EvaluationFunction ef = null;
       
    Random r = new Random();
    AI randomAI = new RandomBiasedAI();
    long max_actions_so_far = 0;
    
    GameState gs_to_start_from = null;
    DownsamplingUCTNode tree = null;
    
    // statistics:
    public long total_runs = 0;
    public long total_cycles_executed = 0;
    public long total_actions_issued = 0;
        
    long MAXACTIONS = 100;
    int MAXSIMULATIONTIME = 1024;
    int MAX_TREE_DEPTH = 10;
    
    int playerForThisComputation;
    
    
    public DownsamplingUCT(UnitTypeTable utt) {
        this(100,-1,100,100,10,
             new RandomBiasedAI(),
             new SimpleSqrtEvaluationFunction3());
    }    

    
    public DownsamplingUCT(int available_time, int max_playouts, int lookahead, long maxactions, int max_depth, AI policy, EvaluationFunction a_ef) {
        super(available_time, max_playouts);
        MAXACTIONS = maxactions;
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        MAX_TREE_DEPTH =  max_depth;
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
        return new DownsamplingUCT(TIME_BUDGET, ITERATIONS_BUDGET, MAXSIMULATIONTIME, MAXACTIONS, MAX_TREE_DEPTH, randomAI, ef);
    }  
    
    
    public final PlayerAction getAction(int player, GameState gs) throws Exception
    {
        if (gs.canExecuteAnyAction(player)) {
            startNewComputation(player,gs.clone());
            computeDuringOneGameFrame();
            return getBestActionSoFar();
        } else {
            return new PlayerAction();        
        }       
    }
    
    
    public void startNewComputation(int a_player, GameState gs) throws Exception {
    	playerForThisComputation = a_player;
        float evaluation_bound = ef.upperBound(gs);
        tree = new DownsamplingUCTNode(playerForThisComputation, 1-playerForThisComputation, gs, null, MAXACTIONS, evaluation_bound);
        gs_to_start_from = gs;
    }    
    
    
    public void resetSearch() {
        if (DEBUG>=2) System.out.println("Resetting search...");
        tree = null;
        gs_to_start_from = null;
    }
    

    public void computeDuringOneGameFrame() throws Exception {
        if (DEBUG>=2) System.out.println("Search...");
        long start = System.currentTimeMillis();
        long cutOffTime = (TIME_BUDGET>0 ? start + TIME_BUDGET:0);
        long end = start;
        long count = 0;
        
        while(true) {
            DownsamplingUCTNode leaf = tree.UCTSelectLeaf(playerForThisComputation, 1-playerForThisComputation, MAXACTIONS, cutOffTime, MAX_TREE_DEPTH);
            
            if (leaf!=null) {
                GameState gs2 = leaf.gs.clone();
                simulate(gs2, gs2.getTime() + MAXSIMULATIONTIME);
                
                int time = gs2.getTime() - gs_to_start_from.getTime();
                double evaluation = ef.evaluate(playerForThisComputation, 1-playerForThisComputation, gs2)*Math.pow(0.99,time/10.0);
            
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
        total_actions_issued++;
                
        int mostVisitedIdx = -1;
        DownsamplingUCTNode mostVisited = null;
        for(int i = 0;i<tree.children.size();i++) {
            DownsamplingUCTNode child = tree.children.get(i);
            if (mostVisited == null || child.visit_count>mostVisited.visit_count) {
                mostVisited = child;
                mostVisitedIdx = i;
            }
        }
        
        if (mostVisitedIdx == -1) {
            System.err.println("DownsamplingUCT.getBestActionSoFar: mostVisitedIdx == -1!!! tree.children.size() = " + tree.children.size());
            return tree.moveGenerator.getRandom();
        }
        
        if (DEBUG>=2) tree.showNode(0,1);        
        if (DEBUG>=1) System.out.println(this.getClass().getSimpleName() + " selected children " + tree.actions.get(mostVisitedIdx) + " explored " + mostVisited.visit_count + " Avg evaluation: " + (mostVisited.accum_evaluation/((double)mostVisited.visit_count)));
        
//        printStats();        
        
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
    
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + TIME_BUDGET + ", " + ITERATIONS_BUDGET + ", " + MAXSIMULATIONTIME + ", " + MAXACTIONS + ", " + MAX_TREE_DEPTH + ", " + randomAI + ", " + ef + ")";
    }
    
    
    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("TimeBudget",int.class,100));
        parameters.add(new ParameterSpecification("IterationsBudget",int.class,-1));
        parameters.add(new ParameterSpecification("PlayoutLookahead",int.class,100));
        parameters.add(new ParameterSpecification("MaxActions",long.class,100));
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


    public long getMaxActions() {
        return MAXACTIONS;
    }
    
    
    public void setMaxActions(long a_ma) {
        MAXACTIONS = a_ma;
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
