/*
* This class was contributed by: Antonin Komenda, Alexander Shleyfman and Carmel Domshlak
*/

package tests;

import ai.AI;
import ai.RandomBiasedAI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleEvaluationFunction;
import ai.montecarlo.ContinuingNaiveMC;
import ai.montecarlo.lsi.PseudoContinuingLSI;
import ai.montecarlo.lsi.Sampling.AgentOrderingType;
import gui.PhysicalGameStateJFrame;
import gui.PhysicalGameStatePanel;
import java.io.OutputStreamWriter;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import util.XMLWriter;

 public class LMCTest {
    public static void main(String args[]) throws Exception {
        String scenarioFileName = "maps/basesWorkers8x8.xml";

        UnitTypeTable utt = UnitTypeTable.utt; // original game (NOOP length = move length)

        int MAX_GAME_CYCLES = 3000; // game time
        int SIMULATION_BUDGET = 1000; // count per decision
        int LOOKAHEAD_CYCLES = 100; // game time
        AI simulationAi = new RandomBiasedAI();
        EvaluationFunction evalFunction = new SimpleEvaluationFunction();

//        AI ai1 = new RandomAI();
//        AI ai1 = new RandomBiasedAI();
//        AI ai1 = new WorkerRush(UnitTypeTable.utt, new AStarPathFinding());
//        AI ai1 = new LightRush(UnitTypeTable.utt, new AStarPathFinding());
//        AI ai1 = new RangedRush(UnitTypeTable.utt, new GreedyPathFinding());
//        AI ai1 = new NaiveMonteCarlo(SIMULATION_COUNT, LOOKAHEAD_TIME, 0.33f, 0.2f, simulationAi, evalFunction);
//        AI ai1 = new LinearMonteCarlo(SIMULATION_COUNT, LOOKAHEAD_TIME, simulationAi, evalFunction);
//        AI ai1 = new LocalLinearMonteCarlo(SIMULATION_COUNT, LOOKAHEAD_TIME, simulationAi, evalFunction);
        AI ai1 = new PseudoContinuingLSI(SIMULATION_BUDGET, LOOKAHEAD_CYCLES,
                0.25, PseudoContinuingLSI.EstimateType.RANDOM_TAIL, PseudoContinuingLSI.EstimateReuseType.ALL,
                PseudoContinuingLSI.GenerateType.PER_AGENT, AgentOrderingType.ENTROPY,
                PseudoContinuingLSI.EvaluateType.HALVING, false,
                PseudoContinuingLSI.RelaxationType.NONE, 2,
                false,
                simulationAi, evalFunction);

//        HumanAI ai2 = new HumanAI();
//        AI ai2 = new RandomBiasedAI();
//        AI ai2 = new WorkerRush(utt, new AStarPathFinding());
//        AI ai2 = new LightRush(UnitTypeTable.utt, new AStarPathFinding());
//        AI ai2 = new RangedRush(UnitTypeTable.utt, new AStarPathFinding());
        AI ai2 = new ContinuingNaiveMC(-1, SIMULATION_BUDGET, LOOKAHEAD_CYCLES, 0.33f, 0.25f, simulationAi, evalFunction);
//        AI ai2 = new GenerateEvaluateMonteCarlo(SIMULATION_BUDGET, LOOKAHEAD_CYCLES,
//                0.25, EstimateType.RANDOM_TAIL, EstimateReuseType.ALL,
//                GenerateType.PER_AGENT, AgentOrderingType.ENTROPY,
//                EvaluateType.HALVING, true,
//                RelaxationType.NONE, 2,
//                false,
//                simulationAi, evalFunction);

        PhysicalGameState pgs = PhysicalGameState.load(scenarioFileName, utt);
        GameState gs = new GameState(pgs, utt);

        XMLWriter xml = new XMLWriter(new OutputStreamWriter(System.out));
        pgs.toxml(xml);
        xml.flush();

        PhysicalGameStateJFrame w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, evalFunction);
        w.repaint();

        // for HumanAI
        //ai2.registerUI(w, 1);

        boolean gameover = false;
        do {
            long timeMillis = System.currentTimeMillis();

            PlayerAction pa1 = ai1.getAction(0, gs);
            PlayerAction pa2 = ai2.getAction(1, gs);
            gs.issueSafe(pa1);
            gs.issueSafe(pa2);

            timeMillis = System.currentTimeMillis() - timeMillis;

            if (timeMillis < 50) {
                Thread.sleep(50 - timeMillis);
            }

            // simulate:
            gameover = gs.cycle();
            w.repaint();
        } while(!gameover && gs.getTime() < MAX_GAME_CYCLES);

        System.out.println("Game Over");

        String rec = "";
        rec += scenarioFileName + "\t";
        rec += MAX_GAME_CYCLES + "\t";
        rec += SIMULATION_BUDGET + "\t";
        rec += LOOKAHEAD_CYCLES + "\t";
        rec += simulationAi.getClass().getSimpleName() + "\t";
        rec += evalFunction.getClass().getSimpleName() + "\t";
        rec += ai1.getClass().getSimpleName() + "\t";
        rec += ai2.getClass().getSimpleName() + "\t";
        rec += gs.winner() + "\t";
        rec += evalFunction.evaluate(0, 1, gs) + "\t";
        rec += ai1.statisticsString() + "\t";
        rec += ai2.statisticsString() + "\t";
        System.out.println(rec);
    }

}
