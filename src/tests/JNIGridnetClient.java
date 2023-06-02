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

import ai.PassiveAI;
import ai.RandomBiasedAI;
import ai.core.AI;
import ai.jni.JNIAI;
import ai.reward.RewardFunctionInterface;
import ai.jni.JNIInterface;
import ai.jni.Response;
import gui.PhysicalGameStateJFrame;
import gui.PhysicalGameStatePanel;
import rts.GameState;
import rts.PartiallyObservableGameState;
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

    // Settings
    public RewardFunctionInterface[] rfs;
    String micrortsPath;
    public String mapPath;
    public AI ai2;
    UnitTypeTable utt;
    public boolean partialObs = false;

    // Internal State
    public PhysicalGameState pgs;
    public GameState gs;
    public GameState player1gs, player2gs;
    boolean gameover = false;
    boolean layerJSON = true;
    public int renderTheme = PhysicalGameStatePanel.COLORSCHEME_WHITE;
    public int maxAttackRadius;
    PhysicalGameStateJFrame w;
    public JNIInterface ai1;

    // storage
    int[][][] masks;
    double[] rewards;
    boolean[] dones;
    Response response;
    PlayerAction pa1;
    PlayerAction pa2;

    public JNIGridnetClient(RewardFunctionInterface[] a_rfs, String a_micrortsPath, String a_mapPath, AI a_ai2, UnitTypeTable a_utt, boolean partial_obs) throws Exception{
        micrortsPath = a_micrortsPath;
        mapPath = a_mapPath;
        rfs = a_rfs;
        utt = a_utt;
        partialObs = partial_obs;
        maxAttackRadius = utt.getMaxAttackRange() * 2 + 1;
        ai1 = new JNIAI(100, 0, utt);
        ai2 = a_ai2;
        if (ai2 == null) {
            throw new Exception("no ai2 was chosen");
        }
        if (micrortsPath.length() != 0) {
            this.mapPath = Paths.get(micrortsPath, mapPath).toString();
        }

        pgs = PhysicalGameState.load(mapPath, utt);

        // initialize storage
        masks = new int[pgs.getHeight()][pgs.getWidth()][1+6+4+4+4+4+utt.getUnitTypes().size()+maxAttackRadius*maxAttackRadius];
        rewards = new double[rfs.length];
        dones = new boolean[rfs.length];
        response = new Response(null, null, null, null);
    }

    public byte[] render(boolean returnPixels) throws Exception {
        if (w==null) {
            w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, partialObs, null, renderTheme);
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
        if (partialObs) {
            player1gs = new PartiallyObservableGameState(gs, player);
            player2gs = new PartiallyObservableGameState(gs, 1 - player);
        } else {
            player1gs = gs;
            player2gs = gs;
        }
        pa1 = ai1.getAction(player, player1gs, action);
        pa2 = ai2.getAction(1 - player, player2gs);
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
        for (int i = 0; i < rewards.length; i++) {
            rfs[i].computeReward(player, 1 - player, te, gs);
            dones[i] = rfs[i].isDone();
            rewards[i] = rfs[i].getReward();
        }
        response.set(
            ai1.getObservation(player, player1gs),
            rewards,
            dones,
            ai1.computeInfo(player, player2gs));
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
        for (Unit u: pgs.getUnits()) {
            final UnitActionAssignment uaa = gs.getActionAssignment(u);
            if (u.getPlayer() == player && uaa == null) {
                masks[u.getY()][u.getX()][0] = 1;
                UnitAction.getValidActionArray(u, gs, utt, masks[u.getY()][u.getX()], maxAttackRadius, 1);
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
        if (partialObs) {
            player1gs = new PartiallyObservableGameState(gs, player);
        } else {
            player1gs = gs;
        }

        for (int i = 0; i < rewards.length; i++) {
            rewards[i] = 0;
            dones[i] = false;
        }
        response.set(
            ai1.getObservation(player, player1gs),
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
