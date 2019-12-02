package tournaments;

import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ContinuingAI;
import ai.core.InterruptibleAI;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.Trace;
import rts.TraceEntry;
import rts.units.UnitTypeTable;
import util.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author douglasrizzo
 */
class Tournament {

    private static int TIMEOUT_CHECK_TOLERANCE = 20;
    private static boolean USE_CONTINUING_ON_INTERRUPTIBLE = true;

    List<AI> AIs;
    List<AI> opponentAIs;
    private int[][] wins;
    private int[][] ties;
    private int[][] AIcrashes;
    private int[][] opponentAIcrashes;
    private int[][] AItimeout;
    private int[][] opponentAItimeout;
    private double[][] accumTime;

    Tournament(List<AI> AIs, List<AI> opponentAIs){
        this.AIs = AIs;
        this.opponentAIs = opponentAIs;
        wins = new int[AIs.size()][opponentAIs.size()];
        ties = new int[AIs.size()][opponentAIs.size()];
        AIcrashes = new int[AIs.size()][opponentAIs.size()];
        opponentAIcrashes = new int[AIs.size()][opponentAIs.size()];
        AItimeout = new int[AIs.size()][opponentAIs.size()];
        opponentAItimeout = new int[AIs.size()][opponentAIs.size()];
        accumTime = new double[AIs.size()][opponentAIs.size()];
    }

    Tournament(List<AI> AIs){
     this(AIs, AIs);
    }

