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
import rts.UnitAction;
import rts.units.Unit;
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
public class JNISelfPlayClient {
    PhysicalGameStateJFrame w;
    public JNIInterface ai1;
    public JNIInterface ai2;
    PhysicalGameState pgs;
    GameState gs;
    UnitTypeTable utt;
    public RewardFunctionInterface[] rfs;
    String mapPath;
    String micrortsPath;
    boolean gameover = false;
    boolean layerJSON = true;
    int gamestep = 0;
    int ai2Frameskip = 20;

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

    public JNISelfPlayClient(RewardFunctionInterface[] a_rfs, String a_micrortsPath, String a_mapPath, UnitTypeTable a_utt) throws Exception{
        micrortsPath = a_micrortsPath;
        mapPath = a_mapPath;
        rfs = a_rfs;
        utt = a_utt;
        ai1 = new JNIAI(100, 0, utt);
        ai2 = new JNIAI(100, 0, utt);
        if (micrortsPath.length() != 0) {
            this.mapPath = Paths.get(micrortsPath, mapPath).toString();
        }
        System.out.println(mapPath);
        System.out.println(rfs);
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

    public Response step(int[][] action, int[][] action2, int frameskip) throws Exception {
        PlayerAction pa1;
        PlayerAction pa2;
        double[] rewards = new double[rfs.length];
        // frameskip
        for (int i=0;i<=frameskip;i++) {
            if (i==0) {
                pa1 = ai1.getAction(0, gs, action);
                pa2 = ai2.getAction(1, gs, action2);
            } else {
                pa1 = new PlayerAction();
                pa1.fillWithNones(gs, 0, 0);
                pa2 = new PlayerAction();
                pa2.fillWithNones(gs, 1, 0);
            }
            gs.issueSafe(pa1);
            gs.issueSafe(pa2);
            TraceEntry te  = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
            te.addPlayerAction(pa1.clone());
            te.addPlayerAction(pa2.clone());
    
            // simulate:
            gameover = gs.cycle();
            if (gameover) {
                // ai1.gameOver(gs.winner());
                // ai2.gameOver(gs.winner());
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
        gamestep += 1;
        return new Response(
            ai1.getObservation(0, gs),
            rewards,
            dones,
            ai1.computeInfo(0, gs));
    }

    public int[][] getUnitActionMasks(int[][] actions) throws Exception {
        int[][] unitActionMasks = new int[actions.length][6+4+4+4+4];
        int width = gs.getPhysicalGameState().getWidth();
        for (int i = 0; i < unitActionMasks.length; i++) {
            Unit u = gs.getPhysicalGameState().getUnitAt(
                actions[i][0] % width,
                actions[i][0] / width);
            unitActionMasks[i] = UnitAction.getValidActionArray(u.getUnitActions(gs), gs, utt);
        }
        return unitActionMasks;
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
