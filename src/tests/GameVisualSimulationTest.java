 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.core.AI;
import ai.*;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.BFSPathFinding;
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
public class GameVisualSimulationTest {
    public static void main(String[] args) throws Exception {
        UnitTypeTable utt = new UnitTypeTable();
        AI ai1 = new WorkerRush(utt, new BFSPathFinding());        
        AI ai2 = new RandomBiasedAI();

        Game game = new Game( utt, "maps/16x16/basesWorkers16x16.xml", false, false, 5000, 20, ai1, ai2);
        game.start();
        
        System.out.println("Game Over");
    }    
}
