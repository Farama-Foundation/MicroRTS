 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.*;
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

        GameState gs = new GameState(pgs, UnitTypeTable.utt);
        int MAXCYCLES = 5000;
        int PERIOD = 40;
        boolean gameover = false;
                
        PhysicalGameStatePanel pgsp = new PhysicalGameStatePanel(gs);
        PhysicalGameStateMouseJFrame w = new PhysicalGameStateMouseJFrame("Game State Visuakizer (Mouse)",640,640,pgsp);

        AI ai1 = new MouseController(w);
        AI ai2 = new RandomBiasedAI();
        
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
