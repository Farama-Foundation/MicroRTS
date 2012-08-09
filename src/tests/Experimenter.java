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
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;

/**
 *
 * @author santi
 */
public class Experimenter {
    public static int DEBUG = 0;
    
    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, int iterations, int max_cycles, boolean visualize) throws Exception {
        runExperiments(bots, maps, iterations, max_cycles, visualize, -1);
    }

    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, int iterations, int max_cycles, boolean visualize, int run_only_those_involving_this_AI) throws Exception {
        int wins[][] = new int[bots.size()][bots.size()];
        int ties[][] = new int[bots.size()][bots.size()];
        int loses[][] = new int[bots.size()][bots.size()];
        
        double win_time[][] = new double[bots.size()][bots.size()];
        double tie_time[][] = new double[bots.size()][bots.size()];
        double lose_time[][] = new double[bots.size()][bots.size()];

        List<AI> bots2 = new LinkedList<AI>();
        for(AI bot:bots) bots2.add(bot.clone());
        
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) 
        {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) 
            {
                if (run_only_those_involving_this_AI!=-1 &&
                    ai1_idx!=run_only_those_involving_this_AI &&
                    ai2_idx!=run_only_those_involving_this_AI) continue;
//                if (ai1_idx==0 && ai2_idx==0) continue;
                
                for(PhysicalGameState pgs:maps) {
                    
                    for (int i = 0; i < iterations; i++) {
                        AI ai1 = bots.get(ai1_idx);
                        AI ai2 = bots2.get(ai2_idx);

                        ai1.reset();
                        ai2.reset();

                        GameState gs = new GameState(pgs.clone());
                        JFrame w = null;
                        if (visualize) w = PhysicalGameStatePanel.newVisualizer(gs, 600, 600);

                        System.out.println("MATCH UP: " + ai1+ " vs " + ai2);
                        
                        boolean gameover = false;
                        do {
                            System.gc();
                            if (DEBUG>=1) {System.out.println("Garbage collecting done.");System.out.flush();}
                            PlayerAction pa1 = ai1.getAction(0, gs);
                            if (DEBUG>=1) {System.out.println("AI1 done.");System.out.flush();}
                            PlayerAction pa2 = ai2.getAction(1, gs);
                            if (DEBUG>=1) {System.out.println("AI2 done.");System.out.flush();}
                            gs.issueSafe(pa1);
                            if (DEBUG>=1) {System.out.println("issue action AI1 done.");System.out.flush();}
                            gs.issueSafe(pa2);
                            if (DEBUG>=1) {System.out.println("issue action AI2 done.");System.out.flush();}
                            gameover = gs.cycle();
                            if (DEBUG>=1) {System.out.println("cycle done.");System.out.flush();}
                            if (w!=null) w.repaint();
                            if (DEBUG>=1) {System.out.println("repaint done.");System.out.flush();}
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

                            ties[ai2_idx][ai1_idx]++;
                            tie_time[ai2_idx][ai1_idx]+=gs.getTime();
                        } else if (winner == 0) {
                            wins[ai1_idx][ai2_idx]++;
                            win_time[ai1_idx][ai2_idx]+=gs.getTime();

                            loses[ai2_idx][ai1_idx]++;
                            lose_time[ai2_idx][ai1_idx]+=gs.getTime();
                        } else if (winner == 1) {
                            loses[ai1_idx][ai2_idx]++;
                            lose_time[ai1_idx][ai2_idx]+=gs.getTime();

                            wins[ai2_idx][ai1_idx]++;
                            win_time[ai2_idx][ai1_idx]+=gs.getTime();
                        }                        
                    }                    
                }
            }
        }

        System.out.println("Notice that the results below are from the perspective of the 'bots1' list.");
        System.out.println("If, in your case, bots1 and bots2 is the same list, and you want the total results, ");
        System.out.println("you can extract them manually, by manipulating the tables below.");
        System.out.println("Wins: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                System.out.print(wins[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }
        System.out.println("Ties: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                System.out.print(ties[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }
        System.out.println("Loses: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                System.out.print(loses[ai1_idx][ai2_idx] + ", ");
            }
            System.out.println("");
        }        
       System.out.println("Win average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (wins[ai1_idx][ai2_idx]>0) {
                    System.out.print((win_time[ai1_idx][ai2_idx]/wins[ai1_idx][ai2_idx]) + ", ");
                } else {
                    System.out.print("-, ");
                }
            }
            System.out.println("");
        }
        System.out.println("Tie average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (ties[ai1_idx][ai2_idx]>0) {
                    System.out.print((tie_time[ai1_idx][ai2_idx]/ties[ai1_idx][ai2_idx]) + ", ");
                } else {
                    System.out.print("-, ");
                }
            }
            System.out.println("");
        }
        System.out.println("Lose average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
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
