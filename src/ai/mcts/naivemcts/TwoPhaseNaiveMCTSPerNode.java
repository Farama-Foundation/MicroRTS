/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.mcts.naivemcts;

import ai.*;
import ai.core.AI;
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
public class TwoPhaseNaiveMCTSPerNode extends AIWithComputationBudget implements InterruptibleAI {
    public static int DEBUG = 0;
    public EvaluationFunction ef = null;
       
    Random r = new Random();
    public AI randomAI = new RandomBiasedAI();
    long max_actions_so_far = 0;
    
    GameState gs_to_start_from = null;
    TwoPhaseNaiveMCTSNode tree = null;
    int node_creation_ID = 0;
            
    public int MAXSIMULATIONTIME = 1024;
    public int MAX_TREE_DEPTH = 10;
    
    int playerForThisComputation;
    
    public float phase1_epsilon_l = 0.3f;
    public float phase1_epsilon_g = 0.0f;
    public float phase1_epsilon_0 = 1.0f;
    public float phase2_epsilon_l = 0.3f;
    public float phase2_epsilon_g = 0.0f;
    public float phase2_epsilon_0 = 0.0f;
    public int phase1_budget = 100;
    
    public int phase1_global_strategy = NaiveMCTSNode.E_GREEDY;
    public int phase2_global_strategy = NaiveMCTSNode.E_GREEDY;
    
    boolean forceExplorationOfNonSampledActions = true;
    
    // statistics:
    public long total_runs = 0;
    public long total_cycles_executed = 0;
    public long total_actions_issued = 0;
    public long total_time = 0;
    
    
    public TwoPhaseNaiveMCTSPerNode(UnitTypeTable utt) {
        this(100,-1,100,10,
             0.3f, 0.0f, 1.0f,
             0.3f, 0.0f, 0.0f,
             100,
             new RandomBiasedAI(),
             new SimpleSqrtEvaluationFunction3(), true);
    } 
    
    
    public TwoPhaseNaiveMCTSPerNode(int available_time, int max_playouts, int lookahead, int max_depth, 
                               float el1, float eg1, float e01,
                               float el2, float eg2, float e02,
                               int p1_budget,
                               AI policy, EvaluationFunction a_ef,
                               boolean fensa) {
        super(available_time, max_playouts);
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        MAX_TREE_DEPTH = max_depth;
        phase1_epsilon_l = el1;
        phase1_epsilon_g = eg1;
        phase1_epsilon_0 = e01;
        phase2_epsilon_l = el2;
        phase2_epsilon_g = eg2;
        phase2_epsilon_0 = e02;
        phase1_budget = p1_budget;
        ef = a_ef;
        forceExplorationOfNonSampledActions = fensa;
    }    
    
    public TwoPhaseNaiveMCTSPerNode(int available_time, int max_playouts, int lookahead, int max_depth, 
                               float el1, float eg1, float e01, int a_gs1,
                               float el2, float eg2, float e02, int a_gs2,
                               int p1_budget,
                               AI policy, EvaluationFunction a_ef,
                               boolean fensa) {
        super(available_time, max_playouts);
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        MAX_TREE_DEPTH = max_depth;
        phase1_epsilon_l = el1;
        phase1_epsilon_g = eg1;
        phase1_epsilon_0 = e01;
        phase1_global_strategy = a_gs1;
        
        phase2_epsilon_l = el2;
        phase2_epsilon_g = eg2;
        phase2_epsilon_0 = e02;
        phase2_global_strategy = a_gs2;
        
        phase1_budget = p1_budget;
        ef = a_ef;
        forceExplorationOfNonSampledActions = fensa;
    }        

