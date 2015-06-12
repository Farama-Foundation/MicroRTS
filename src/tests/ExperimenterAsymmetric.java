/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.BranchingFactorCalculator;
import ai.core.AI;
import gui.PhysicalGameStatePanel;
import java.io.PrintStream;
import java.util.List;
import javax.swing.JFrame;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class ExperimenterAsymmetric {
    public static boolean PRINT_BRANCHING_AT_EACH_MOVE = false;

    public static void runExperiments(List<AI> bots1, List<AI> bots2, List<PhysicalGameState> maps, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out) throws Exception {
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
                        long lastTimeActionIssued = 0;

                        ai1.reset();
                        ai2.reset();

                        GameState gs = new GameState(pgs.clone(),UnitTypeTable.utt);
                        JFrame w = null;
                        if (visualize) w = PhysicalGameStatePanel.newVisualizer(gs, 600, 600);

                        out.println("MATCH UP: " + ai1+ " vs " + ai2);
                        System.gc();
                        
                        boolean gameover = false;
                        do {
                            if (PRINT_BRANCHING_AT_EACH_MOVE) {
                                String bf1 = (gs.canExecuteAnyAction(0) ? ""+BranchingFactorCalculator.branchingFactorByResourceUsageSeparatingFast(gs, 0):"-");
                                String bf2 = (gs.canExecuteAnyAction(1) ? ""+BranchingFactorCalculator.branchingFactorByResourceUsageSeparatingFast(gs, 1):"-");
                                if (!bf1.equals("-") || !bf2.equals("-")) {
                                    out.print("branching\t" + bf1 + "\t" + bf2 + "\n");
                                }
                            }
                            PlayerAction pa1 = ai1.getAction(0, gs);
                            PlayerAction pa2 = ai2.getAction(1, gs);
                            if (gs.issueSafe(pa1)) lastTimeActionIssued = gs.getTime();
                            if (gs.issueSafe(pa2)) lastTimeActionIssued = gs.getTime();
                            gameover = gs.cycle();
                            if (w!=null) w.repaint();
                            try {
                                Thread.sleep(1);    // give time to the window to repaint
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } while (!gameover && 
                                 (gs.getTime() < max_cycles) && 
                                 (gs.getTime() - lastTimeActionIssued < max_inactive_cycles));
                        if (w!=null) w.dispose();
                        int winner = gs.winner();
                        out.println("Winner: " + winner + "  in " + gs.getTime() + " cycles");
                        out.println(ai1 + " : " + ai1.statisticsString());
                        out.println(ai2 + " : " + ai2.statisticsString());
                        out.flush();
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

        out.println("Notice that the results below are only from the perspective of the 'bots1' list.");
        out.println("If you want a symmetric experimentation, use the 'Experimenter' class");
        out.println("Wins: ");
        for (int ai1_idx = 0; ai1_idx < bots1.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots2.size(); ai2_idx++) {
                out.print(wins[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }
        out.println("Ties: ");
        for (int ai1_idx = 0; ai1_idx < bots1.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots2.size(); ai2_idx++) {
                out.print(ties[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }
        out.println("Loses: ");
        for (int ai1_idx = 0; ai1_idx < bots1.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots2.size(); ai2_idx++) {
                out.print(loses[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }        
       out.println("Win average time: ");
        for (int ai1_idx = 0; ai1_idx < bots1.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots2.size(); ai2_idx++) {
                if (wins[ai1_idx][ai2_idx]>0) {
                    out.print((win_time[ai1_idx][ai2_idx]/wins[ai1_idx][ai2_idx]) + ", ");
                } else {
                    out.print("-, ");
                }
            }
            out.println("");
        }
        out.println("Tie average time: ");
        for (int ai1_idx = 0; ai1_idx < bots1.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots2.size(); ai2_idx++) {
                if (ties[ai1_idx][ai2_idx]>0) {
                    out.print((tie_time[ai1_idx][ai2_idx]/ties[ai1_idx][ai2_idx]) + ", ");
                } else {
                    out.print("-, ");
                }
            }
            out.println("");
        }
        out.println("Lose average time: ");
        for (int ai1_idx = 0; ai1_idx < bots1.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots2.size(); ai2_idx++) {
                if (loses[ai1_idx][ai2_idx]>0) {
                    out.print((lose_time[ai1_idx][ai2_idx]/loses[ai1_idx][ai2_idx]) + ", ");
                } else {
                    out.print("-, ");
                }
            }
            out.println("");
        }              
        out.flush();
    }
}
