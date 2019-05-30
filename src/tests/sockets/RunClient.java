/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package tests.sockets;

import ai.core.AI;
import ai.*;
import ai.socket.SocketAI;
import gui.PhysicalGameStatePanel;

import java.nio.file.Paths;

import javax.swing.JFrame;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import ai.socket.SocketRewardAI;
import gui.PhysicalGameStateJFrame;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 *
 * @author santi
 * 
 *         Once you have the server running (for example, run
 *         "RunServerExample.java"), set the proper IP and port in the variable
 *         below, and run this file. One of the AIs (ai1) is run remotely using
 *         the server.
 * 
 *         Notice that as many AIs as needed can connect to the same server. For
 *         example, uncomment line 44 below and comment 45, to see two AIs using
 *         the same server.
 * 
 */
public class RunClient {
    @Parameter(names = "--server-ip", description = "The microRTS server IP")
    String serverIP = "127.0.0.1";

    @Parameter(names = "--server-port", description = "The microRTS server port")
    int serverPort = 9898;

    @Parameter(names = "--map", description = "Which map in the `maps` folder are you using?")
    String map = "maps/4x4/base4x4.xml";

    @Parameter(names = "--ai-type", description = "The microRTS server IP")
    String aiType = "passive";

    @Parameter(names = "--render", description = "Whether to render the game")
    boolean render = false;

    @Parameter(names = "--microrts-path", description = "The path of microrts unzipped folder")
    String micrortsPath = "";

    PhysicalGameStateJFrame w;
    AI ai;

    public static void main(String args[]) throws Exception {
        RunClient rc = new RunClient();
        JCommander.newBuilder().addObject(rc).build().parse(args);
        rc.run();
    }

    public void run() throws Exception {

        UnitTypeTable utt = new UnitTypeTable();

        boolean gameover = false;
        boolean layerJSON = true;

        SocketRewardAI srai = new SocketRewardAI(100, 0, serverIP, serverPort, SocketRewardAI.LANGUAGE_JSON, utt,
                layerJSON);
        switch (aiType) {
            case "passive":
                ai = new PassiveAI();
                break;
            case "random-biased":
                ai = new RandomBiasedAI();
                break;
            default:
                throw new Exception("no AI was chosen");
        }

        System.out.println("Socket client started");

        if (micrortsPath.length() != 0) {
            map = Paths.get(micrortsPath, map).toString();
        }

        PhysicalGameState pgs = PhysicalGameState.load(map, utt);
        GameState gs = new GameState(pgs, utt);
        if (render) {
            w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);
        }
        while (true) {
            srai.reset();
            ai.reset();
            pgs = PhysicalGameState.load(map, utt);
            gs = new GameState(pgs, utt);
            while (true) {
                if (render) {
                    w.setStateCloning(gs);
                    w.repaint();
                }
                srai.computeReward(0, 1, gs);
                PlayerAction pa1 = srai.getAction(0, gs);
                if (srai.done) {
                    break;
                }
                PlayerAction pa2 = ai.getAction(1, gs);
                gs.issueSafe(pa1);
                gs.issueSafe(pa2);

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
            ai.gameOver(gs.winner());
            if (srai.finished) {
                break;
            }
        }
    }
}
