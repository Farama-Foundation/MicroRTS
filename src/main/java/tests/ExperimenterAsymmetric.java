/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.BranchingFactorCalculatorDouble;
import ai.core.AI;
import gui.PhysicalGameStatePanel;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JFrame;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.Trace;
import rts.TraceEntry;
import rts.units.UnitTypeTable;
import util.RunnableWithTimeOut;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class ExperimenterAsymmetric {
    public static long BRANCHING_CALCULATION_TIMEOUT = 7200000;
    public static boolean PRINT_BRANCHING_AT_EACH_MOVE = false;
    
    static class BranchingCalculatorWithTimeOut {
        static double branching = 0;
        static boolean running = false;
        static double branching(GameState gs, int player, long timeOutMillis) throws Exception {
            if (running) throw new Exception("Two calls to BranchingCalculatorWithTimeOut in parallel!");
            branching = 0;
            running = true;
            try {
                RunnableWithTimeOut.runWithTimeout(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            branching = BranchingFactorCalculatorDouble.branchingFactorByResourceUsageSeparatingFast(gs, player);
                        }catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, timeOutMillis, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
//                System.out.println("BranchingCalculatorWithTimeOut: timeout!");
            }    
            running = false;
            return branching;
        }
    }

    public static void runExperiments(List<AI> bots1, List<AI> bots2, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out) throws Exception {
    	runExperiments(bots1, bots2, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, out, false, false, "");
        	
    }
        
    public static void runExperiments(List<AI> bots1, List<AI> bots2, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out, boolean saveTrace, boolean saveZip, String traceDir) throws Exception {
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
            	int m=0;
                for(PhysicalGameState pgs:maps) {
                    
                    for (int i = 0; i < iterations; i++) {
                    	//cloning just in case an AI has a memory leak
                    	//by using a clone, it is discarded, along with the leaked memory,
                    	//after each game, rather than accumulating
                    	//over several games
                        AI ai1 = bots1.get(ai1_idx).clone();
                        AI ai2 = bots2.get(ai2_idx).clone();
                        long lastTimeActionIssued = 0;

                        ai1.reset();
                        ai2.reset();

                        GameState gs = new GameState(pgs.clone(),utt);
                        JFrame w = null;
                        if (visualize) w = PhysicalGameStatePanel.newVisualizer(gs, 600, 600);

                        out.println("MATCH UP: " + ai1+ " vs " + ai2);
                        System.gc();
                        
                        boolean gameover = false;
                        Trace trace = null;
                        TraceEntry te;
                        if(saveTrace){
                        	trace = new Trace(utt);
                        	te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                            trace.addEntry(te);
                        }
                        do {
                            if (PRINT_BRANCHING_AT_EACH_MOVE) {
                                String bf1 = (gs.canExecuteAnyAction(0) ? ""+BranchingCalculatorWithTimeOut.branching(gs, 0, BRANCHING_CALCULATION_TIMEOUT):"-");
                                String bf2 = (gs.canExecuteAnyAction(1) ? ""+BranchingCalculatorWithTimeOut.branching(gs, 1, BRANCHING_CALCULATION_TIMEOUT):"-");
                                if (!bf1.equals("-") || !bf2.equals("-")) {
                                    out.print("branching\t" + bf1 + "\t" + bf2 + "\n");
                                }
                            }
                            PlayerAction pa1 = ai1.getAction(0, gs);
                            PlayerAction pa2 = ai2.getAction(1, gs);
                            
                            if (saveTrace && (!pa1.isEmpty() || !pa2.isEmpty())) {
                                te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                                te.addPlayerAction(pa1.clone());
                                te.addPlayerAction(pa2.clone());
                                trace.addEntry(te);
                            }
                            
                            if (gs.issueSafe(pa1)) lastTimeActionIssued = gs.getTime();
                            if (gs.issueSafe(pa2)) lastTimeActionIssued = gs.getTime();
                            gameover = gs.cycle();
                            if (w!=null){ 
                            	w.repaint();
                            	try {
                            		Thread.sleep(1);    // give time to the window to repaint
                            	} catch (Exception e) {
                            		e.printStackTrace();
                            	}
                            }
                        } while (!gameover && 
                                 (gs.getTime() < max_cycles) && 
                                 (gs.getTime() - lastTimeActionIssued < max_inactive_cycles));
                        ai1.gameOver(gs.winner());
                        ai2.gameOver(gs.winner());
                        if(saveTrace){
                        	te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
                        	trace.addEntry(te);
                        	XMLWriter xml;
                        	ZipOutputStream zip = null;
                        	String filename=ai1.toString()+"Vs"+ai2.toString()+"-"+m+"-"+i;
                        	filename=filename.replace("/", "");
                        	filename=filename.replace(")", "");
                        	filename=filename.replace("(", "");
                        	filename=traceDir+"/"+filename;
                        	if(saveZip){
                        		zip=new ZipOutputStream(new FileOutputStream(filename+".zip"));
                        		zip.putNextEntry(new ZipEntry("game.xml"));
                        		xml = new XMLWriter(new OutputStreamWriter(zip));
                        	}else{
                        		xml = new XMLWriter(new FileWriter(filename+".xml"));
                        	}
                        	trace.toxml(xml);
                        	xml.flush();
                        	if(saveZip){
                        		zip.closeEntry();
                        		zip.close();
                        	}
                        }
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
                    m++;
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
