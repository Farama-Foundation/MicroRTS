/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tournaments;

import ai.core.AI;

import java.io.File;
import java.io.Writer;
import java.util.List;

import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 * @author douglasrizzo
 */
public class FixedOpponentsTournament extends Tournament {

    public FixedOpponentsTournament(List<AI> AIs, List<AI> opponentAIs) {
        super(AIs, opponentAIs);
    }

    public void runTournament(List<String> maps,
                              int iterations,
                              int maxGameLength,
                              int timeBudget,
                              int iterationsBudget,
                              long preAnalysisBudgetFirstTimeInAMap,
                              long preAnalysisBudgetRestOfTimes,
                              boolean fullObservability,
                              boolean timeoutCheck,
                              boolean runGC,
                              boolean preAnalysis,
                              UnitTypeTable utt,
                              String traceOutputfolder,
                              Writer out,
                              Writer progress,
                              String folderForReadWriteFolders) throws Exception {
        if (progress != null) {
            progress.write(getClass().getName()+": Starting tournament\n");
        }
        out.write(getClass().getName()+"\n");
        out.write("AIs\n");
        for (AI ai : AIs) {
            out.write("\t" + ai.toString() + "\n");
        }
        out.write("opponent AIs\n");
        for (AI opponentAI : opponentAIs) {
            out.write("\t" + opponentAI.toString() + "\n");
        }
        out.write("maps\n");
        for (String map : maps) {
            out.write("\t" + map + "\n");
        }
        out.write("iterations\t" + iterations + "\n");
        out.write("maxGameLength\t" + maxGameLength + "\n");
        out.write("timeBudget\t" + timeBudget + "\n");
        out.write("iterationsBudget\t" + iterationsBudget + "\n");
        out.write("fullObservability\t" + fullObservability + "\n");
        out.write("timeoutCheck\t" + timeoutCheck + "\n");
        out.write("runGC\t" + runGC + "\n");
        out.write("iteration\tmap\tai1\tai2\ttime\twinner\tcrashed\ttimedout\n");
        out.flush();

        // create all the read/write folders:
        String readWriteFolders[] = new String[AIs.size()];
        for (int i = 0; i < AIs.size(); i++) {
            readWriteFolders[i] = folderForReadWriteFolders + "/AI" + i + "readWriteFolder";
            File f = new File(readWriteFolders[i]);
            f.mkdir();
        }

        boolean firstPreAnalysis[][] = new boolean[AIs.size()][maps.size()];
        for (int i = 0; i < AIs.size(); i++) {
            for (int j = 0; j < maps.size(); j++) {
                firstPreAnalysis[i][j] = true;
            }
        }

        String opponentReadWriteFolders[] = new String[opponentAIs.size()];
        for (int i = 0; i < opponentAIs.size(); i++) {
            opponentReadWriteFolders[i] = folderForReadWriteFolders + "/opponentAI" + i + "readWriteFolder";
            File f = new File(opponentReadWriteFolders[i]);
            f.mkdir();
        }

        boolean opponentFirstPreAnalysis[][] = new boolean[opponentAIs.size()][maps.size()];
        for (int i = 0; i < opponentAIs.size(); i++) {
            for (int j = 0; j < maps.size(); j++) {
                opponentFirstPreAnalysis[i][j] = true;
            }
        }

        for (int iteration = 0; iteration < iterations; iteration++) {
            for (int map_idx = 0; map_idx < maps.size(); map_idx++) {
                PhysicalGameState pgs = PhysicalGameState.load(maps.get(map_idx), utt);
                for (int ai1_idx = 0; ai1_idx < AIs.size(); ai1_idx++) {
                    for (int ai2_idx = 0; ai2_idx < opponentAIs.size(); ai2_idx++) {
                        playSingleGame(maxGameLength, timeBudget, iterationsBudget,
                                preAnalysisBudgetFirstTimeInAMap, preAnalysisBudgetRestOfTimes, fullObservability,
                                timeoutCheck, runGC, preAnalysis, utt, traceOutputfolder, out, progress, readWriteFolders, firstPreAnalysis,
                                iteration, map_idx, pgs, ai1_idx,
                                ai2_idx);
                    }
                }
            }
        }

        printEndSummary(maps, iterations, out, progress);
    }

}
