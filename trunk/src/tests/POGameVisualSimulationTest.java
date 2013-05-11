 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.*;
import ai.abstraction.LightRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import gui.PhysicalGameStatePanel;
import java.io.OutputStreamWriter;
import javax.swing.JFrame;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class POGameVisualSimulationTest {
    public static void main(String args[]) throws Exception {
        PhysicalGameState pgs = PhysicalGameState.load("maps/basesWorkers16x16.xml", UnitTypeTable.utt);
//        PhysicalGameState pgs = PhysicalGameState.load("maps/steven/RandomBiasedAIMediumMap.xml", UnitTypeTable.utt);
//        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

        GameState gs = new GameState(pgs, UnitTypeTable.utt);
        int MAXCYCLES = 5000;
        int PERIOD = 20;
        boolean gameover = false;
        
//        AI ai1 = new RandomAI();
//        AI ai1 = new WorkerRush(UnitTypeTable.utt, new AStarPathFinding());
        AI ai1 = new LightRush(UnitTypeTable.utt, new AStarPathFinding());
//        AI ai1 = new RangedRush(UnitTypeTable.utt, new GreedyPathFinding());
//        AI ai1 = new ContinuingNaiveMC(PERIOD, 200, 0.33f, 0.2f, new RandomBiasedAI(), new SimpleEvaluationFunction());

        AI ai2 = new RandomBiasedAI();
//        AI ai2 = new LightRush();
        
        XMLWriter xml = new XMLWriter(new OutputStreamWriter(System.out));
        pgs.toxml(xml);
        xml.flush();

        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640, true);

        long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
        do{
            if (System.currentTimeMillis()>=nextTimeToUpdate) {
                PlayerAction pa1 = ai1.getAction(0, new PartiallyObservableGameState(gs,0));
                PlayerAction pa2 = ai2.getAction(1, new PartiallyObservableGameState(gs,1));
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
