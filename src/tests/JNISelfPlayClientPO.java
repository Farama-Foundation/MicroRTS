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
import tests.JNIClientPO;
import tests.JNIClientPO.Response;

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
public class JNISelfPlayClientPO {

    PhysicalGameStateJFrame w;
    public JNIInterface[] ais = new JNIInterface[2];
    // public JNIInterface ai1;
    // public JNIInterface ai2;
    PhysicalGameState pgs;
    GameState gs;
    PartiallyObservableGameState pogs1, pogs2;
    UnitTypeTable utt;
    public RewardFunctionInterface[] rfs;
    String mapPath;
    String micrortsPath;
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

    public JNISelfPlayClientPO(RewardFunctionInterface[] a_rfs, String a_micrortsPath, String a_mapPath, UnitTypeTable a_utt) throws Exception{
        micrortsPath = a_micrortsPath;
        mapPath = a_mapPath;
        rfs = a_rfs;
        utt = a_utt;
        maxAttackRadius = utt.getMaxAttackRange() * 2 + 1;
        if (micrortsPath.length() != 0) {
            this.mapPath = Paths.get(micrortsPath, mapPath).toString();
        }
        System.out.println(mapPath);
        System.out.println(rfs);
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
            w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, true, null, renderTheme);
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
        pogs1 = new PartiallyObservableGameState(gs, 0);
        pogs2 = new PartiallyObservableGameState(gs, 1);
        for (int i = 0; i < numPlayers; i++) {
            pas[i] = i == 0 ? ais[i].getAction(i, pogs1, action1) : ais[i].getAction(i, pogs2, action2);
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
            PartiallyObservableGameState pogs = new PartiallyObservableGameState(gs, i);
            response[i].set(
                ais[i].getObservation(i, pogs),
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
        for (int i = 0; i < pgs.getUnits().size(); i++) {
            Unit u = pgs.getUnits().get(i);
            UnitActionAssignment uaa = gs.getUnitActions().get(u);
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
            ais[i].reset();
            for (int j = 0; j < rewards.length; j++) {
                rewards[i][j] = 0;
                dones[i][j] = false;
            }
            PartiallyObservableGameState pogs = new PartiallyObservableGameState(gs, i);
            response[i].set(
                ais[i].getObservation(i, pogs),
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
