/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.montecarlo;

import ai.AI;
import ai.RandomBiasedAI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleEvaluationFunction;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;

/**
 *
 * @author santi
 */
public class DownsamplingMonteCarlo extends AI {
    public static final int DEBUG = 1;
    EvaluationFunction ef = null;
    
    Random r = new Random();
    AI randomAI = new RandomBiasedAI();
    long max_actions_so_far = 0;
        
    long MAXACTIONS = 100;
    int NSIMULATIONS = 1000;
    int MAXSIMULATIONTIME = 1024;
    
    public DownsamplingMonteCarlo(long maxactions, int simulations, int lookahead, AI policy, EvaluationFunction a_ef) {
        MAXACTIONS = maxactions;
        NSIMULATIONS = simulations;
        MAXSIMULATIONTIME = lookahead;
        randomAI = policy;
        ef = a_ef;
    }


    public void reset() {        
    }    
        
    
    public AI clone() {
        return new DownsamplingMonteCarlo(MAXACTIONS, NSIMULATIONS, MAXSIMULATIONTIME, randomAI, ef);
    }
    
    
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (!gs.canExecuteAnyAction(player) || gs.winner()!=-1) {
            return new PlayerAction();
        }        
                
        PlayerActionGenerator pag = new PlayerActionGenerator(gs,player);
        pag.randomizeOrder();
        List<PlayerAction> l = new LinkedList<PlayerAction>();
        if (pag.getSize()>2*MAXACTIONS) {
            for(int i = 0;i<MAXACTIONS;i++) {
                l.add(pag.getRandom());
            }
            max_actions_so_far = Math.max(pag.getSize(),max_actions_so_far);
            if (DEBUG>=1) System.out.println("MontCarloAI for player " + player + " chooses between " + pag.getSize() + " actions [maximum so far " + max_actions_so_far + "] (cycle " + gs.getTime() + ")");
        } else {      
            PlayerAction pa = null;
            long count = 0;
            do{
                pa = pag.getNextAction(-1);
                if (pa!=null) {
                    l.add(pa);
                    count++;
                    if (count>=2*MAXACTIONS) break; // this is needed since some times, moveGenerator.size() overflows
                }
            }while(pa!=null);
            max_actions_so_far = Math.max(l.size(),max_actions_so_far);
            if (DEBUG>=1) System.out.println("MontCarloAI for player " + player + " chooses between " + l.size() + " actions [maximum so far " + max_actions_so_far + "] (cycle " + gs.getTime() + ")");
            while(l.size()>MAXACTIONS) l.remove(r.nextInt(l.size()));
        }
        
        
        PlayerAction best = null;
        float best_score = 0;
        int SYMS_PER_ACTION = NSIMULATIONS/l.size();
        for(PlayerAction pa:l) {
            float score = 0;
            for(int i = 0;i<SYMS_PER_ACTION;i++) {
                GameState gs2 = gs.cloneIssue(pa);
                GameState gs3 = gs2.clone();
                simulate(gs3,gs3.getTime() + MAXSIMULATIONTIME);
                int time = gs3.getTime() - gs2.getTime();
                // Discount factor:
                score += ef.evaluate(player, 1-player, gs3)*Math.pow(0.99,time/10.0);
            }
            if (best==null || score>best_score) {
                best = pa;
                best_score = score;
            }
            if (DEBUG>=2) System.out.println("child " + pa + " explored " + SYMS_PER_ACTION + " Avg evaluation: " + (score/((double)NSIMULATIONS)));
        }
        return best;
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
        return "DownsamplingMonteCarlo(" + NSIMULATIONS + "," + MAXSIMULATIONTIME + "," + MAXACTIONS + ")";
    }
    
}
