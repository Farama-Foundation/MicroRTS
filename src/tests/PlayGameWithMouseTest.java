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
        PhysicalGameState pgs = PhysicalGameState.load("maps/basesWorkers16x16.xml", UnitTypeTable.utt);
//        PhysicalGameState pgs = PhysicalGameState.load("maps/basesWorkers8x8.xml", UnitTypeTable.utt);

        GameState gs = new GameState(pgs, UnitTypeTable.utt);
        int MAXCYCLES = 10000;
        int PERIOD = 100;
        boolean gameover = false;
                
        PhysicalGameStatePanel pgsp = new PhysicalGameStatePanel(gs);
        PhysicalGameStateMouseJFrame w = new PhysicalGameStateMouseJFrame("Game State Visuakizer (Mouse)",640,640,pgsp);
//        PhysicalGameStateMouseJFrame w = new PhysicalGameStateMouseJFrame("Game State Visuakizer (Mouse)",400,400,pgsp);

        AI ai1 = new MouseController(w);
//        AI ai2 = new PassiveAI();
//        AI ai2 = new RandomBiasedAI();
//        AI ai2 = new LightRush(UnitTypeTable.utt, new AStarPathFinding());
        AI ai2 = new ContinuingAI(new NaiveMCTS(PERIOD, -1, 100, 20, 0.33f, 0.0f, 0.75f, new RandomBiasedAI(), new SimpleEvaluationFunction()));

        long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
        do{
            if (System.currentTimeMillis()>=nextTimeToUpdate) {
                PlayerAction pa1 = ai1.getAction(0, gs);
                PlayerAction pa2 = ai2.getAction(1, gs);
                gs.issueSafe(pa1);
                gs.issueSafe(pa2);

                // simulate:
                gameover = gs.cycle();
                w.repaint();
                nextTimeToUpdate+=PERIOD;
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }while(!gameover && gs.getTime()<MAXCYCLES);
        
        System.out.println("Game Over");
    }    
}
