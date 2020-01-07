/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package tests;

import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;
import javax.imageio.ImageIO;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ai.PassiveAI;
import ai.RandomBiasedAI;
import ai.RandomNoAttackAI;
import ai.core.AI;
import ai.jni.JNIAI;
import ai.jni.JNILocalAI;
import ai.jni.JNIInterface;
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
public class JNIClient {

    @Parameter(names = "--ai2-type", description = "The type of AI2")
    String ai2Type = "passive";

    @Parameter(names = "--seed", description = "The random seed")
    int seed = 3;

    PhysicalGameStateJFrame w;
    public JNIInterface ai1;
    public AI ai2;
    PhysicalGameState pgs;
    GameState gs;
    UnitTypeTable utt;
    String mapPath;
    String micrortsPath;
    boolean gameover = false;
    boolean layerJSON = true;

    public class Response {
        public int[][][] observation;
        public double reward;
        public boolean done;
        public String info;

        public Response(int[][][] observation, double reward, boolean done, String info) {
            this.observation = observation;
            this.reward = reward;
            this.done = done;
            this.info = info;
        }
    }

    public JNIClient(String a_micrortsPath, String a_mapPath) throws Exception{
        micrortsPath = a_micrortsPath;
        mapPath = a_mapPath;
        utt = new UnitTypeTable();
        utt.getUnitType("Worker").harvestTime = 10;
        ai1 = new JNIAI(100, 0, utt);
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
            this.mapPath = Paths.get(micrortsPath, mapPath).toString();
        }
        System.out.println(mapPath);
    }

    public JNIClient(String a_micrortsPath, String a_mapPath, int windowSize) throws Exception{
        micrortsPath = a_micrortsPath;
        mapPath = a_mapPath;
        utt = new UnitTypeTable();
        utt.getUnitType("Worker").harvestTime = 10;
        ai1 = new JNILocalAI(100, 0, utt, windowSize);
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
            mapPath = Paths.get(micrortsPath, mapPath).toString();
        }
        System.out.println(mapPath);
    }

    public byte[] render(boolean returnPixels) throws Exception {
        // TODO: The blue and red color reversed. Low priority
        long startTime = System.nanoTime();
        if (w==null) {
            w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);
        }
        w.setStateCloning(gs);
        w.repaint();

        if (!returnPixels) {
            return null;
        }
        BufferedImage image = new BufferedImage(w.getWidth(),
        w.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        w.paint(image.getGraphics());

        WritableRaster raster = image .getRaster();
        DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
        return data.getData();
    }

    public Response step(int[][] action) throws Exception {
        PlayerAction pa1 = ai1.getAction(0, gs, action);
        PlayerAction pa2 = ai2.getAction(1, gs);
        gs.issueSafe(pa1);
        gs.issueSafe(pa2);

        // simulate:
        gameover = gs.cycle();
        if (gameover) {
            // ai1.gameOver(gs.winner());
            ai2.gameOver(gs.winner());
        }
        try {
            Thread.yield();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TODO return observation in JSON format
        return new Response(
            ai1.getObservation(0, gs),
            ai1.computeReward(0, 1, gs),
            gameover,
            ai1.computeInfo(0, gs));
    }

    public String sendUTT() throws Exception {
        Writer w = new StringWriter();
        utt.toJSON(w);
        return w.toString(); // now it works fine
    }

    public Response reset() throws Exception {
        ai1.reset();
        ai2.reset();
        System.out.println(mapPath);
        pgs = PhysicalGameState.load(mapPath, utt);
        gs = new GameState(pgs, utt);
        return new Response(
            ai1.getObservation(0, gs),
            0.0,
            false,
            "{}");
    }

    public void close() throws Exception {
        if (w!=null) {
            w.dispose();    
        }
    }
}
