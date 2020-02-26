/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package tests;

import java.io.Writer;
import java.nio.file.Paths;

import java.awt.image.BufferedImage;
import java.io.StringWriter;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import com.beust.jcommander.Parameter;

import ai.PassiveAI;
import ai.RandomBiasedAI;
import ai.RandomNoAttackAI;
import ai.core.AI;
import ai.jni.JNIAI;
import ai.jni.JNILocalAI;
import ai.rewardfunction.RewardFunctionInterface;
import ai.jni.JNIInterface;
import gui.PhysicalGameStateJFrame;
import gui.PhysicalGameStatePanel;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.Trace;
import rts.TraceEntry;
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
    public RewardFunctionInterface[] rfs;
    String mapPath;
    String micrortsPath;
    boolean gameover = false;
    boolean layerJSON = true;

    public class Response {
        public int[][][] observation;
        public double[] reward;
        public boolean[] done;
        public String info;

        public Response(int[][][] observation, double reward[], boolean done[], String info) {
            this.observation = observation;
            this.reward = reward;
            this.done = done;
            this.info = info;
        }
    }

    public JNIClient(RewardFunctionInterface[] a_rfs, String a_micrortsPath, String a_mapPath) throws Exception{
        micrortsPath = a_micrortsPath;
        mapPath = a_mapPath;
        rfs = a_rfs;
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
        System.out.println(rfs);
    }

    public JNIClient(RewardFunctionInterface[] a_rfs, String a_micrortsPath, String a_mapPath, AI a_ai2) throws Exception{
        micrortsPath = a_micrortsPath;
        mapPath = a_mapPath;
        rfs = a_rfs;
        utt = new UnitTypeTable();
        utt.getUnitType("Worker").harvestTime = 10;
        ai1 = new JNIAI(100, 0, utt);
        ai2 = a_ai2;
        if (ai2 == null) {
            throw new Exception("no ai2 was chosen");
        }
        if (micrortsPath.length() != 0) {
            this.mapPath = Paths.get(micrortsPath, mapPath).toString();
        }
        System.out.println(mapPath);
        System.out.println(rfs);
    }

    public JNIClient(RewardFunctionInterface[] a_rfs, String a_micrortsPath, String a_mapPath, int windowSize) throws Exception{
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

    public Response step(int[][] action, int frameskip) throws Exception {
        double totalReward = 0.0;
        PlayerAction pa1;
        PlayerAction pa2;
        double[] rewards = new double[rfs.length];
        // frameskip
        for (int i=0;i<=frameskip;i++) {
            if (i==0) {
                pa1 = ai1.getAction(0, gs, action);
            } else {
                pa1 = new PlayerAction();
                pa1.fillWithNones(gs, 0, 0);
            }
            pa2 = ai2.getAction(1, gs);
            gs.issueSafe(pa1);
            gs.issueSafe(pa2);
            TraceEntry te  = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
            te.addPlayerAction(pa1.clone());
            te.addPlayerAction(pa2.clone());
    
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
            
            for (int j = 0; j < rewards.length; j++) {
                rfs[j].computeReward(0, 1, te, gs);
                rewards[j] += rfs[j].getReward();
            }
        }

        boolean[] dones = new boolean[rfs.length];
        for (int i = 0; i < rewards.length; i++) {
            dones[i] = rfs[i].isDone();
        }
        // TODO return observation in JSON format
        return new Response(
            ai1.getObservation(0, gs),
            rewards,
            dones,
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
        pgs = PhysicalGameState.load(mapPath, utt);
        gs = new GameState(pgs, utt);
        return new Response(
            ai1.getObservation(0, gs),
            new double[rfs.length],
            new boolean[rfs.length],
            "{}");
    }

    public void close() throws Exception {
        if (w!=null) {
            w.dispose();    
        }
    }
}