    public void reset() {
        tree = null;
        gs_to_start_from = null;
        total_runs = 0;
        total_cycles_executed = 0;
        total_actions_issued = 0;
        total_time = 0;
        node_creation_ID = 0;
    }    
        
    
    public AI clone() {
        return new TwoPhaseNaiveMCTSPerNode(TIME_BUDGET, ITERATIONS_BUDGET, MAXSIMULATIONTIME, MAX_TREE_DEPTH, 
                                             phase1_epsilon_l, phase1_epsilon_g, phase1_epsilon_0,
                                             phase2_epsilon_l, phase2_epsilon_g, phase2_epsilon_0,
                                             phase1_budget, randomAI, ef, forceExplorationOfNonSampledActions);
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
        node_creation_ID = 0;
        tree = new TwoPhaseNaiveMCTSNode(playerForThisComputation, 1-playerForThisComputation, gs, null, ef.upperBound(gs), node_creation_ID++, forceExplorationOfNonSampledActions);
        
        max_actions_so_far = Math.max(tree.moveGenerator.getSize(),max_actions_so_far);
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
        long end = start;
        long count = 0;
        while(true) {
            if (!iteration(playerForThisComputation)) break;
            count++;
            end = System.currentTimeMillis();
            if (TIME_BUDGET>=0 && (end - start)>=TIME_BUDGET) break; 
            if (ITERATIONS_BUDGET>=0 && count>=ITERATIONS_BUDGET) break;             
        }
//        System.out.println("HL: " + count + " time: " + (System.currentTimeMillis() - start) + " (" + available_time + "," + max_playouts + ")");
        total_time += (end - start);
        total_cycles_executed++;
    }
    
    
    public boolean iteration(int player) throws Exception {
        TwoPhaseNaiveMCTSNode leaf;
//        System.out.println("  " + n_phase1_iterations_left);
        leaf = tree.selectLeaf(player, 1-player, phase1_epsilon_l, phase1_epsilon_g, phase1_epsilon_0, phase1_global_strategy, 
                                                 phase2_epsilon_l, phase2_epsilon_g, phase2_epsilon_0, phase2_global_strategy, 
                                                 phase1_budget,
                                                 MAX_TREE_DEPTH, node_creation_ID++);

        if (leaf!=null) {            
            GameState gs2 = leaf.gs.clone();
            simulate(gs2, gs2.getTime() + MAXSIMULATIONTIME);

            int time = gs2.getTime() - gs_to_start_from.getTime();
            double evaluation = ef.evaluate(player, 1-player, gs2)*Math.pow(0.99,time/10.0);

            leaf.propagateEvaluation((float)evaluation,null);            

            total_runs++;
            
//            System.out.println(total_runs + " - " + epsilon_0 + ", " + epsilon_l + ", " + epsilon_g);
            
        } else {
            // no actions to choose from :)
            System.err.println(this.getClass().getSimpleName() + ": claims there are no more leafs to explore...");
            return false;
        }
        return true;
    }
    
    public PlayerAction getBestActionSoFar() {
        int idx = getMostVisitedActionIdx();
        if (idx==-1) {
            if (DEBUG>=1) System.out.println("TwoPhaseNaiveMCTS no children selected. Returning an empty asction");
            return new PlayerAction();
        }
        if (DEBUG>=2) tree.showNode(0,1,ef);
        if (DEBUG>=1) {
            NaiveMCTSNode best = (NaiveMCTSNode) tree.children.get(idx);
            System.out.println("TwoPhaseNaiveMCTS selected children " + tree.actions.get(idx) + " explored " + best.visit_count + " Avg evaluation: " + (best.accum_evaluation/((double)best.visit_count)));
        }
        return tree.actions.get(idx);
    }
    
    
    public int getMostVisitedActionIdx() {
        total_actions_issued++;
            
        int bestIdx = -1;
        NaiveMCTSNode best = null;
        if (DEBUG>=2) {
//            for(Player p:gs_to_start_from.getPlayers()) {
//                System.out.println("Resources P" + p.getID() + ": " + p.getResources());
//            }
            System.out.println("Number of playouts: " + tree.visit_count);
            tree.printUnitActionTable();
        }
        for(int i = 0;i<tree.children.size();i++) {
            NaiveMCTSNode child = (NaiveMCTSNode)tree.children.get(i);
            if (DEBUG>=2) {
                System.out.println("child " + tree.actions.get(i) + " explored " + child.visit_count + " Avg evaluation: " + (child.accum_evaluation/((double)child.visit_count)));
            }
//            if (best == null || (child.accum_evaluation/child.visit_count)>(best.accum_evaluation/best.visit_count)) {
            if (best == null || child.visit_count>best.visit_count) {
                best = child;
                bestIdx = i;
            }
        }
        
        return bestIdx;
    }
    
    
    public int getHighestEvaluationActionIdx() {
        total_actions_issued++;
            
        int bestIdx = -1;
        NaiveMCTSNode best = null;
        if (DEBUG>=2) {
//            for(Player p:gs_to_start_from.getPlayers()) {
//                System.out.println("Resources P" + p.getID() + ": " + p.getResources());
//            }
            System.out.println("Number of playouts: " + tree.visit_count);
            tree.printUnitActionTable();
        }
        for(int i = 0;i<tree.children.size();i++) {
            NaiveMCTSNode child = (NaiveMCTSNode)tree.children.get(i);
            if (DEBUG>=2) {
                System.out.println("child " + tree.actions.get(i) + " explored " + child.visit_count + " Avg evaluation: " + (child.accum_evaluation/((double)child.visit_count)));
            }
//            if (best == null || (child.accum_evaluation/child.visit_count)>(best.accum_evaluation/best.visit_count)) {
            if (best == null || (child.accum_evaluation/((double)child.visit_count))>(best.accum_evaluation/((double)best.visit_count))) {
                best = child;
                bestIdx = i;
            }
        }
        
        return bestIdx;
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
    
    public NaiveMCTSNode getTree() {
        return tree;
    }
    
    public GameState getGameStateToStartFrom() {
        return gs_to_start_from;
    }
    
    
    public String toString() {
        return getClass().getSimpleName() + "(" + TIME_BUDGET + ", " + ITERATIONS_BUDGET + ", " + MAXSIMULATIONTIME + "," + MAX_TREE_DEPTH + "," + 
                                             phase1_epsilon_l + ", " + phase1_epsilon_g + ", " + phase1_epsilon_0 + ", " + 
                                             phase2_epsilon_l + ", " + phase2_epsilon_g + ", " + phase2_epsilon_0 + ", " + 
                                             phase1_budget + ", " + randomAI + ", " + ef  + ")";       
    }
    
    public String statisticsString() {
        return "Total runs: " + total_runs + 
               ", runs per action: " + (total_runs/(float)total_actions_issued) + 
               ", runs per cycle: " + (total_runs/(float)total_cycles_executed) + 
               ", averate time per cycle: " + (total_time/(float)total_cycles_executed) + 
               ", max branching factor: " + max_actions_so_far;
    }
    
    
    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("TimeBudget",int.class,100));
        parameters.add(new ParameterSpecification("IterationsBudget",int.class,-1));
        parameters.add(new ParameterSpecification("PlayoutLookahead",int.class,100));
        parameters.add(new ParameterSpecification("MaxTreeDepth",int.class,10));
        
