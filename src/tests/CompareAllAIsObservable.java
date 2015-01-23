/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.portfolio.ContinuingPortfolioAI;
import ai.*;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.abstraction.pathfinding.GreedyPathFinding;
import ai.evaluation.SimpleEvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction2;
import ai.mcts.naivemcts.ContinuingNaiveMCTS;
import ai.mcts.uct.ContinuingDownsamplingUCT;
import ai.mcts.uct.ContinuingUCT;
import ai.mcts.uct.ContinuingUCTUnitActions;
import ai.minimax.ABCD.IDContinuingABCD;
import ai.minimax.RMMiniMax.IDContinuingRTMinimax;
import ai.minimax.RMMiniMax.IDContinuingRTMinimaxRandomized;
import ai.montecarlo.*;
import java.io.File;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class CompareAllAIsObservable {
    
    public static void main(String args[]) throws Exception 
    {
        int TIME = 100;
        int MAX_ACTIONS = 100;
        int MAX_PLAYOUTS = -1;
        int PLAYOUT_TIME = 100;
        int MAX_DEPTH = 10;
        int RANDOMIZED_AB_REPEATS = 10;
        
        List<AI> bots = new LinkedList<AI>();

        bots.add(new RandomAI());
        bots.add(new RandomBiasedAI());
        bots.add(new LightRush(UnitTypeTable.utt, new BFSPathFinding()));
        bots.add(new RangedRush(UnitTypeTable.utt, new BFSPathFinding()));
        bots.add(new WorkerRush(UnitTypeTable.utt, new BFSPathFinding()));
        bots.add(new ContinuingPortfolioAI(new AI[]{new WorkerRush(UnitTypeTable.utt, new BFSPathFinding()),
                                          new LightRush(UnitTypeTable.utt, new BFSPathFinding()),
                                          new RangedRush(UnitTypeTable.utt, new BFSPathFinding()),
                                          new RandomBiasedAI()}, 
                                 new boolean[]{true,true,true,false}, 
                                 TIME, MAX_PLAYOUTS, PLAYOUT_TIME*4, new SimpleSqrtEvaluationFunction2()));
        
        bots.add(new IDContinuingRTMinimax(TIME, new SimpleEvaluationFunction()));
        bots.add(new IDContinuingRTMinimaxRandomized(TIME, RANDOMIZED_AB_REPEATS, new SimpleEvaluationFunction()));
        bots.add(new IDContinuingABCD(TIME, MAX_PLAYOUTS, new LightRush(UnitTypeTable.utt, new GreedyPathFinding()), PLAYOUT_TIME, new SimpleSqrtEvaluationFunction2(), false));

        bots.add(new ContinuingMC(TIME, PLAYOUT_TIME, MAX_PLAYOUTS, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction2()));
        bots.add(new ContinuingDownsamplingMC(TIME, PLAYOUT_TIME, MAX_ACTIONS, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction2()));
        bots.add(new ContinuingNaiveMC(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, 0.33f, 0.25f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction2()));
        bots.add(new ContinuingNaiveMC(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, 1.00f, 0.25f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction2()));

        bots.add(new ContinuingUCT(TIME, PLAYOUT_TIME, MAX_DEPTH, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction2()));
        bots.add(new ContinuingDownsamplingUCT(TIME, PLAYOUT_TIME, MAX_ACTIONS, MAX_DEPTH, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction2()));
        bots.add(new ContinuingUCTUnitActions(TIME, PLAYOUT_TIME, MAX_DEPTH*10, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction2()));
        bots.add(new ContinuingNaiveMCTS(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_DEPTH, 0.33f, 0.0f, 0.75f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction2()));
        bots.add(new ContinuingNaiveMCTS(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_DEPTH, 1.00f, 0.0f, 0.25f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction2()));

        PrintStream out = new PrintStream(new File("results.txt"));
        
        // Separate the matchs by map:
        List<PhysicalGameState> maps = new LinkedList<PhysicalGameState>();        

        maps.clear();
        maps.add(PhysicalGameState.load("maps/basesWorkers8x8.xml",UnitTypeTable.utt));
//        Experimenter.runExperimentsPartiallyObservable(bots, maps, 10, 3000, 300, true, out);
        Experimenter.runExperiments(bots, maps, 10, 3000, 300, true, out);
      
        maps.clear();
        maps.add(PhysicalGameState.load("maps/melee12x12mixed12.xml",UnitTypeTable.utt));
        Experimenter.runExperiments(bots, maps, 10, 3000, 300, true, out);

        maps.clear();
        maps.add(PhysicalGameState.load("maps/melee8x8mixed6.xml",UnitTypeTable.utt));
        Experimenter.runExperiments(bots, maps, 10, 3000, 300, true, out);

        maps.clear();
        maps.add(PhysicalGameState.load("maps/melee4x4light2.xml",UnitTypeTable.utt));
        Experimenter.runExperiments(bots, maps, 10, 3000, 300, true, out);
    }
}
