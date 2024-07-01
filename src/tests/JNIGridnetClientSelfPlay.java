package tests;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Arrays;

import ai.core.AI;
import ai.jni.JNIAI;
import ai.jni.JNIInterface;
import ai.jni.Response;
import ai.reward.RewardFunctionInterface;
import gui.PhysicalGameStateJFrame;
import gui.PhysicalGameStatePanel;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.TraceEntry;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 * Instances of this class each let us run a single environment (or sequence
 * of them, if we reset() in between) between two players. 
 * 
 * In this client, it is assumed that actions are selected by external code for 
 * **both** players. See JNIGridnetClient.java for a client where only one
 * player is externally controlled, and the other is a plain Java AI.
 *
 * @author santi and costa
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
    public JNIInterface[] ais = new JNIInterface[2];	// These AIs won't actually play: just convert action formats for us
    public PhysicalGameState pgs;
    public GameState gs;
    public GameState[] playergs = new GameState[2];
    boolean gameover = false;
    boolean layerJSON = true;
    public int renderTheme = PhysicalGameStatePanel.COLORSCHEME_WHITE;
    public int maxAttackRadius;
    public int numPlayers = 2;

    // Storage
    
    // [player][Y][X][
    //		0: do we own a unit without action assignment here?			|-- 1
    //
    //		1: can we do a no-op action?								|
    //		2: can we do a move action?									|
    //		3: can we do a harvest action?								|-- 6 action types
    //		4: can we do a return to base with resources action?		|
    //		5: can we do a produce unit action?							|
    //		6: can we do an attack action								|
    //
    //		7: can we move up/north?									|
    //		8: can we move right/east?									|-- 4 move directions
    //		9: can we move down/south?									|
    //		10: can we move left/west?									|
    //
    //		11: can we harvest up/north?								|
    //		12: can we harvest right/east?								|-- 4 harvest directions
    //		13: can we harvest down/south?								|
    //		14: can we harvest left/west?								|
    //
    //		15: can we return resources to base up/north?				|
    //		16: can we return resources to base right/east?				|-- 4 return resources to base directions
    //		17: can we return resources to base down/south?				|
    //		18: can we return resources to base left/west?				|
    //
    //		19: can we produce unit up/north?							|
    //		20: can we produce unit right/east?							|-- 4 produce unit directions
    //		21: can we produce unit down/south?							|
    //		22: can we produce unit left/west?							|
    //
    //		23: can we produce a unit of type 0?						|
    //		24: can we produce a unit of type 1?						|-- k (= 7) unit types to produce
    //		...: ....													|
    //		29: can we produce a unit of type 6?						|
    //
    //		30: can we attack relative position at ...?					|
    //		31: can we attack relative position at ...?					|-- (maxAttackRange)^2 relative attack locations
    //		...: ...													|
    // ]
    int[][][][] masks = new int[2][][][];
    
    double[][] rewards = new double[2][];
    boolean[][] dones = new boolean[2][];
    Response[] response = new Response[2];
    PlayerAction[] pas = new PlayerAction[2];

    /**
     * 
     * @param a_rfs Reward functions we want to use to compute rewards at every step.
     * @param a_micrortsPath Path for the microrts root directory (with Java code and maps).
     * @param a_mapPath Path (under microrts root dir) for map to load.
     * @param a_utt
     * @param partial_obs
     * @throws Exception
     */
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

    /**
     * @param player
     * @return Legal actions mask for given player.
     * @throws Exception
     */
    public int[][][] getMasks(int player) throws Exception {
        for (int i = 0; i < masks[0].length; i++) {
            for (int j = 0; j < masks[0][0].length; j++) {
            	Arrays.fill(masks[player][i][j], 0);
            }
        }
        for (Unit u: pgs.getUnits()) {
            if (u.getPlayer() == player && gs.getActionAssignment(u) == null) {
                masks[player][u.getY()][u.getX()][0] = 1;
                UnitAction.getValidActionArray(u, gs, utt, masks[player][u.getY()][u.getX()], maxAttackRadius, 1);
            }
        }
        return masks[player];
    }

    /**
     * @return String representation (in JSON format) of the Unit Type Table
     * @throws Exception
     */
    public String sendUTT() throws Exception {
        Writer w = new StringWriter();
        utt.toJSON(w);
        return w.toString(); // now it works fine
    }

    public void reset() throws Exception {
        pgs = PhysicalGameState.load(mapPath, utt);
        
        for (int i = 0; i < numPlayers; i++) {
            masks[i] = new int[pgs.getHeight()][pgs.getWidth()][1+6+4+4+4+4+utt.getUnitTypes().size()+maxAttackRadius*maxAttackRadius];
        }
        
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
