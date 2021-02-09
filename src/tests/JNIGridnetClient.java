/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package tests;

import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
import rts.UnitActionAssignment;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import weka.core.pmml.jaxbbindings.False;

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
public class JNIGridnetClient {

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
    public int renderTheme = PhysicalGameStatePanel.COLORSCHEME_WHITE;

    // storage
    int[][][] masks;
    double[] rewards;
    boolean[] dones;
    Response response;

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

        public void set(int[][][] observation, double reward[], boolean done[], String info) {
            this.observation = observation;
            this.reward = reward;
            this.done = done;
            this.info = info;
        }
    }

    public JNIGridnetClient(RewardFunctionInterface[] a_rfs, String a_micrortsPath, String a_mapPath, AI a_ai2, UnitTypeTable a_utt) throws Exception{
        micrortsPath = a_micrortsPath;
        mapPath = a_mapPath;
        rfs = a_rfs;
        utt = a_utt;
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
        pgs = PhysicalGameState.load(mapPath, utt);

        // initialize storage
        masks = new int[pgs.getHeight()][pgs.getWidth()][1+6+4+4+4+4+utt.getUnitTypes().size()+pgs.getWidth()*pgs.getHeight()];
        rewards = new double[rfs.length];
        dones = new boolean[rfs.length];
        response = new Response(null, null, null, null);
    }

    public byte[] render(boolean returnPixels) throws Exception {
        if (w==null) {
            w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, false, renderTheme);
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

    public Response gameStep(int[][] action, int player) throws Exception {
        PlayerAction pa1;
        PlayerAction pa2;
        pa1 = ai1.getAction(player, gs, action);
        pa2 = ai2.getAction(1 - player, gs);

        gs.issueSafe(pa1);
        gs.issueSafe(pa2);
        TraceEntry te  = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
        te.addPlayerAction(pa1.clone());
        te.addPlayerAction(pa2.clone());

        // simulate:
        gameover = gs.cycle();
        if (gameover) {
            // ai1.gameOver(gs.winner());
            ai2.gameOver(gs.winner());
        }
        
        for (int j = 0; j < rewards.length; j++) {
            rfs[j].computeReward(player, 1 - player, te, gs);
            rewards[j] = rfs[j].getReward();
        }
        for (int i = 0; i < rewards.length; i++) {
            dones[i] = rfs[i].isDone();
        }
        response.set(
            ai1.getObservation(player, gs),
            rewards,
            dones,
            ai1.computeInfo(player, gs));
        return response;
    }

    public int[][][] getMasks(int player) throws Exception {
        for (int i = 0; i < masks.length; i++) {
            for (int j = 0; j < masks[0].length; j++) {
                for (int k = 0; k < masks[0][0].length; k++) {
                    masks[i][j][k] = 0;
                }
            }
        }
        for (int i = 0; i < pgs.getUnits().size(); i++) {
            Unit u = pgs.getUnits().get(i);
            UnitActionAssignment uaa = gs.getUnitActions().get(u);
            if (u.getPlayer() == player && uaa == null) {
                masks[u.getY()][u.getX()][0] = 1;
                UnitAction.getValidActionArray(u.getUnitActions(gs), gs, utt, masks[u.getY()][u.getX()]);
            }
        }
        return masks;
    }

    public String sendUTT() throws Exception {
        Writer w = new StringWriter();
        utt.toJSON(w);
        return w.toString(); // now it works fine
    }

    public Response reset(int player) throws Exception {
        ai1.reset();
        ai2 = ai2.clone();
        ai2.reset();
        pgs = PhysicalGameState.load(mapPath, utt);
        gs = new GameState(pgs, utt);

        for (int i = 0; i < rewards.length; i++) {
            rewards[i] = 0;
            dones[i] = false;
        }
        response.set(
            ai1.getObservation(player, gs),
            rewards,
            dones,
            "{}");
        return response;
    }

    public void close() throws Exception {
        if (w!=null) {
            w.dispose();    
        }
    }
}
