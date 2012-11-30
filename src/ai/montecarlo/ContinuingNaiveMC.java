/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.montecarlo;

import ai.AI;
import ai.RandomBiasedAI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleEvaluationFunction;
import java.util.*;
import rts.*;
import rts.units.Unit;
import util.Pair;
import util.Sampler;

/**
 *
 * @author santi
 */
public class ContinuingNaiveMC extends AI {
    public static final int DEBUG = 0;
    EvaluationFunction ef = null;
    
    public class UnitActionTableEntry {
        Unit u;
        int nactions = 0;
        List<UnitAction> actions = null;
        float[] accum_evaluation = null;
        int[] visit_count = null;
    }    
    
    public class PlayerActionTableEntry {
        long code;
        int selectedUnitActions[];
        PlayerAction pa;
        float accum_evaluation = 0;
        int visit_count = 0;
    }
    
    Random r = new Random();
    AI randomAI = new RandomBiasedAI();
    long max_actions_so_far = 0;
    
    PlayerActionGenerator  moveGenerator = null;
    long multipliers[] = null;
    List<UnitActionTableEntry> unitActionTable = null;
    HashMap<Long,PlayerActionTableEntry> playerActionTable = null;
    GameState gs_to_start_from = null;
    int run = 0;
    
    // statistics:
    public long total_runs = 0;
    public long total_cycles_executed = 0;
    public long total_actions_issued = 0;    
        
    int TIME_PER_CYCLE = 100;
    int MAXSIMULATIONTIME = 1024;
    float epsilon1 = 0.25f;  
    float epsilon2 = 0.2f;
    int minSamples = 10;
    
