/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.*;
import ai.montecarlo.*;
import ai.naivemcts.ContinuingNaiveMCTS;
import ai.rtminimax.IDContinuingRTMinimax;
import ai.rtminimax.IDContinuingRTMinimax;
import ai.naivemcts.NaiveMCTS;
import ai.rtminimax.IDContinuingRTMinimaxRandomized;
import ai.uct.UCT;
import gui.PhysicalGameStatePanel;
import java.util.List;
import javax.swing.JFrame;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;

/**
 *
 * @author santi
 */
public class ExperimenterAsymmetric {

    public static void runExperiments(List<AI> bots1, List<AI> bots2, List<PhysicalGameState> maps, int iterations, int max_cycles, boolean visualize) throws Exception {
        int wins[][] = new int[bots1.size()][bots2.size()];
        int ties[][] = new int[bots1.size()][bots2.size()];
        int loses[][] = new int[bots1.size()][bots2.size()];
        
        double win_time[][] = new double[bots1.size()][bots2.size()];
        double tie_time[][] = new double[bots1.size()][bots2.size()];
        double lose_time[][] = new double[bots1.size()][bots2.size()];

        for (int ai1_idx = 0; ai1_idx < bots1.size(); ai1_idx++) 
        {
            for (int ai2_idx = 0; ai2_idx < bots2.size(); ai2_idx++) 
            {
                for(PhysicalGameState pgs:maps) {
                    
                    for (int i = 0; i < iterations; i++) {
                        AI ai1 = bots1.get(ai1_idx);
                        AI ai2 = bots2.get(ai2_idx);

                        ai1.reset();
                        ai2.reset();

                        GameState gs = new GameState(pgs.clone());
                        JFrame w = null;
                        if (visualize) w = PhysicalGameStatePanel.newVisualizer(gs, 600, 600);

                        System.out.println("MATCH UP: " + ai1+ " vs " + ai2);
                        System.gc();
                        
                        boolean gameover = false;
                        do {
                            PlayerAction pa1 = ai1.getAction(0, gs);
                            PlayerAction pa2 = ai2.getAction(1, gs);
                            gs.issueSafe(pa1);
                            gs.issueSafe(pa2);
                            gameover = gs.cycle();
                            if (w!=null) w.repaint();
                            try {
                                Thread.sleep(1);    // give time to the window to repaint
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } while (!gameover && gs.getTime() < max_cycles);
                        if (w!=null) w.dispose();
                        int winner = gs.winner();
                        System.out.println("Winner: " + winner + "  in " + gs.getTime() + " cycles");
                        if (winner == -1) {
                            ties[ai1_idx][ai2_idx]++;
                            tie_time[ai1_idx][ai2_idx]+=gs.getTime();
                        } else if (winner == 0) {
                            wins[ai1_idx][ai2_idx]++;
                            win_time[ai1_idx][ai2_idx]+=gs.getTime();
                        } else if (winner == 1) {
                            loses[ai1_idx][ai2_idx]++;
                            lose_time[ai1_idx][ai2_idx]+=gs.getTime();
                        }                        
                    }                    
                }
            }
        }

        System.out.println("Notice that the results below are only from the perspective of the 'bots1' list.");
        System.out.println("If you want a symmetric experimentation, use the 'Experimenter' class");
        System.out.println("Wins: ");
        for (int ai1_idx = 0; ai1_idx < bots1.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots2.size(); ai2_idx++) {
                System.out.print(wins[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }
        System.out.println("Ties: ");
        for (int ai1_idx = 0; ai1_idx < bots1.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots2.size(); ai2_idx++) {
                System.out.print(ties[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }
        System.out.println("Loses: ");
        for (int ai1_idx = 0; ai1_idx < bots1.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots2.size(); ai2_idx++) {
                System.out.print(loses[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }        
       System.out.println("Win average time: ");
        for (int ai1_idx = 0; ai1_idx < bots1.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots2.size(); ai2_idx++) {
                if (wins[ai1_idx][ai2_idx]>0) {
                    System.out.print((win_time[ai1_idx][ai2_idx]/wins[ai1_idx][ai2_idx]) + ", ");
                } else {
                    System.out.print("-, ");
                }
            }
            System.out.println("");
        }
        System.out.println("Tie average time: ");
        for (int ai1_idx = 0; ai1_idx < bots1.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots2.size(); ai2_idx++) {
                if (ties[ai1_idx][ai2_idx]>0) {
                    System.out.print((tie_time[ai1_idx][ai2_idx]/ties[ai1_idx][ai2_idx]) + ", ");
                } else {
                    System.out.print("-, ");
                }
            }
            System.out.println("");
        }
        System.out.println("Lose average time: ");
        for (int ai1_idx = 0; ai1_idx < bots1.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots2.size(); ai2_idx++) {
                if (loses[ai1_idx][ai2_idx]>0) {
                    System.out.print((lose_time[ai1_idx][ai2_idx]/loses[ai1_idx][ai2_idx]) + ", ");
                } else {
                    System.out.print("-, ");
                }
            }
            System.out.println("");
        }              
        
    }
}
