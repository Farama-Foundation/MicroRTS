/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tournaments;

import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ContinuingAI;
import ai.core.InterruptibleAI;
import gui.PhysicalGameStateJFrame;
import gui.PhysicalGameStatePanel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
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
public class RoundRobinTournament {

    public static boolean visualize = false;
    public static int TIMEOUT_CHECK_TOLERANCE = 20;
    public static boolean USE_CONTINUING_ON_INTERRUPTIBLE = true;

    public static void runTournament(List<AI> AIs,
            int playOnlyGamesInvolvingThisAI,
            List<String> maps,
            int iterations,
            int maxGameLength,
            int timeBudget,
            int iterationsBudget,
            long preAnalysisBudgetFirstTimeInAMap,
            long preAnalysisBudgetRestOfTimes, 
            boolean fullObservability,
            boolean selfMatches,
            boolean timeoutCheck,
            boolean runGC,
            boolean preAnalysis,
            UnitTypeTable utt,
            String traceOutputfolder,
            Writer out,
            Writer progress,
            String folderForReadWriteFolders) throws Exception {
        if (progress != null) {
            progress.write("RoundRobinTournament: Starting tournament\n");
        }

        int wins[][] = new int[AIs.size()][AIs.size()];
        int ties[][] = new int[AIs.size()][AIs.size()];
        int AIcrashes[][] = new int[AIs.size()][AIs.size()];
        int AItimeout[][] = new int[AIs.size()][AIs.size()];
        double accumTime[][] = new double[AIs.size()][AIs.size()];

        out.write("RoundRobinTournament\n");
        out.write("AIs\n");
        for (int i = 0; i < AIs.size(); i++) {
            out.write("\t" + AIs.get(i).toString() + "\n");
        }
        out.write("maps\n");
        for (int i = 0; i < maps.size(); i++) {
            out.write("\t" + maps.get(i) + "\n");
        }
        out.write("iterations\t" + iterations + "\n");
        out.write("maxGameLength\t" + maxGameLength + "\n");
        out.write("timeBudget\t" + timeBudget + "\n");
        out.write("iterationsBudget\t" + iterationsBudget + "\n");
        out.write("pregameAnalysisBudget\t" + preAnalysisBudgetFirstTimeInAMap + "\t" + preAnalysisBudgetRestOfTimes + "\n");
        out.write("fullObservability\t" + fullObservability + "\n");
        out.write("timeoutCheck\t" + timeoutCheck + "\n");
        out.write("runGC\t" + runGC + "\n");
        out.write("iteration\tmap\tai1\tai2\ttime\twinner\tcrashed\ttimedout\n");
        out.flush();
        
        // create all the read/write folders:
        String readWriteFolders[] = new String[AIs.size()];
        boolean firstPreAnalysis[][] = new boolean[AIs.size()][maps.size()];
        for(int i = 0;i<AIs.size();i++) {
            readWriteFolders[i] = folderForReadWriteFolders + "/AI" + i + "readWriteFolder";
            File f = new File(readWriteFolders[i]);
            f.mkdir();
            for(int j = 0;j<maps.size();j++) {
                firstPreAnalysis[i][j] = true;
            }
        }
        
        for (int iteration = 0; iteration < iterations; iteration++) {
            for (int map_idx = 0; map_idx < maps.size(); map_idx++) {
                PhysicalGameState pgs = PhysicalGameState.load(maps.get(map_idx), utt);
                for (int ai1_idx = 0; ai1_idx < AIs.size(); ai1_idx++) {
                    for (int ai2_idx = 0; ai2_idx < AIs.size(); ai2_idx++) {
                        if (!selfMatches && ai1_idx == ai2_idx) continue;
                        if (playOnlyGamesInvolvingThisAI!=-1) {
                            if (ai1_idx != playOnlyGamesInvolvingThisAI &&
                                ai2_idx != playOnlyGamesInvolvingThisAI) continue;
                        }
                        // variables to keep track of time ussage amongst the AIs:
                        int numTimes1 = 0;
                        int numTimes2 = 0;
                        double averageTime1 = 0;
                        double averageTime2 = 0;
                        int numberOfTimeOverBudget1 = 0;
                        int numberOfTimeOverBudget2 = 0;
                        double averageTimeOverBudget1 = 0;
                        double averageTimeOverBudget2 = 0;
                        int numberOfTimeOverTwiceBudget1 = 0;
                        int numberOfTimeOverTwiceBudget2 = 0;
                        double averageTimeOverTwiceBudget1 = 0;
                        double averageTimeOverTwiceBudget2 = 0;
                        
                        AI ai1 = AIs.get(ai1_idx).clone();
                        AI ai2 = AIs.get(ai2_idx).clone();

                        if (ai1 instanceof AIWithComputationBudget) {
                            ((AIWithComputationBudget) ai1).setTimeBudget(timeBudget);
                            ((AIWithComputationBudget) ai1).setIterationsBudget(iterationsBudget);
                        }
                        if (ai2 instanceof AIWithComputationBudget) {
                            ((AIWithComputationBudget) ai2).setTimeBudget(timeBudget);
                            ((AIWithComputationBudget) ai2).setIterationsBudget(iterationsBudget);
                        }
                        
                        if (USE_CONTINUING_ON_INTERRUPTIBLE) {
                            if (ai1 instanceof InterruptibleAI) ai1 = new ContinuingAI(ai1);
                            if (ai2 instanceof InterruptibleAI) ai2 = new ContinuingAI(ai2);
                        }

                        ai1.reset();
                        ai2.reset();
                        
                        GameState gs = new GameState(pgs.clone(), utt);
                        PhysicalGameStateJFrame w = null;
                        if (visualize) w = PhysicalGameStatePanel.newVisualizer(gs, 600, 600, !fullObservability);

                        if (progress != null) {
                            progress.write("MATCH UP: " + ai1 + " vs " + ai2 + "\n");
                        }
                        
                        if (preAnalysis) {
                            long preTime1 = preAnalysisBudgetRestOfTimes;
                            if (firstPreAnalysis[ai1_idx][map_idx]) {
                                preTime1 = preAnalysisBudgetFirstTimeInAMap;
                                firstPreAnalysis[ai1_idx][map_idx] = false;
                            }
                            long pre_start1 = System.currentTimeMillis();
                            ai1.preGameAnalysis(gs, preAnalysisBudgetRestOfTimes, readWriteFolders[ai1_idx]);
                            long pre_end1 = System.currentTimeMillis();
                            if (progress != null) {
                                progress.write("preGameAnalysis player 1 took " + (pre_end1 - pre_start1) + "\n");
                                if ((pre_end1 - pre_start1)>preTime1) progress.write("TIMEOUT PLAYER 1!\n");
                            }
                            long preTime2 = preAnalysisBudgetRestOfTimes;
                            if (firstPreAnalysis[ai2_idx][map_idx]) {
                                preTime2 = preAnalysisBudgetFirstTimeInAMap;
                                firstPreAnalysis[ai2_idx][map_idx] = false;
                            }
                            long pre_start2 = System.currentTimeMillis();
                            ai2.preGameAnalysis(gs, preTime2, readWriteFolders[ai2_idx]);
                            long pre_end2 = System.currentTimeMillis();
                            if (progress != null) {
                                progress.write("preGameAnalysis player 2 took " + (pre_end2 - pre_start2) + "\n");
                                if ((pre_end2 - pre_start2)>preTime2) progress.write("TIMEOUT PLAYER 2!\n");
                            }
                        }                        
                        

                        boolean gameover = false;
                        int crashed = -1;
                        int timedout = -1;
                        Trace trace = null;
                        TraceEntry te;
                        if (traceOutputfolder != null) {
                            trace = new Trace(utt);
                            te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
                            trace.addEntry(te);
                        }
                        do {
                            PlayerAction pa1 = null;
                            PlayerAction pa2 = null;
                            long AI1start = 0, AI2start = 0, AI1end = 0, AI2end = 0;
                            if (fullObservability) {
                                if (runGC) {
                                    System.gc();
                                }
                                try {
                                    AI1start = System.currentTimeMillis();
                                    pa1 = ai1.getAction(0, gs);
                                    AI1end = System.currentTimeMillis();
                                } catch (Exception e) {
                                    crashed = 0;
                                    break;
                                }
                                if (runGC) {
                                    System.gc();
                                }
                                try {
                                    AI2start = System.currentTimeMillis();
                                    pa2 = ai2.getAction(1, gs);
                                    AI2end = System.currentTimeMillis();
                                } catch (Exception e) {
                                    crashed = 1;
                                    break;
                                }
                            } else {
                                if (runGC) {
                                    System.gc();
                                }
                                try {
                                    PartiallyObservableGameState po_gs = new PartiallyObservableGameState(gs, 0);
                                    AI1start = System.currentTimeMillis();
                                    pa1 = ai1.getAction(0, po_gs);
                                    AI1end = System.currentTimeMillis();
                                } catch (Exception e) {
                                    crashed = 0;
                                    break;
                                }
                                if (runGC) {
                                    System.gc();
                                }
                                try {
                                    PartiallyObservableGameState po_gs = new PartiallyObservableGameState(gs, 1);
                                    AI2start = System.currentTimeMillis();
                                    pa2 = ai2.getAction(1, po_gs);
                                    AI2end = System.currentTimeMillis();
                                } catch (Exception e) {
                                    crashed = 1;
                                    break;
                                }
                            }
                                                        
                            {
                                long AI1time = AI1end - AI1start;
                                long AI2time = AI2end - AI2start;
                                numTimes1++;
                                numTimes2++;
                                averageTime1+=AI1time;
                                averageTime2+=AI2time;
                                if (AI1time > timeBudget) {
                                    numberOfTimeOverBudget1++;
                                    averageTimeOverBudget1 += AI1time;
                                    if (AI1time > timeBudget*2) {
                                        numberOfTimeOverTwiceBudget1++;
                                        averageTimeOverTwiceBudget1 += AI1time;
                                    }
                                }
                                if (AI2time > timeBudget) {
                                    numberOfTimeOverBudget2++;
                                    averageTimeOverBudget2 += AI2time;
                                    if (AI2time > timeBudget*2) {
                                        numberOfTimeOverTwiceBudget2++;
                                        averageTimeOverTwiceBudget2 += AI2time;                                        
                                    }
                                }
                                if (timeoutCheck) {
                                    if (AI1time > timeBudget + TIMEOUT_CHECK_TOLERANCE) {
                                        timedout = 0;
                                        break;
                                    }
                                    if (AI2time > timeBudget + TIMEOUT_CHECK_TOLERANCE) {
                                        timedout = 1;
                                        break;
                                    }
                                }
                            }
                            if (traceOutputfolder != null && (!pa1.isEmpty() || !pa2.isEmpty())) {
                                te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
                                te.addPlayerAction(pa1.clone());
                                te.addPlayerAction(pa2.clone());
                                trace.addEntry(te);
                            }

                            gs.issueSafe(pa1);
                            gs.issueSafe(pa2);
                            gameover = gs.cycle();
                            
                            if (w!=null) {
                                w.setStateCloning(gs);
                                w.repaint();
                                try {
                                    Thread.sleep(1);    // give time to the window to repaint
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            
                        } while (!gameover
                                && (gs.getTime() < maxGameLength));
                        
                        if (w!=null) w.dispose();
                        
                        if (traceOutputfolder != null) {
                            File folder = new File(traceOutputfolder);
                            if (!folder.exists()) folder.mkdirs();
                            te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
                            trace.addEntry(te);
                            XMLWriter xml;
                            ZipOutputStream zip = null;
                            String filename = ai1_idx + "-vs-" + ai2_idx + "-" + map_idx + "-" + iteration;
                            filename = filename.replace("/", "");
                            filename = filename.replace(")", "");
                            filename = filename.replace("(", "");
                            filename = traceOutputfolder + "/" + filename;
                            zip = new ZipOutputStream(new FileOutputStream(filename + ".zip"));
                            zip.putNextEntry(new ZipEntry("game.xml"));
                            xml = new XMLWriter(new OutputStreamWriter(zip));
                            trace.toxml(xml);
                            xml.flush();
                            zip.closeEntry();
                            zip.close();
                        }

                        int winner = -1;
                        if (crashed != -1) {
                            winner = 1 - crashed;
                            if (crashed == 0) {
                                AIcrashes[ai1_idx][ai2_idx]++;
                            }
                            if (crashed == 1) {
                                AIcrashes[ai2_idx][ai1_idx]++;
                            }
                        } else if (timedout != -1) {
                            winner = 1 - timedout;
                            if (timedout == 0) {
                                AItimeout[ai1_idx][ai2_idx]++;
                            }
                            if (timedout == 1) {
                                AItimeout[ai2_idx][ai1_idx]++;
                            }
                        } else {
                            winner = gs.winner();
                        }
                        ai1.gameOver(winner);
                        ai2.gameOver(winner);
                        
                        out.write(iteration + "\t" + map_idx + "\t" + ai1_idx + "\t" + ai2_idx + "\t"
                                + gs.getTime() + "\t" + winner + "\t" + crashed + "\t" + timedout + "\n");
                        out.flush();
                        if (progress != null) {
                            progress.write("Winner: " + winner + "  in " + gs.getTime() + " cycles\n");
                            progress.write(ai1 + " : " + ai1.statisticsString() + "\n");
                            progress.write(ai2 + " : " + ai2.statisticsString() + "\n");
                            progress.write("AI1 time usage, average:  " + (averageTime1/numTimes1) + 
                                           ", # times over budget: " + numberOfTimeOverBudget1 + " (avg " + (averageTimeOverBudget1/numberOfTimeOverBudget1) + 
                                           ") , # times over 2*budget: " + numberOfTimeOverTwiceBudget1 + " (avg " + (averageTimeOverTwiceBudget1/numberOfTimeOverTwiceBudget1) + ")\n");
                            progress.write("AI2 time usage, average:  " + (averageTime2/numTimes2) + 
                                           ", # times over budget: " + numberOfTimeOverBudget2 + " (avg " + (averageTimeOverBudget2/numberOfTimeOverBudget2) + 
                                           ") , # times over 2*budget: " + numberOfTimeOverTwiceBudget2 + " (avg " + (averageTimeOverTwiceBudget2/numberOfTimeOverTwiceBudget2) + ")\n");
                        }
                        progress.flush();
                        if (winner == -1) {
                            ties[ai1_idx][ai2_idx]++;
                            ties[ai2_idx][ai1_idx]++;
                        } else if (winner == 0) {
                            wins[ai1_idx][ai2_idx]++;
                        } else if (winner == 1) {
                            wins[ai2_idx][ai1_idx]++;
                        }
                        accumTime[ai1_idx][ai2_idx] += gs.getTime();
                    }
                }
            }
        }

        out.write("Wins:\n");
        for (int ai1_idx = 0; ai1_idx < AIs.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < AIs.size(); ai2_idx++) {
                out.write(wins[ai1_idx][ai2_idx] + "\t");
            }
            out.write("\n");
        }
        out.write("Ties:\n");
        for (int ai1_idx = 0; ai1_idx < AIs.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < AIs.size(); ai2_idx++) {
                out.write(ties[ai1_idx][ai2_idx] + "\t");
            }
            out.write("\n");
        }
        out.write("Average Game Length:\n");
        for (int ai1_idx = 0; ai1_idx < AIs.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < AIs.size(); ai2_idx++) {
                out.write(accumTime[ai1_idx][ai2_idx] / (maps.size() * iterations) + "\t");
            }
            out.write("\n");
        }
        out.write("AI crashes:\n");
        for (int ai1_idx = 0; ai1_idx < AIs.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < AIs.size(); ai2_idx++) {
                out.write(AIcrashes[ai1_idx][ai2_idx] + "\t");
            }
            out.write("\n");
        }
        out.write("AI timeout:\n");
        for (int ai1_idx = 0; ai1_idx < AIs.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < AIs.size(); ai2_idx++) {
                out.write(AItimeout[ai1_idx][ai2_idx] + "\t");
            }
            out.write("\n");
        }
        out.flush();
        if (progress != null) {
            progress.write("RoundRobinTournament: tournament ended\n");
        }
        progress.flush();
    }
}
