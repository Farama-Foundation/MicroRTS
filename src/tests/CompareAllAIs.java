/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.*;
import ai.abstraction.LightRush;
import ai.abstraction.WorkerRush;
import ai.montecarlo.*;
import ai.naivemcts.ContinuingNaiveMCTS;
import ai.rtminimax.IDContinuingRTMinimax;
import ai.rtminimax.IDContinuingRTMinimax;
import ai.naivemcts.NaiveMCTS;
import ai.rtminimax.IDContinuingRTMinimaxRandomized;
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
        List<AI> bots = new LinkedList<AI>();
        bots.add(new RandomBiasedAI());
        bots.add(new LightRush(UnitTypeTable.utt));
        bots.add(new WorkerRush(UnitTypeTable.utt));
        bots.add(new IDContinuingRTMinimax(100));
        bots.add(new IDContinuingRTMinimaxRandomized(100));
        bots.add(new ContinuingMC(100, 100, new RandomBiasedAI()));
        bots.add(new ContinuingDownsamplingMC(100, 100, 100, new RandomBiasedAI()));
        bots.add(new ContinuingNaiveMC(100, 100, 0.33f, 0.2f, new RandomBiasedAI()));
        bots.add(new ContinuingUCT(100, 100, new RandomBiasedAI()));
        bots.add(new ContinuingDownsamplingUCT(100, 100, 100, new RandomBiasedAI()));
        bots.add(new ContinuingUCTUnitActions(100, 100, new RandomBiasedAI()));
        bots.add(new ContinuingNaiveMCTS(100, 100, 0.33f, 0.2f, new RandomBiasedAI()));
        List<PhysicalGameState> maps = new LinkedList<PhysicalGameState>();

        maps.add(PhysicalGameState.load("maps/melee8x8light4.xml",UnitTypeTable.utt));
        maps.add(PhysicalGameState.load("maps/basesWorkers8x8.xml",UnitTypeTable.utt));
        maps.add(PhysicalGameState.load("maps/basesWorkers16x16.xml",UnitTypeTable.utt));
        
        Experimenter.runExperiments(bots, maps, 10, 3000, true);
    }
}
