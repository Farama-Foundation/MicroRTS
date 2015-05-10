/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.montecarlo;

import ai.core.AI;
import ai.RandomBiasedAI;
import ai.core.InterruptibleAIWithComputationBudget;
import ai.evaluation.EvaluationFunction;
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
public class MonteCarlo extends InterruptibleAIWithComputationBudget {
    public static final int DEBUG = 0;
    EvaluationFunction ef = null;
    
    
    public class PlayerActionTableEntry {
        PlayerAction pa;
        float accum_evaluation = 0;
        int visit_count = 0;
    }
    
    
    Random r = new Random();
    AI randomAI = new RandomBiasedAI();
    long max_actions_so_far = 0;
    
    PlayerActionGenerator  moveGenerator = null;
    boolean allMovesGenerated = false;
    List<PlayerActionTableEntry> actions = null;
    GameState gs_to_start_from = null;
    int run = 0;
    int playerForThisComputation;
    
    // statistics:
    public long total_runs = 0;
    public long total_cycles_executed = 0;
    public long total_actions_issued = 0;
        
    long MAXACTIONS = 100;
    int MAXSIMULATIONTIME = 1024;
    
    public MonteCarlo(int available_time, int playouts_per_cycle, int lookahead, AI policy, EvaluationFunction a_ef) {
        super(available_time, playouts_per_cycle);
        MAXACTIONS = -1;
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        ef = a_ef;
    }

    public MonteCarlo(int available_time, int playouts_per_cycle, int lookahead, long maxactions, AI policy, EvaluationFunction a_ef) {
        super(available_time, playouts_per_cycle);
        MAXACTIONS = maxactions;
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        ef = a_ef;
    }
    
    
    public void printStats() {
        if (total_cycles_executed>0 && total_actions_issued>0) {
            System.out.println("Average runs per cycle: " + ((double)total_runs)/total_cycles_executed);
            System.out.println("Average runs per action: " + ((double)total_runs)/total_actions_issued);
        }
    }
    
    public void reset() {
        moveGenerator = null;
        actions = null;
        gs_to_start_from = null;
        run = 0;
    }    
    
    public AI clone() {
        return new MonteCarlo(MAX_TIME, MAX_ITERATIONS, MAXSIMULATIONTIME, MAXACTIONS, randomAI, ef);
    }
    
    public void startNewComputation(int a_player, GameState gs) throws Exception {
        if (DEBUG>=2) System.out.println("Starting a new search...");
        if (DEBUG>=2) System.out.println(gs);
        playerForThisComputation = a_player;
        gs_to_start_from = gs;
        moveGenerator = new PlayerActionGenerator(gs,playerForThisComputation);
        moveGenerator.randomizeOrder();
        allMovesGenerated = false;
        actions = null;  
        run = 0;
    }    
    
    
    public void resetSearch() {
        if (DEBUG>=2) System.out.println("Resetting search...");
        gs_to_start_from = null;
        moveGenerator = null;
        actions = null;
        run = 0;
    }
    

    public void computeDuringOneGameFrame() throws Exception {
        if (DEBUG>=2) System.out.println("Search...");
        long start = System.currentTimeMillis();
        int nruns = 0;
        long cutOffTime = (MAX_TIME>0 ? System.currentTimeMillis() + MAX_TIME:0);
        if (MAX_TIME<=0) cutOffTime = 0;
        
        if (actions==null) {
            actions = new ArrayList<>();
            if (MAXACTIONS>0 && moveGenerator.getSize()>2*MAXACTIONS) {
                for(int i = 0;i<MAXACTIONS;i++) {
                    MonteCarlo.PlayerActionTableEntry pate = new MonteCarlo.PlayerActionTableEntry();
                    pate.pa = moveGenerator.getRandom();
                    actions.add(pate);
                }
                max_actions_so_far = Math.max(moveGenerator.getSize(),max_actions_so_far);
                if (DEBUG>=1) System.out.println("MontCarloAI (random action sampling) for player " + playerForThisComputation + " chooses between " + moveGenerator.getSize() + " actions [maximum so far " + max_actions_so_far + "] (cycle " + gs_to_start_from.getTime() + ")");
            } else {      
                PlayerAction pa;
                long count = 0;
                do{
                    pa = moveGenerator.getNextAction(cutOffTime);
                    if (pa!=null) {
                        MonteCarlo.PlayerActionTableEntry pate = new MonteCarlo.PlayerActionTableEntry();
                        pate.pa = pa;
                        actions.add(pate);
                        count++;
                        if (MAXACTIONS>0 && count>=2*MAXACTIONS) break; // this is needed since some times, moveGenerator.size() overflows
                    }
                }while(pa!=null);
                max_actions_so_far = Math.max(actions.size(),max_actions_so_far);
                if (DEBUG>=1) System.out.println("MontCarloAI (complete generation plus random reduction) for player " + playerForThisComputation + " chooses between " + actions.size() + " actions [maximum so far " + max_actions_so_far + "] (cycle " + gs_to_start_from.getTime() + ")");
                while(MAXACTIONS>0 && actions.size()>MAXACTIONS) actions.remove(r.nextInt(actions.size()));
            }      
        }
        
        while(true) {
            if (MAX_TIME>0 && (System.currentTimeMillis() - start)>=MAX_TIME) break;
            if (MAX_ITERATIONS>0 && nruns>=MAX_ITERATIONS) break;
            monteCarloRun(playerForThisComputation, gs_to_start_from);
            nruns++;
        }
        
        total_cycles_executed++;
    }
    

    public void monteCarloRun(int player, GameState gs) throws Exception {
        int idx = run%actions.size();
//        System.out.println(idx);
        PlayerActionTableEntry pate = actions.get(idx);

        GameState gs2 = gs.cloneIssue(pate.pa);
        GameState gs3 = gs2.clone();
        simulate(gs3,gs3.getTime() + MAXSIMULATIONTIME);
        int time = gs3.getTime() - gs2.getTime();

        pate.accum_evaluation += ef.evaluate(player, 1-player, gs3)*Math.pow(0.99,time/10.0);    
        pate.visit_count++;
        run++;
        total_runs++;
    }
    
    
    public PlayerAction getBestActionSoFar() {
        // find the best:
        PlayerActionTableEntry best = null;
        for(PlayerActionTableEntry pate:actions) {
            if (best==null || (pate.accum_evaluation/pate.visit_count)>(best.accum_evaluation/best.visit_count)) {
                best = pate;
            }
        }
        if (best==null) best = best;
        
        if (DEBUG>=1) {
            System.out.println("Executed " + run + " runs");
            System.out.println("Selected action: " + best + " visited " + best.visit_count + " with average evaluation " + (best.accum_evaluation/best.visit_count));
        }      
        
        total_actions_issued++;
        
        return best.pa;        
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
    
    public String toString() {
        return "MonteCarlo(" + MAXACTIONS + "," + MAX_TIME + "," +  MAX_ITERATIONS + "," + MAXSIMULATIONTIME + ")";
    }
    
}
