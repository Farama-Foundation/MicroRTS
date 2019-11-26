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
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class PlayGameWithMouseTest {
    public static void main(String args[]) throws Exception {
        UnitTypeTable utt = new UnitTypeTable();
        PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/basesWorkers16x16.xml", utt);

        GameState gs = new GameState(pgs, utt);
        int PERIOD = 100;
        PhysicalGameStatePanel pgsp = new PhysicalGameStatePanel(gs);
        PhysicalGameStateMouseJFrame w = new PhysicalGameStateMouseJFrame("Game State Visualizer (Mouse)",640,640, pgsp);

        AI ai1 = new MouseController(w);
        AI ai2 = new ContinuingAI(new NaiveMCTS(PERIOD, -1, 100, 20, 0.33f, 0.0f, 0.75f, new RandomBiasedAI(), new SimpleEvaluationFunction(), true));

        Game game = new Game( utt, "maps/16x16/basesWorkers16x16.xml", false, false, 5000, PERIOD, ai1, ai2);
        game.start(w);
        
        System.out.println("Game Over");
    }    
}
