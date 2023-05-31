/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package tests;

import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

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
public class JNIGridnetClientSelfPlay {


    // Settings
    public RewardFunctionInterface[] rfs;
    String micrortsPath;
    public String mapPath;
    public AI ai2;
    UnitTypeTable utt;
    boolean partialObs = false;

    // Internal State
    PhysicalGameStateJFrame w;
    public JNIInterface[] ais = new JNIInterface[2];
    public PhysicalGameState pgs;
    public GameState gs;
    public GameState[] playergs = new GameState[2];
    boolean gameover = false;
    boolean layerJSON = true;
    public int renderTheme = PhysicalGameStatePanel.COLORSCHEME_WHITE;
    public int maxAttackRadius;
    public int numPlayers = 2;

    // storage
    int[][][][] masks = new int[2][][][];
    double[][] rewards = new double[2][];
    boolean[][] dones = new boolean[2][];
    Response[] response = new Response[2];
    PlayerAction[] pas = new PlayerAction[2];

    public JNIGridnetClientSelfPlay(RewardFunctionInterface[] a_rfs, String a_micrortsPath, String a_mapPath, UnitTypeTable a_utt, boolean partial_obs) throws Exception{
        micrortsPath = a_micrortsPath;
        mapPath = a_mapPath;
        rfs = a_rfs;
        utt = a_utt;
        partialObs = partial_obs;
        maxAttackRadius = utt.getMaxAttackRange() * 2 + 1;
        if (micrortsPath.length() != 0) {
            this.mapPath = Paths.get(micrortsPath, mapPath).toString();
        }

        pgs = PhysicalGameState.load(mapPath, utt);

        // initialize storage
        for (int i = 0; i < numPlayers; i++) {
            ais[i] = new JNIAI(100, 0, utt);
            masks[i] = new int[pgs.getHeight()][pgs.getWidth()][1+6+4+4+4+4+utt.getUnitTypes().size()+maxAttackRadius*maxAttackRadius];
            rewards[i] = new double[rfs.length];
            dones[i] = new boolean[rfs.length];
            response[i] = new Response(null, null, null, null);
        }
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

    public void gameStep(int[][] action1, int[][] action2) throws Exception {
        TraceEntry te  = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
        for (int i = 0; i < numPlayers; i++) {
            playergs[i] = gs;
            if (partialObs) {
                playergs[i] = new PartiallyObservableGameState(gs, i);
            }
            pas[i] = i == 0 ? ais[i].getAction(i, playergs[0], action1) : ais[i].getAction(i, playergs[1], action2);
            gs.issueSafe(pas[i]);
            te.addPlayerAction(pas[i].clone());
        }
        // simulate:
        gameover = gs.cycle();
        if (gameover) {
            // ai1.gameOver(gs.winner());
            // ai2.gameOver(gs.winner());
        }

        for (int i = 0; i < numPlayers; i++) {
            for (int j = 0; j < rfs.length; j++) {
                rfs[j].computeReward(i, 1 - i, te, gs);
                rewards[i][j] = rfs[j].getReward();
                dones[i][j] = rfs[j].isDone();
            }
            response[i].set(
                ais[i].getObservation(i, playergs[i]),
                rewards[i],
                dones[i],
                "{}");
        }
    }

    public int[][][] getMasks(int player) throws Exception {
        for (int i = 0; i < masks[0].length; i++) {
            for (int j = 0; j < masks[0][0].length; j++) {
                for (int k = 0; k < masks[0][0][0].length; k++) {
                    masks[player][i][j][k] = 0;
                }
            }
        }
        for (Unit u: pgs.getUnits()) {
            final UnitActionAssignment uaa = gs.getActionAssignment(u);
            if (u.getPlayer() == player && uaa == null) {
                masks[player][u.getY()][u.getX()][0] = 1;
                UnitAction.getValidActionArray(u, gs, utt, masks[player][u.getY()][u.getX()], maxAttackRadius, 1);
            }
        }
        return masks[player];
    }

    public String sendUTT() throws Exception {
        Writer w = new StringWriter();
        utt.toJSON(w);
        return w.toString(); // now it works fine
    }

    public void reset() throws Exception {
        pgs = PhysicalGameState.load(mapPath, utt);
        gs = new GameState(pgs, utt);
        for (int i = 0; i < numPlayers; i++) {
            playergs[i] = gs;
            if (partialObs) {
                playergs[i] = new PartiallyObservableGameState(gs, i);
            }
            ais[i].reset();
            for (int j = 0; j < rewards.length; j++) {
                rewards[i][j] = 0;
                dones[i][j] = false;
            }
            response[i].set(
                ais[i].getObservation(i, playergs[i]),
                rewards[i],
                dones[i],
                "{}");
        }

        // return response;
    }

    public Response getResponse(int player) {
        return response[player];
    }

    public void close() throws Exception {
        if (w!=null) {
            w.dispose();    
        }
    }
}