    public ContinuingNaiveMC(int available_time, int lookahead, float e1, float e2, AI policy, EvaluationFunction a_ef) {
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        epsilon1 = e1;
        epsilon2 = e2;
        TIME_PER_CYCLE = available_time;
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
        multipliers = null;
        unitActionTable = null;
        playerActionTable = null;
        gs_to_start_from = null;
        run = 0;
    }    
    
    
    public AI clone() {
        return new ContinuingNaiveMC(TIME_PER_CYCLE, MAXSIMULATIONTIME, epsilon1, epsilon2, randomAI, ef);
    }
    
    
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.winner()!=-1) return new PlayerAction();
        if (gs.canExecuteAnyAction(player)) {
            // continue or start a search:
            if (moveGenerator==null) startNewSearch(player,gs);
            search(player, TIME_PER_CYCLE);
            PlayerAction best = getBestAction();
            resetSearch();
            return best;
        } else {
            if (moveGenerator!=null) {
                // continue previous search:
                search(player, TIME_PER_CYCLE);
            } else {
                // determine who will be the next player:
                GameState gs2 = gs.clone();
                while(gs2.winner()==-1 && 
                      !gs2.gameover() &&
                    !gs2.canExecuteAnyAction(0) && 
                    !gs2.canExecuteAnyAction(1)) gs2.cycle();
                if (gs2.canExecuteAnyAction(player)) {
                    // start a new search:
                    startNewSearch(player,gs2);
                    search(player, TIME_PER_CYCLE);
                    return new PlayerAction();
                } else {
                    return new PlayerAction();
                }
            }
        }
        
        return new PlayerAction();
    }    
    
    
    public void startNewSearch(int player, GameState gs) throws Exception {
        gs_to_start_from = gs;
        moveGenerator = new PlayerActionGenerator(gs, player);
        multipliers = new long[moveGenerator.getChoices().size()];
        unitActionTable = new LinkedList<UnitActionTableEntry>();
        long baseMultiplier = 1;
        int idx = 0;
        for (Pair<Unit, List<UnitAction>> choice : moveGenerator.getChoices()) {
            UnitActionTableEntry ae = new UnitActionTableEntry();
            ae.u = choice.m_a;
            ae.nactions = choice.m_b.size();
            ae.actions = choice.m_b;
            ae.accum_evaluation = new float[ae.nactions];
            ae.visit_count = new int[ae.nactions];
            for (int i = 0; i < ae.nactions; i++) {
                ae.accum_evaluation[i] = 0;
                ae.visit_count[i] = 0;
            }
            unitActionTable.add(ae);
            multipliers[idx] = baseMultiplier;
            baseMultiplier*=ae.nactions;
            idx++;
        }
        playerActionTable = new LinkedHashMap<Long,PlayerActionTableEntry>();    // associates action codes with children           
        run = 0;
    }
    
    
    public void resetSearch() {
        gs_to_start_from = null;
        moveGenerator = null;
        multipliers = null;
        unitActionTable = null;
        playerActionTable = null;
        run = 0;
    }    
    
    
    public void search(int player, long available_time) throws Exception {
        if (moveGenerator.getSize()==1) return;
//        System.out.println(moveGenerator.getSize());
        long start = System.currentTimeMillis();
        while((System.currentTimeMillis() - start)<available_time) {
            monteCarloRun(player, gs_to_start_from);
        }
        
        total_cycles_executed++;
    }
    
    
    public void monteCarloRun(int player, GameState gs) throws Exception {
        PlayerAction pa = null;
        long actionCode = 0;
        int selectedUnitActions[] = new int[unitActionTable.size()];

        if (run>0 && r.nextFloat()<epsilon2) {
            // explore the player action with the highest value found so far:
            PlayerActionTableEntry best = null;
            for(PlayerActionTableEntry pate:playerActionTable.values()) {
                if (best==null || (pate.accum_evaluation/pate.visit_count)>(best.accum_evaluation/best.visit_count)) {
                    best = pate;
                }
            }

            pa = best.pa;
            actionCode = best.code;
            selectedUnitActions = best.selectedUnitActions;
        } else {
            // For each unit, rank the unitActions according to preference:
            List<double []> distributions = new LinkedList<double []>();
            List<Integer> notSampledYet = new LinkedList<Integer>();
            for(UnitActionTableEntry ate:unitActionTable) {
                double []dist = new double[ate.nactions];
                double total = 0;
                int maxIdx = -1;
                float maxEvaluation = 0;
                int visits = 0;
                for(int i = 0;i<ate.nactions;i++) {
                    if (maxIdx==-1 || 
                        (visits!=0 && (ate.accum_evaluation[i]/ate.visit_count[i])>maxEvaluation) || 
                        (visits!=0 && ate.visit_count[i]==0)) {
                        maxIdx = i;
                        maxEvaluation = (ate.accum_evaluation[i]/ate.visit_count[i]);
                        visits = ate.visit_count[i];
                    }
                    dist[i] = 1;
                    total+=dist[i];
                }
                if (ate.visit_count[maxIdx]!=0) {
                    if (total>1) dist[maxIdx] = ((total - 1)/epsilon1) * (1 - epsilon1);
//                    System.out.println("Num: " + ate.nactions + " total: " + total + " max: " + dist[maxIdx]);
                }   // the maximum index has "1 - epsilon probability of being chosen

                if (DEBUG>=3) {
                    System.out.print("[ ");
                    for(int i = 0;i<ate.nactions;i++) System.out.print("(" + ate.visit_count[i] + "," + ate.accum_evaluation[i]/ate.visit_count[i] + ")");
                    System.out.println("]");
                    System.out.print("[ ");
                    for(int i = 0;i<dist.length;i++) System.out.print(dist[i] + " ");
                    System.out.println("]");
                }

                notSampledYet.add(distributions.size());
                distributions.add(dist);
            }

            // Select the best combination that results in a valid playeraction by epsilon-greedy sampling:
            ResourceUsage base_ru = new ResourceUsage();
            PhysicalGameState pgs = gs.getPhysicalGameState();
            for(Unit u:pgs.getUnits()) {
                UnitActionAssignment uaa = gs.getUnitActions().get(u);
                if (uaa!=null) {
                    ResourceUsage ru = uaa.action.resourceUsage(u, pgs);
                    base_ru.merge(ru);
                }
            }
            
            pa = new PlayerAction();
            actionCode = 0;
            pa.setResourceUsage(base_ru.clone());            
            while(!notSampledYet.isEmpty()) {
                int i = notSampledYet.remove(r.nextInt(notSampledYet.size()));
                
                try {
                    UnitActionTableEntry ate = unitActionTable.get(i);
                    int code;
                    UnitAction ua;
                    ResourceUsage r2;
                    do {
                        code = Sampler.weighted(distributions.get(i));
                        ua = ate.actions.get(code);
                        r2 = ua.resourceUsage(ate.u, pgs);
                    }while(!pa.getResourceUsage().consistentWith(r2, gs));
                    
                    pa.getResourceUsage().merge(r2);
                    pa.addUnitAction(ate.u, ua);

                    selectedUnitActions[i] = code;
                    actionCode+= ((long)code)*multipliers[i];
                    
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        PlayerActionTableEntry pate = playerActionTable.get(actionCode);
        if (pate==null) {
            pate = new PlayerActionTableEntry();
            pate.code = actionCode;
            pate.selectedUnitActions = selectedUnitActions;
            pate.pa = pa;
            playerActionTable.put(actionCode,pate);
        }

        // do the run!
        GameState gs2 = gs.cloneIssue(pa);
        GameState gs3 = gs2.clone();
        simulate(gs3,gs3.getTime() + MAXSIMULATIONTIME);
        int time = gs3.getTime() - gs2.getTime();
        // Discount factor:
        double eval = ef.evaluate(player, 1-player, gs3)*Math.pow(0.99,time/10.0);
        pate.accum_evaluation += eval;
        pate.visit_count++;

//            if (eval<-300) {
//                System.err.println(eval);
//                PhysicalGameStateVisualizer w = PhysicalGameStateVisualizer.newVisualizer(gs3);
//            }

        for(int i = 0;i<unitActionTable.size();i++) {
            int uaIdx = selectedUnitActions[i];
            unitActionTable.get(i).accum_evaluation[uaIdx]+= eval;
            unitActionTable.get(i).visit_count[uaIdx]++;
        }    
        
        run++;
        total_runs++;
    }
    
        
    public PlayerAction getBestAction() {
        // find the best:
        PlayerActionTableEntry best = null;
        PlayerActionTableEntry bestIgnoringMinSamples = null;
        for(PlayerActionTableEntry pate:playerActionTable.values()) {
            if (pate.visit_count>minSamples) {
                if (best==null || (pate.accum_evaluation/pate.visit_count)>(best.accum_evaluation/best.visit_count)) {
                    best = pate;
                }
            }
            if (bestIgnoringMinSamples==null || (pate.accum_evaluation/pate.visit_count)>(bestIgnoringMinSamples.accum_evaluation/bestIgnoringMinSamples.visit_count)) {
                bestIgnoringMinSamples = pate;
            }
        }
        if (best==null) best = bestIgnoringMinSamples;
        
        if (DEBUG>=2) printState(unitActionTable, playerActionTable);

        if (DEBUG>=1) {
            System.out.println("Executed " + run + " runs");
            System.out.println("Explored actions: " + playerActionTable.size() + " / " + moveGenerator.getSize());
            System.out.println("Selected action: " + best + " visited " + best.visit_count + " with average evaluation " + (best.accum_evaluation/best.visit_count));
        }        
        
        total_actions_issued++;
        
        if (best==null) return new PlayerAction();        
        return best.pa;        
    }
    
    
    public void printState(List<UnitActionTableEntry> unitActionTable,
                           HashMap<Long,PlayerActionTableEntry> playerActionTable) {
        System.out.println("Unit actions table:");
        for(UnitActionTableEntry uat : unitActionTable) {
            System.out.println("Actions for unit " + uat.u);
            for (int i = 0; i < uat.nactions; i++) {
                System.out.println("   " + uat.actions.get(i) + " visited " + uat.visit_count[i] + " with average evaluation " + (uat.accum_evaluation[i] / uat.visit_count[i]));
            }
        }        
        System.out.println("Player actions:");
        for(PlayerActionTableEntry pate:playerActionTable.values()) {
            System.out.println(pate.pa + " visited " + pate.visit_count + " with average evaluation " + (pate.accum_evaluation / pate.visit_count));
        }
        
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
        return "ContinuingNaiveMC(" + MAXSIMULATIONTIME + "," + epsilon1 + "," + epsilon2 +  ")";
    }
}
