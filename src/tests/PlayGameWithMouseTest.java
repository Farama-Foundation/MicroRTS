 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.core.AI;
import ai.*;
import ai.core.ContinuingAI;
import ai.evaluation.SimpleEvaluationFunction;
import ai.mcts.naivemcts.NaiveMCTS;
import gui.MouseController;
import gui.PhysicalGameStateMouseJFrame;
import gui.PhysicalGameStatePanel;
import rts.Game;
import rts.GameState;
import rts.MouseGame;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class PlayGameWithMouseTest {
    public static void main(String args[]) throws Exception {
        int PERIOD = 100;

        AI opponent = new ContinuingAI(new NaiveMCTS(PERIOD, -1, 100, 20, 0.33f, 0.0f, 0.75f, new RandomBiasedAI(), new SimpleEvaluationFunction(), true));

        Game game = new MouseGame( new UnitTypeTable(), "maps/16x16/basesWorkers16x16.xml", false, false, 5000, PERIOD, opponent);
        game.start();
        
        System.out.println("Game Over");
    }    
}
