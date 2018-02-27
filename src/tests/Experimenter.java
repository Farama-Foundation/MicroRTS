/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.core.AI;
import ai.*;
import gui.PhysicalGameStateJFrame;
import gui.PhysicalGameStatePanel;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.Trace;
import rts.TraceEntry;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class Experimenter {
    public static int DEBUG = 0;
    public static boolean GC_EACH_FRAME = true;
    
    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize) throws Exception {
        runExperiments(bots, maps, utt,iterations, max_cycles, max_inactive_cycles, visualize, System.out, -1, false);
    }

    public static void runExperimentsPartiallyObservable(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize) throws Exception {
        runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, System.out, -1, true);
    }

    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out) throws Exception {
        runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, out, -1, false);
    }

    public static void runExperimentsPartiallyObservable(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out) throws Exception {
        runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, out, -1, true);
    }

    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out, 
                                      int run_only_those_involving_this_AI, boolean partiallyObservable) throws Exception {
        runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, out, run_only_those_involving_this_AI, false, partiallyObservable);
    }
 
    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out, 
            int run_only_those_involving_this_AI, boolean skip_self_play, boolean partiallyObservable) throws Exception {
    	runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, out, run_only_those_involving_this_AI, skip_self_play, partiallyObservable,
        		false, false, "");
    }
    
    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out, 
                                      int run_only_those_involving_this_AI, boolean skip_self_play, boolean partiallyObservable,
                                      boolean saveTrace, boolean saveZip, String traceDir) throws Exception {
        int wins[][] = new int[bots.size()][bots.size()];
        int ties[][] = new int[bots.size()][bots.size()];
        int loses[][] = new int[bots.size()][bots.size()];
        
        double win_time[][] = new double[bots.size()][bots.size()];
        double tie_time[][] = new double[bots.size()][bots.size()];
        double lose_time[][] = new double[bots.size()][bots.size()];

        List<AI> bots2 = new LinkedList<>();
        for(AI bot:bots) bots2.add(bot.clone());
        
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) 
        {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) 
            {
                if (run_only_those_involving_this_AI!=-1 &&
                    ai1_idx!=run_only_those_involving_this_AI &&
                    ai2_idx!=run_only_those_involving_this_AI) continue;
//                if (ai1_idx==0 && ai2_idx==0) continue;
                if (skip_self_play && ai1_idx==ai2_idx) continue;
                int m=0;
                for(PhysicalGameState pgs:maps) {
                    
                    for (int i = 0; i < iterations; i++) {
                    	//cloning just in case an AI has a memory leak
                    	//by using a clone, it is discarded, along with the leaked memory,
                    	//after each game, rather than accumulating
                    	//over several games
                        AI ai1 = bots.get(ai1_idx).clone();
                        AI ai2 = bots2.get(ai2_idx).clone();
                        long lastTimeActionIssued = 0;

                        ai1.reset();
                        ai2.reset();

                        GameState gs = new GameState(pgs.clone(),utt);
                        PhysicalGameStateJFrame w = null;
                        if (visualize) w = PhysicalGameStatePanel.newVisualizer(gs, 600, 600, partiallyObservable);

                        out.println("MATCH UP: " + ai1 + " vs " + ai2);
                        
                        boolean gameover = false;
                        Trace trace = null;
                        TraceEntry te;
                        if(saveTrace){
                        	trace = new Trace(utt);
                        	te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                            trace.addEntry(te);
                        }
                        do {
                            if (GC_EACH_FRAME) System.gc();
                            PlayerAction pa1 = null, pa2 = null;
                            if (partiallyObservable) {
                                pa1 = ai1.getAction(0, new PartiallyObservableGameState(gs,0));
//                                if (DEBUG>=1) {System.out.println("AI1 done.");out.flush();}
                                pa2 = ai2.getAction(1, new PartiallyObservableGameState(gs,1));
//                                if (DEBUG>=1) {System.out.println("AI2 done.");out.flush();}
                            } else {
                                pa1 = ai1.getAction(0, gs);
                                if (DEBUG>=1) {System.out.println("AI1 done.");out.flush();}
                                pa2 = ai2.getAction(1, gs);
                                if (DEBUG>=1) {System.out.println("AI2 done.");out.flush();}
                            }
                            if (saveTrace && (!pa1.isEmpty() || !pa2.isEmpty())) {
                                te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                                te.addPlayerAction(pa1.clone());
                                te.addPlayerAction(pa2.clone());
                                trace.addEntry(te);
                            }
                            
                            if (gs.issueSafe(pa1)) lastTimeActionIssued = gs.getTime();
//                            if (DEBUG>=1) {System.out.println("issue action AI1 done: " + pa1);out.flush();}
                            if (gs.issueSafe(pa2)) lastTimeActionIssued = gs.getTime();
//                            if (DEBUG>=1) {System.out.println("issue action AI2 done:" + pa2);out.flush();}
                            gameover = gs.cycle();
                            if (DEBUG>=1) {System.out.println("cycle done.");out.flush();}
                            if (w!=null) {
                                w.setStateCloning(gs);
                                w.repaint();
                                try {
                                    Thread.sleep(1);    // give time to the window to repaint
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
//                              if (DEBUG>=1) {System.out.println("repaint done.");out.flush();}
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
                    m++;
                }
            }
        }

        out.println("Wins: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out.print(wins[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }
        out.println("Ties: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out.print(ties[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }
        out.println("Loses: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out.print(loses[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }        
        out.println("Win average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (wins[ai1_idx][ai2_idx]>0) {
                    out.print((win_time[ai1_idx][ai2_idx]/wins[ai1_idx][ai2_idx]) + ", ");
                } else {
                    out.print("-, ");
                }
            }
            out.println("");
        }
        out.println("Tie average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (ties[ai1_idx][ai2_idx]>0) {
                    out.print((tie_time[ai1_idx][ai2_idx]/ties[ai1_idx][ai2_idx]) + ", ");
                } else {
                    out.print("-, ");
                }
            }
            out.println("");
        }
        out.println("Lose average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
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
