 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.sockets;

import ai.core.AI;
import ai.*;
import ai.socket.SocketAI;
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
 * 
 * Once you have the server running (for example, run "RunServerExample.java"),
 * set the proper IP and port in the variable below, and run this file.
 * One of the AIs (ai1) is run remotely using the server.
 * 
 * Notice that as many AIs as needed can connect to the same server. For
 * example, uncomment line 44 below and comment 45, to see two AIs using the same server.
 * 
 */
public class RunClientExample {
    public static void main(String args[]) throws Exception {
        UnitTypeTable utt = new UnitTypeTable();
        AI ai1 = new SocketAI(100,0, "127.0.0.1", 9898, SocketAI.LANGUAGE_JSON, utt);
        AI ai2 = new RandomBiasedAI();

        Game game = new Game( utt, "maps/16x16/basesWorkers16x16.xml", false, false, 5000, 20, ai1, ai2);
        game.start();
    }    
}
