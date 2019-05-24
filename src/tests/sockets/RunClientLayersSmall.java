/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package tests.sockets;

import ai.core.AI;
import ai.*;
import ai.socket.SocketRewardAI;
import gui.PhysicalGameStateJFrame;
import gui.PhysicalGameStatePanel;
import javax.swing.JFrame;
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
public class RunClientLayersSmall {
    public static void main(String args[]) throws Exception {
        String serverIP = "127.0.0.1";
        int serverPort = 9898;

        UnitTypeTable utt = new UnitTypeTable();

        boolean gameover = false;
        boolean layerJSON = true;

        SocketRewardAI srai = new SocketRewardAI(100,0, serverIP, serverPort, SocketRewardAI.LANGUAGE_JSON, utt, layerJSON);
        AI rbai = new PassiveAI();

        System.out.println("Socket client started");

        PhysicalGameState pgs = PhysicalGameState.load("maps/4x4/base4x4.xml", utt);
        GameState gs = new GameState(pgs, utt);
        PhysicalGameStateJFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);
        while (true) {
            srai.reset();
            rbai.reset();
            // maybe there is a way to not have to restart the panel.
            pgs = PhysicalGameState.load("maps/4x4/base4x4.xml", utt);
            gs = new GameState(pgs, utt);
            // w.setStateDirect(gs);

            while (true) {
                w.setStateCloning(gs);
                w.repaint();
                PlayerAction pa1 = srai.getAction(0, gs);
                if (srai.done) {
                    break;
                }
                PlayerAction pa2 = rbai.getAction(1, gs);
                gs.issueSafe(pa1);
                gs.issueSafe(pa2);
                srai.computeReward(0, 1, gs);

                // simulate:
                gameover = gs.cycle();
                if (gameover) {
                    break;
                }
                try {
                    Thread.yield();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            srai.gameOver(gs.winner(), gs);
            rbai.gameOver(gs.winner());
            if (srai.finished) {
                break;
            }
        }
    }    
}
