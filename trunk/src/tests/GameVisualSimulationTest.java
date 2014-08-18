 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.*;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.abstraction.pathfinding.GreedyPathFinding;
import ai.evaluation.SimpleEvaluationFunction;
import ai.mcts.naivemcts.ContinuingNaiveMCTS;
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
public class GameVisualSimulationTest {
    public static void main(String args[]) throws Exception {
        PhysicalGameState pgs = PhysicalGameState.load("maps/basesWorkers16x16.xml", UnitTypeTable.utt);
//        PhysicalGameState pgs = PhysicalGameState.load("maps/steven/RandomBiasedAIMediumMap.xml", UnitTypeTable.utt);
//        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

        GameState gs = new GameState(pgs, UnitTypeTable.utt);
        int TIME = 100;
        int MAX_PLAYOUTS = 1000;
        int PLAYOUT_TIME = -1;
        int MAX_DEPTH = 10;
        int MAXCYCLES = 5000;
        int PERIOD = 20;
        boolean gameover = false;
        
//        AI ai1 = new RandomAI();
        AI ai1 = new WorkerRush(UnitTypeTable.utt, new BFSPathFinding());
//        AI ai1 = new LightRush(UnitTypeTable.utt, new AStarPathFinding());
//        AI ai1 = new RangedRush(UnitTypeTable.utt, new GreedyPathFinding());
//        AI ai1 = new ContinuingNaiveMC(PERIOD, 200, 0.33f, 0.2f, new RandomBiasedAI(), new SimpleEvaluationFunction());
/*
        AI ai1 = new ContinuingNaiveMCTS(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_DEPTH, 
                                         1.00f, 1.00f,
                                         0.00f, 1.00f,
                                         0.25f, 0.9975f,
                                         new RandomBiasedAI(), new SimpleEvaluationFunction());
*/
        
        AI ai2 = new RandomBiasedAI();
//        AI ai2 = new LightRush(UnitTypeTable.utt, new AStarPathFinding());
//        AI ai2 = new RangedRush(UnitTypeTable.utt, new AStarPathFinding());

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
        
        System.out.println("Game Over");
    }    
}
