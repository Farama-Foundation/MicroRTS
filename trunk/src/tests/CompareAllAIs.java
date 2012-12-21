/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.*;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.GreedyPathFinding;
import ai.evaluation.EvaluationFunctionWithActions;
import ai.evaluation.SimpleEvaluationFunction;
import ai.minimax.IDContinuingABCD;
import ai.montecarlo.*;
import ai.naivemcts.ContinuingNaiveMCTS;
import ai.minimax.IDContinuingRTMinimax;
import ai.minimax.IDContinuingRTMinimax;
import ai.minimax.IDContinuingRTMinimaxRandomized;
import ai.uct.ContinuingDownsamplingUCT;
import ai.uct.ContinuingUCT;
import ai.uct.ContinuingUCTUnitActions;
import ai.uct.UCT;
import gui.PhysicalGameStatePanel;
import java.util.LinkedList;
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
public class CompareAllAIs {

    public static void main(String args[]) throws Exception 
    {
        int TIME = 100;
        int MAX_ACTIONS = 100;
        int PLAYOUT_TIME = 100;
        int MAX_DEPTH = 10;
        int RANDOMIZED_AB_REPEATS = 10;
        
        List<AI> bots = new LinkedList<AI>();

        bots.add(new RandomAI());
        bots.add(new RandomBiasedAI());
        bots.add(new LightRush(UnitTypeTable.utt, new AStarPathFinding()));
        bots.add(new RangedRush(UnitTypeTable.utt, new AStarPathFinding()));
        bots.add(new WorkerRush(UnitTypeTable.utt, new AStarPathFinding()));
      
        bots.add(new IDContinuingRTMinimax(TIME, new SimpleEvaluationFunction()));
        bots.add(new IDContinuingRTMinimaxRandomized(TIME, RANDOMIZED_AB_REPEATS, new SimpleEvaluationFunction()));
//        bots.add(new IDContinuingABCD(TIME, new RandomBiasedAI(), PLAYOUT_TIME, new SimpleEvaluationFunction()));
        bots.add(new IDContinuingABCD(TIME, new LightRush(UnitTypeTable.utt, new GreedyPathFinding()), PLAYOUT_TIME, new SimpleEvaluationFunction()));

        bots.add(new ContinuingMC(TIME, PLAYOUT_TIME, new RandomBiasedAI(), new SimpleEvaluationFunction()));
        bots.add(new ContinuingDownsamplingMC(TIME, PLAYOUT_TIME, MAX_ACTIONS, new RandomBiasedAI(), new SimpleEvaluationFunction()));
        bots.add(new ContinuingNaiveMC(TIME, PLAYOUT_TIME, 0.25f, 0.25f, new RandomBiasedAI(), new SimpleEvaluationFunction()));

        bots.add(new ContinuingUCT(TIME, PLAYOUT_TIME, MAX_DEPTH, new RandomBiasedAI(), new SimpleEvaluationFunction()));
        bots.add(new ContinuingDownsamplingUCT(TIME, PLAYOUT_TIME, MAX_ACTIONS, MAX_DEPTH, new RandomBiasedAI(), new SimpleEvaluationFunction()));
        bots.add(new ContinuingUCTUnitActions(TIME, PLAYOUT_TIME, MAX_DEPTH, new RandomBiasedAI(), new SimpleEvaluationFunction()));

        //        bots.add(new ContinuingNaiveMCTS(TIME, PLAYOUT_TIME, MAX_DEPTH, 0.25f, 0.9f, new RandomBiasedAI(), new SimpleEvaluationFunction()));
        
        // Separate the matchs by map:
        List<PhysicalGameState> maps = new LinkedList<PhysicalGameState>();
        maps.add(PhysicalGameState.load("maps/melee4x4light2.xml",UnitTypeTable.utt));
//        Experimenter.runExperiments(bots, maps, 10, 3000, 300, true);
        Experimenter.runExperiments(bots, maps, 10, 3000, 300, true, 7);

        maps.clear();
        maps.add(PhysicalGameState.load("maps/melee8x8mixed6.xml",UnitTypeTable.utt));
//        Experimenter.runExperiments(bots, maps, 10, 3000, 300, true);
        Experimenter.runExperiments(bots, maps, 10, 3000, 300, true, 7);

        maps.clear();
        maps.add(PhysicalGameState.load("maps/basesWorkers8x8.xml",UnitTypeTable.utt));
//        Experimenter.runExperiments(bots, maps, 10, 3000, 300, true);
        Experimenter.runExperiments(bots, maps, 10, 3000, 300, true, 7);
      
    }
}
