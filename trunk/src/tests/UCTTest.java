/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.AI;
import ai.RandomBiasedAI;
import ai.abstraction.LightRush;
import ai.abstraction.pathfinding.GreedyPathFinding;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.EvaluationFunctionWithActions;
import ai.evaluation.SimpleEvaluationFunction;
import ai.minimax.ABCD;
import ai.minimax.IDABCD;
import ai.minimax.IDContinuingABCD;
import ai.montecarlo.ContinuingNaiveMC;
import ai.minimax.IDContinuingRTMinimaxRandomized;
import ai.uct.ContinuingUCT;
import gui.PhysicalGameStatePanel;
import java.io.OutputStreamWriter;
import javax.swing.JFrame;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class UCTTest {
 public static void main(String args[]) throws Exception {
//        PhysicalGameState pgs = PhysicalGameState.load("maps/basesWorkers8x8.xml",UnitTypeTable.utt);
        PhysicalGameState pgs = MapGenerator.melee4x4light2();

        GameState gs = new GameState(pgs, UnitTypeTable.utt);
        int MAXCYCLES = 5000;
        int PERIOD = 100;
        boolean gameover = false;
        
        AI ai1 = new ContinuingUCT(PERIOD, 100, 10, new RandomBiasedAI(), new SimpleEvaluationFunction());
        AI ai2 = new RandomBiasedAI();
        
        XMLWriter xml = new XMLWriter(new OutputStreamWriter(System.out));
        pgs.toxml(xml);
        xml.flush();

        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640);

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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }while(!gameover && gs.getTime()<MAXCYCLES);
        
        System.out.println("Game Over! Winner " + gs.winner());
    }      
}
