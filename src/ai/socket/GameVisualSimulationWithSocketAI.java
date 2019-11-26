 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.socket;

import ai.core.AI;
import ai.*;
import gui.PhysicalGameStatePanel;
import javax.swing.JFrame;

import rts.Game;
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
        
        AI ai1 = new SocketAI(100,0, "127.0.0.1", 9898, SocketAI.LANGUAGE_XML, utt);
        AI ai2 = new RandomBiasedAI();

        Game game = new Game( utt, "maps/16x16/basesWorkers16x16.xml", false, false, 5000, 20, ai1, ai2);
        game.start();
        
        System.out.println("Game Over");
    }    
}
