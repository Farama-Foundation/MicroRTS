/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package tests;

import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ai.PassiveAI;
import ai.RandomBiasedAI;
import ai.RandomNoAttackAI;
import ai.core.AI;
import ai.socket.IndividualSocketRewardAI;
import ai.socket.SocketAIInterface;
import ai.socket.SocketRewardAI;
import ai.socket.SocketRewardPenaltyOnInvalidActionAI;
import gui.PhysicalGameStateJFrame;
import gui.PhysicalGameStatePanel;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

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
public class PythonClient {
    @Parameter(names = "--server-ip", description = "The microRTS server IP")
    String serverIP = "127.0.0.1";

    @Parameter(names = "--server-port", description = "The microRTS server port")
    int serverPort = 9898;

    @Parameter(names = "--unix-socket-path", description = "The path to the unix domain socket file")
    String unixSocketPath = "/home/costa/Documents/work/go/src/github.com/vwxyzjn/gym-microrts/unix/u";

    @Parameter(names = "--map", description = "Which map in the `maps` folder are you using?")
    String map = "maps/4x4/baseTwoWorkersMaxResources4x4.xml";

    @Parameter(names = "--ai1-type", description = "The type of AI1")
    String ai1Type = "random-no-attack";

    @Parameter(names = "--ai2-type", description = "The type of AI2")
    String ai2Type = "passive";

    @Parameter(names = "--window-size", description = "The microRTS server IP")
    int windowSize = 1;

    @Parameter(names = "--seed", description = "The random seed")
    int seed = 3;

    @Parameter(names = "--evaluation-filename", description = "Whether to save the evaluation results in a the supplied filename")
    String evaluationFileName = "./test.json";

    @Parameter(names = "--microrts-path", description = "The path of microrts unzipped folder")
    String micrortsPath = "";

    PhysicalGameStateJFrame w;
    public SocketAIInterface ai1;
    public AI ai2;
    PhysicalGameState pgs;
    GameState gs;
    UnitTypeTable utt;
    boolean gameover = false;
    boolean layerJSON = true;

    public PythonClient() {

    }

    public void run() throws Exception {

        utt = new UnitTypeTable();
        utt.getUnitType("Worker").harvestTime = 10;
        
        ai1 = new RandomNoAttackAI(seed);

        switch (ai2Type) {
            case "passive":
                ai2 = new PassiveAI();
                break;
            case "random-biased":
                ai2 = new RandomBiasedAI();
                break;
            default:
                throw new Exception("no ai2 was chosen");
        }


        if (micrortsPath.length() != 0) {
            map = Paths.get(micrortsPath, map).toString();
        }
        System.out.println(map);
        pgs = PhysicalGameState.load(map, utt);
        gs = new GameState(pgs, utt);
        // System.out.println(render(true));
        // w.dispose();
    }


    public byte[] render(boolean returnPixels) throws Exception {
        ai1.reset();
        ai2.reset();
        // pgs = PhysicalGameState.load(map, utt);
        // gs = new GameState(pgs, utt);
        if (w==null) {
            w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);
        }
        w.setStateCloning(gs);
        w.repaint();
        BufferedImage image = new BufferedImage(w.getWidth(),
        w.getHeight(), BufferedImage.TYPE_INT_RGB);
        // paints into image's Graphics
        w.paint(image.getGraphics());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", baos);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    public void step() throws Exception {
            ai1.computeReward(0, 1, gs);
            PlayerAction pa1 = ai1.getAction(0, gs);
            PlayerAction pa2 = ai2.getAction(1, gs);
            gs.issueSafe(pa1);
            gs.issueSafe(pa2);

            // simulate:
            gameover = gs.cycle();
            if (gameover) {
                ai1.gameOver(gs.winner());
                ai2.gameOver(gs.winner());
            }
            try {
                Thread.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }


    }

    public void reset() throws Exception {
        ai1.reset();
        ai2.reset();
        pgs = PhysicalGameState.load(map, utt);
        gs = new GameState(pgs, utt);
    }

    public void close() throws Exception {
        if (w!=null) {
            w.dispose();    
        }
    }
}
