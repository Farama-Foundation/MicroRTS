 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.socket;

import ai.core.AI;
import ai.*;
import gui.PhysicalGameStatePanel;
import javax.swing.JFrame;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class GameVisualSimulationWithSocketAI {
    public static void main(String args[]) throws Exception {
        UnitTypeTable utt = new UnitTypeTable();
        PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/basesWorkers16x16.xml", utt);
//        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

        GameState gs = new GameState(pgs, utt);
        int MAXCYCLES = 5000;
        int PERIOD = 20;
        boolean gameover = false;
        
//        AI ai1 = new WorkerRush(utt, new BFSPathFinding());        
        AI ai1 = new SocketAI(100,0, "127.0.0.1", 9898, SocketAI.LANGUAGE_XML, utt);
//        AI ai1 = new SocketAI(100,0, "127.0.0.1", 9898, SocketAI.LANGUAGE_JSON, utt);
        AI ai2 = new RandomBiasedAI();
        
        ai1.reset();
        ai2.reset();

        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);
//        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_WHITE);

        ai1.preGameAnalysis(gs, 1000, ".");
        ai2.preGameAnalysis(gs, 1000, ".");

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
