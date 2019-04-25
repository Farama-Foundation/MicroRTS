 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.sockets;

import ai.core.AI;
import ai.*;
import ai.socket.SocketRewardAI;
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
public class RunClientLayers {
    public static void main(String args[]) throws Exception {
        String serverIP = "127.0.0.1";
        int serverPort = 9898;

        UnitTypeTable utt = new UnitTypeTable();

        int PERIOD = 20;
        boolean gameover = false;
        boolean layerJSON = true;
        

        SocketRewardAI srai = new SocketRewardAI(100,0, serverIP, serverPort, SocketRewardAI.LANGUAGE_JSON, utt, layerJSON);
        AI rbai = new RandomBiasedAI();

        System.out.println("Socket client started");
        while (true) {
            srai.reset();
            rbai.reset();
            // maybe there is a way to not have to restart the panel.
            PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/basesWorkers16x16.xml", utt);
            GameState gs = new GameState(pgs, utt);
            JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);

            long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
            while (true) {
                if (System.currentTimeMillis()>=nextTimeToUpdate) {
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
                    w.repaint();
                    nextTimeToUpdate+=PERIOD;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
