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
    ArrayList<int[]> ai1Actions;
    double[] ai1SimulatedRewards;
    public int[][] ai1UnitMasks;
    public int renderTheme = PhysicalGameStatePanel.COLORSCHEME_WHITE;

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

    public JNIClient(RewardFunctionInterface[] a_rfs, String a_micrortsPath, String a_mapPath, AI a_ai2, UnitTypeTable a_utt) throws Exception{
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
        ai1SimulatedRewards = new double[rfs.length];
        System.out.println(mapPath);
        System.out.println(rfs);
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


    public Response step(int[][] action, int player) throws Exception {
        int numSourceUnits = sumArray(ai1UnitMasks);
        for (int[] a: action) {
            ai1Actions.add(a);
            int x = a[0] % gs.getPhysicalGameState().getWidth(), y = a[0] / gs.getPhysicalGameState().getWidth();
            ai1UnitMasks[y][x] = 0;
        }
        int[][] submittedAction = new int[ai1Actions.size()][action[0].length];
        for (int i = 0; i < submittedAction.length; i++) {
            for (int j = 0; j < submittedAction[0].length; j++) {
                submittedAction[i][j] = ai1Actions.get(i)[j];
            }
        }
        Response r = numSourceUnits > 1 
            ? simulateStep(submittedAction, player) : gameStep(submittedAction, player);
        double[] rewardDiff = subtract(r.reward, ai1SimulatedRewards);
        ai1SimulatedRewards = add(ai1SimulatedRewards, rewardDiff);
        r.reward = rewardDiff;
        if (numSourceUnits <= 1) {
            ai1Actions = new ArrayList<int[]>();
            ai1UnitMasks = getUnitMasks(player);
            ai1SimulatedRewards = new double[rfs.length];
        }
        return r;
    }

    public Response gameStep(int[][] action, int player) throws Exception {
        PlayerAction pa1;
        PlayerAction pa2;
        double[] rewards = new double[rfs.length];
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
            rewards[j] += rfs[j].getReward();
        }
        boolean[] dones = new boolean[rfs.length];
        for (int i = 0; i < rewards.length; i++) {
            dones[i] = rfs[i].isDone();
        }
        return new Response(
            ai1.getObservation(player, gs),
            rewards,
            dones,
            ai1.computeInfo(player, gs));
    }

    public Response simulateStep(int[][] action, int player) throws Exception {
        PlayerAction pa1;
        PlayerAction pa2;
        GameState simulatedGs = gs.clone();
        double[] rewards = new double[rfs.length];
        pa1 = ai1.getAction(player, simulatedGs, action);
        pa2 = new PlayerAction();
        pa2.fillWithNones(simulatedGs, 1 - player, 0);
        simulatedGs.issueSafe(pa1);
        simulatedGs.issueSafe(pa2);
        TraceEntry te  = new TraceEntry(simulatedGs.getPhysicalGameState().clone(), simulatedGs.getTime());
        te.addPlayerAction(pa1.clone());
        te.addPlayerAction(pa2.clone());

        // simulate:
        gameover = simulatedGs.cycle();
        if (gameover) {
            // ai1.gameOver(simulatedGs.winner());
            // ai2.gameOver(simulatedGs.winner());
        }
        for (int j = 0; j < rewards.length; j++) {
            rfs[j].computeReward(player, 1 - player, te, simulatedGs);
            rewards[j] += rfs[j].getReward();
        }
        boolean[] dones = new boolean[rfs.length];
        for (int i = 0; i < rewards.length; i++) {
            dones[i] = rfs[i].isDone();
        }
        return new Response(
            ai1.getObservation(player, simulatedGs),
            rewards,
            dones,
            ai1.computeInfo(player, simulatedGs));
    }

    public int[][] getUnitActionMasks(int[][] actions) throws Exception {
        int width = gs.getPhysicalGameState().getWidth();
        int height = gs.getPhysicalGameState().getHeight();
        int[][] unitActionMasks = new int[actions.length][6+4+4+4+4+utt.getUnitTypes().size()+width*height];
        if (sumArray(ai1UnitMasks)!=0){
            for (int i = 0; i < unitActionMasks.length; i++) {
                Unit u = gs.getPhysicalGameState().getUnitAt(
                    actions[i][0] % width,
                    actions[i][0] / width);
                unitActionMasks[i] = UnitAction.getValidActionArray(u.getUnitActions(gs), gs, utt);
            }
        }
        return unitActionMasks;
    }

    public int[][] getUnitMasks(int player) throws Exception {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        int[][] unitMasks = new int[pgs.getHeight()][pgs.getWidth()];
        for (int i = 0; i < pgs.getUnits().size(); i++) {
            Unit u = pgs.getUnits().get(i);
            UnitActionAssignment uaa = gs.getUnitActions().get(u);
            if (u.getPlayer() == player && uaa == null) {
                unitMasks[u.getY()][u.getX()] = 1;
            }
        }
        return unitMasks;
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
        ai1Actions = new ArrayList<int[]>();
        ai1UnitMasks = getUnitMasks(player);
        ai1SimulatedRewards = new double[rfs.length];
        return new Response(
            ai1.getObservation(player, gs),
            new double[rfs.length],
            new boolean[rfs.length],
            "{}");
    }

    public void close() throws Exception {
        if (w!=null) {
            w.dispose();    
        }
    }

    public static int sumArray(int[][] array) {
        int sum = 0;
        for (int[] ints : array) {
            for (int number : ints) {
                sum += number;
            }
        }
        return sum;
    }

    public static double[] add(double[] first, double[] second) {
        double[] result = new double[first.length];
        for (int i = 0; i < first.length; i++) {
            result[i] = first[i] + second[i];
        }
        return result;
    }

    public static double[] subtract(double[] first, double[] second) {
        double[] result = new double[first.length];
        for (int i = 0; i < first.length; i++) {
            result[i] = first[i] - second[i];
        }
        return result;
    }
}