    void playSingleGame(int maxGameLength, int timeBudget,
                               int iterationsBudget, long preAnalysisBudgetFirstTimeInAMap,
                               long preAnalysisBudgetRestOfTimes, boolean fullObservability,
                               boolean timeoutCheck, boolean runGC, boolean preAnalysis,
                               UnitTypeTable utt, String traceOutputfolder, Writer out,
                               Writer progress,
                               String[] readWriteFolders,
                               boolean[][] firstPreAnalysis, int iteration, int map_idx, PhysicalGameState pgs,
                               int ai1_idx, int ai2_idx) throws Exception {
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

        AI ai1 = this.AIs.get(ai1_idx).clone();
        AI ai2 = this.opponentAIs.get(ai2_idx).clone();

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

        if (progress != null) progress.write("MATCH UP: " + ai1 + " vs " + ai2 + "\n");

        if (preAnalysis && firstPreAnalysis != null) {
            preAnalysisSingleAI(preAnalysisBudgetFirstTimeInAMap, preAnalysisBudgetRestOfTimes, progress, readWriteFolders[ai1_idx], firstPreAnalysis[ai1_idx], map_idx, ai1, gs);
            preAnalysisSingleAI(preAnalysisBudgetFirstTimeInAMap, preAnalysisBudgetRestOfTimes, progress, readWriteFolders[ai2_idx], firstPreAnalysis[ai2_idx], map_idx, ai2, gs);
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
            if (runGC) System.gc();
            try {
                AI1start = System.currentTimeMillis();
                pa1 = ai1.getAction(0, fullObservability ? gs : new PartiallyObservableGameState(gs, 0));
                AI1end = System.currentTimeMillis();
            } catch (Exception e) {
                crashed = 0;
                break;
            }
            if (runGC) System.gc();
            try {
                AI2start = System.currentTimeMillis();
                pa2 = ai2.getAction(1, fullObservability ? gs : new PartiallyObservableGameState(gs, 1));
                AI2end = System.currentTimeMillis();
            } catch (Exception e) {
                crashed = 1;
                break;
            }

            {
                long AI1time = AI1end - AI1start;
                long AI2time = AI2end - AI2start;
                numTimes1++;
                numTimes2++;
                averageTime1 += AI1time;
                averageTime2 += AI2time;
                if (AI1time > timeBudget) {
                    numberOfTimeOverBudget1++;
                    averageTimeOverBudget1 += AI1time;
                    if (AI1time > timeBudget * 2) {
                        numberOfTimeOverTwiceBudget1++;
                        averageTimeOverTwiceBudget1 += AI1time;
                    }
                }
                if (AI2time > timeBudget) {
                    numberOfTimeOverBudget2++;
                    averageTimeOverBudget2 += AI2time;
                    if (AI2time > timeBudget * 2) {
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
        } while (!gameover &&
                (gs.getTime() < maxGameLength));

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
                this.AIcrashes[ai1_idx][ai2_idx]++;
            }
            if (crashed == 1) {
                opponentAIcrashes[ai1_idx][ai2_idx]++;
            }
        } else if (timedout != -1) {
            winner = 1 - timedout;
            if (timedout == 0) {
                this.AItimeout[ai1_idx][ai2_idx]++;
            }
            if (timedout == 1) {
                if (crashed==1) opponentAItimeout[ai1_idx][ai2_idx]++;
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
            progress.write("AI1 time usage, average:  " + (averageTime1 / numTimes1) +
                    ", # times over budget: " + numberOfTimeOverBudget1 + " (avg " + (averageTimeOverBudget1 / numberOfTimeOverBudget1) +
                    ") , # times over 2*budget: " + numberOfTimeOverTwiceBudget1 + " (avg " + (averageTimeOverTwiceBudget1 / numberOfTimeOverTwiceBudget1) + ")\n");
            progress.write("AI2 time usage, average:  " + (averageTime2 / numTimes2) +
                    ", # times over budget: " + numberOfTimeOverBudget2 + " (avg " + (averageTimeOverBudget2 / numberOfTimeOverBudget2) +
                    ") , # times over 2*budget: " + numberOfTimeOverTwiceBudget2 + " (avg " + (averageTimeOverTwiceBudget2 / numberOfTimeOverTwiceBudget2) + ")\n");
        }
        progress.flush();

        if (winner == -1) {
            this.ties[ai1_idx][ai2_idx]++;
//                            ties[ai2_idx][ai1_idx]++;
        } else if (winner == 0) {
            this.wins[ai1_idx][ai2_idx]++;
        } else if (winner == 1) {
//                            wins[ai2_idx][ai1_idx]++;
        }
        accumTime[ai1_idx][ai2_idx] += gs.getTime();
    }

    private static void preAnalysisSingleAI(long preAnalysisBudgetFirstTimeInAMap, long preAnalysisBudgetRestOfTimes, Writer progress, String readWriteFolder, boolean[] firstPreAnalysis, int map_idx, AI ai1, GameState gs) throws Exception {
        long preTime1 = preAnalysisBudgetRestOfTimes;
        if (firstPreAnalysis[map_idx]) {
            preTime1 = preAnalysisBudgetFirstTimeInAMap;
            firstPreAnalysis[map_idx] = false;
        }
        long pre_start1 = System.currentTimeMillis();
        ai1.preGameAnalysis(gs, preAnalysisBudgetRestOfTimes, readWriteFolder);
        long pre_end1 = System.currentTimeMillis();
        if (progress != null) {
            progress.write("preGameAnalysis player 1 took " + (pre_end1 - pre_start1) + "\n");
            if ((pre_end1 - pre_start1) > preTime1) progress.write("TIMEOUT PLAYER 1!\n");
        }
    }

    void printEndSummary(List<String> maps, int iterations, Writer out, Writer progress) throws IOException {
        out.write("Wins:\n");
        for (int ai1_idx = 0; ai1_idx < AIs.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < opponentAIs.size(); ai2_idx++) {
                out.write(wins[ai1_idx][ai2_idx] + "\t");
            }
            out.write("\n");
        }
        out.write("Ties:\n");
        for (int ai1_idx = 0; ai1_idx < AIs.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < opponentAIs.size(); ai2_idx++) {
                out.write(ties[ai1_idx][ai2_idx] + "\t");
            }
            out.write("\n");
        }
        out.write("Average Game Length:\n");
        for (int ai1_idx = 0; ai1_idx < AIs.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < opponentAIs.size(); ai2_idx++) {
                out.write(accumTime[ai1_idx][ai2_idx] / (maps.size() * iterations) + "\t");
            }
            out.write("\n");
        }
        out.write("AI crashes:\n");
        for (int ai1_idx = 0; ai1_idx < AIs.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < opponentAIs.size(); ai2_idx++) {
                out.write(AIcrashes[ai1_idx][ai2_idx] + "\t");
            }
            out.write("\n");
        }
        out.write("opponent AI crashes:\n");
        for (int ai1_idx = 0; ai1_idx < AIs.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < opponentAIs.size(); ai2_idx++) {
                out.write(opponentAIcrashes[ai1_idx][ai2_idx] + "\t");
            }
            out.write("\n");
        }
        out.write("AI timeout:\n");
        for (int ai1_idx = 0; ai1_idx < AIs.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < opponentAIs.size(); ai2_idx++) {
                out.write(AItimeout[ai1_idx][ai2_idx] + "\t");
            }
            out.write("\n");
        }
        out.write("opponent AI timeout:\n");
        for (int ai1_idx = 0; ai1_idx < AIs.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < opponentAIs.size(); ai2_idx++) {
                out.write(opponentAItimeout[ai1_idx][ai2_idx] + "\t");
            }
            out.write("\n");
        }
        out.flush();
        if (progress != null) progress.write(this.getClass().getName()+": tournament ended\n");
        progress.flush();
    }
}