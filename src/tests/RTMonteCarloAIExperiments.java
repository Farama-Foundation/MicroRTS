/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.abstraction.LightRush;
import ai.montecarlo.NaiveMonteCarlo;
import ai.*;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.evaluation.SimpleEvaluationFunction;
import ai.montecarlo.DownsamplingMonteCarlo;
import ai.montecarlo.ContinuingDownsamplingMC;
import ai.montecarlo.ContinuingNaiveMC;
import ai.naivemcts.ContinuingNaiveMCTS;
import ai.rtminimax.IDContinuingRTMinimax;
import ai.rtminimax.IDContinuingRTMinimax;
import ai.naivemcts.NaiveMCTS;
import ai.uct.ContinuingDownsamplingUCT;
import ai.uct.ContinuingUCT;
import ai.uct.ContinuingUCTUnitActions;
import ai.uct.UCT;
import gui.PhysicalGameStatePanel;
import javax.swing.JFrame;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class RTMonteCarloAIExperiments {

    public static void main(String args[]) throws Exception {
        int MAXCYCLES = 3000;
        boolean visualize = true;

        int RTMM_TIME = 100;

        int loses[][] = new int[2][6];
        int ties[][] = new int[2][6];
        int wins[][] = new int[2][6];
        int accum_loses_length[][] = new int[2][6];
        int accum_ties_length[][] = new int[2][6];
        int accum_wins_length[][] = new int[2][6];
        int mc_runs_per_cycle[] = new int[6];
        int mc_runs_per_action[] = new int[6];
        int mc_count[] = new int[6];
        
        // to track the length of the games

        for (int ai1_idx = 0; ai1_idx < 2; ai1_idx++) 
        {
//            int ai1_idx = 1;
            for (int ai2_idx = 0; ai2_idx < 6; ai2_idx++) 
            {
//                int ai2_idx = 3;
                for (int i = 0; i < 10; i++) {
                     AI ai1 = null;
                    AI ai2 = null;

                    switch (ai1_idx) {
                        case 0:
//                            ai1 = new RandomBiasedAI();
                            ai1 = new ContinuingUCT(100, RTMM_TIME, new RandomBiasedAI(), new SimpleEvaluationFunction());
//                            ai1 = new WorkerRushAI();
                            break;
                        case 1:
                            ai1 = new LightRush(UnitTypeTable.utt, new AStarPathFinding());
                            break;
                    }
                    switch (ai2_idx) {
                        case 0:
                            ai2 = new ContinuingUCTUnitActions(100,RTMM_TIME, new RandomBiasedAI(), new SimpleEvaluationFunction());
//                            ai2 = new ContinuingUCT(100, RTMM_TIME, new RandomBiasedAI());
//                            ai2 = new ContinuingNaiveMCTS(100, RTMM_TIME, new RandomBiasedAI());
                            break;
                        case 1:
                            ai2 = new ContinuingDownsamplingMC(200, 100, RTMM_TIME, new RandomBiasedAI(), new SimpleEvaluationFunction());
                            break;
                        case 2:
                            ai2 = new ContinuingDownsamplingMC(500, 100, RTMM_TIME, new RandomBiasedAI(), new SimpleEvaluationFunction());
                            break;
                        case 3:
                            ai2 = new ContinuingDownsamplingMC(1000, 100, RTMM_TIME, new RandomBiasedAI(), new SimpleEvaluationFunction());
                            break;
                        case 4:
                            ai2 = new ContinuingDownsamplingMC(2000, 100, RTMM_TIME, new RandomBiasedAI(), new SimpleEvaluationFunction());
                            break;
                        case 5:
                            ai2 = new ContinuingDownsamplingMC(4000, 100, RTMM_TIME, new RandomBiasedAI(), new SimpleEvaluationFunction());
                            break;
                    }


                    PhysicalGameState pgs = MapGenerator.basesWorkers8x8();
                    GameState gs = new GameState(pgs,UnitTypeTable.utt);
                    JFrame w = null;
                    if (visualize) w = PhysicalGameStatePanel.newVisualizer(gs, 600, 600);

                    System.out.println("MATCH UP: " + ai1 + " vs " + ai2);
                    System.gc();

                    boolean gameover = false;
                    do {
                        PlayerAction pa1 = ai1.getAction(0, gs);
                        PlayerAction pa2 = ai2.getAction(1, gs);
                        gs.issueSafe(pa1);
                        gs.issueSafe(pa2);

                        // simulate:
                        gameover = gs.cycle();
                        if (w!=null) w.repaint();
                        try {
                            Thread.sleep(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } while (!gameover && gs.getTime() < MAXCYCLES);
                    if (w!=null) w.dispose();

                    int winner = gs.winner();
                    System.out.println("Winner: " + winner + "  in " + gs.getTime() + " cycles");
                    if (winner == -1) {
                        ties[ai1_idx][ai2_idx]++;
                        accum_ties_length[ai1_idx][ai2_idx]+=gs.getTime();
                    } else if (winner == 0) {
                        loses[ai1_idx][ai2_idx]++;
                        accum_loses_length[ai1_idx][ai2_idx]+=gs.getTime();
                    } else if (winner == 1) {
                        wins[ai1_idx][ai2_idx]++;
                        accum_wins_length[ai1_idx][ai2_idx]+=gs.getTime();
                    }
                    
                    if (ai2 instanceof ContinuingDownsamplingMC) {
                        mc_runs_per_cycle[ai2_idx] += ((double)((ContinuingDownsamplingMC)ai2).total_runs) / ((ContinuingDownsamplingMC)ai2).total_cycles_executed;
                        mc_runs_per_action[ai2_idx] += ((double)((ContinuingDownsamplingMC)ai2).total_runs) / ((ContinuingDownsamplingMC)ai2).total_actions_issued;                        
                        mc_count[ai2_idx]++;
                    }
                    if (ai2 instanceof ContinuingNaiveMC) {
                        mc_runs_per_cycle[ai2_idx] += ((double)((ContinuingNaiveMC)ai2).total_runs) / ((ContinuingNaiveMC)ai2).total_cycles_executed;
                        mc_runs_per_action[ai2_idx] += ((double)((ContinuingNaiveMC)ai2).total_runs) / ((ContinuingNaiveMC)ai2).total_actions_issued;                        
                        mc_count[ai2_idx]++;
                    }
                    
                }
            }
        }

        for (int ai1_idx = 0; ai1_idx < 2; ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < 6; ai2_idx++) {
                if (ties[ai1_idx][ai2_idx]>0) accum_ties_length[ai1_idx][ai2_idx]/=ties[ai1_idx][ai2_idx];
                if (loses[ai1_idx][ai2_idx]>0) accum_loses_length[ai1_idx][ai2_idx]/=loses[ai1_idx][ai2_idx];
                if (wins[ai1_idx][ai2_idx]>0) accum_wins_length[ai1_idx][ai2_idx]/=wins[ai1_idx][ai2_idx];
            }
        }
        
        System.out.println("wins: ");
        showMatrix(wins);
        System.out.println("ties: ");
        showMatrix(ties);
        System.out.println("loses: ");
        showMatrix(loses);
        System.out.println("win time: ");
        showMatrix(accum_wins_length);
        System.out.println("tie time: ");
        showMatrix(accum_ties_length);
        System.out.println("lose time: ");
        showMatrix(accum_loses_length);
        
        System.out.println("Number of Montecarlo runs per cycle:");
        for(int i = 0;i<6;i++) {
            System.out.println(((double)mc_runs_per_cycle[i])/mc_count[i]);
        }

        System.out.println("Number of Montecarlo runs per action:");
        for(int i = 0;i<6;i++) {
            System.out.println(((double)mc_runs_per_action[i])/mc_count[i]);
        }

    }
    
    static void showMatrix(int m[][]) {
        for (int ai1_idx = 0; ai1_idx < m.length; ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < m[ai1_idx].length; ai2_idx++) {
                System.out.print(m[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }        

        System.out.println("Avg. for AI2:");
        for (int ai2_idx = 0; ai2_idx < m[0].length; ai2_idx++) {
            float accum = 0;
            for (int ai1_idx = 0; ai1_idx < m.length; ai1_idx++) {
                accum+=m[ai1_idx][ai2_idx];
            }
            accum/=m.length;
            System.out.print(accum + ", ");
        }        
        System.out.println("");
    }
    
}