        parameters.add(new ParameterSpecification("E1_l",float.class,0.3));
        parameters.add(new ParameterSpecification("E1_g",float.class,0.0));
        parameters.add(new ParameterSpecification("E1_0",float.class,1.0));
                
        parameters.add(new ParameterSpecification("E2_l",float.class,0.3));
        parameters.add(new ParameterSpecification("E2_g",float.class,0.0));
        parameters.add(new ParameterSpecification("E2_0",float.class,0.0));

        parameters.add(new ParameterSpecification("Phase1_Budget",int.class,100));

        parameters.add(new ParameterSpecification("DefaultPolicy",AI.class, randomAI));
        parameters.add(new ParameterSpecification("EvaluationFunction", EvaluationFunction.class, new SimpleSqrtEvaluationFunction3()));

        parameters.add(new ParameterSpecification("ForceExplorationOfNonSampledActions",boolean.class,true));
        
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
    
    
    public float getE1_l() {
        return phase1_epsilon_l;
    }
    
    
    public void setE1_l(float a_e1_l) {
        phase1_epsilon_l = a_e1_l;
    }
    
    
    public float getE1_g() {
        return phase1_epsilon_g;
    }
    
    
    public void setE1_g(float a_e1_g) {
        phase1_epsilon_g = a_e1_g;
    }


    public float getE1_0() {
        return phase1_epsilon_0;
    }
    
    
    public void setE1_0(float a_e1_0) {
        phase1_epsilon_0 = a_e1_0;
    }
        
    
    public float getE2_l() {
        return phase2_epsilon_l;
    }
    
    
    public void setE2_l(float a_e2_l) {
        phase2_epsilon_l = a_e2_l;
    }
    
    
    public float getE2_g() {
        return phase2_epsilon_g;
    }
    
    
    public void setE2_g(float a_e2_g) {
        phase2_epsilon_g = a_e2_g;
    }


    public float getE2_0() {
        return phase2_epsilon_0;
    }
    
    
    public void setE2_0(float a_e2_0) {
        phase2_epsilon_0 = a_e2_0;
    }
    
    
    public int getPhase1_Budget() {
        return phase1_budget;
    }
    
    
    public void setPhase1_Budget(int a_p1b) {
        phase1_budget = a_p1b;
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
    
    public boolean getForceExplorationOfNonSampledActions() {
        return forceExplorationOfNonSampledActions;
    }
    
    public void setForceExplorationOfNonSampledActions(boolean fensa)
    {
        forceExplorationOfNonSampledActions = fensa;
    }
}
