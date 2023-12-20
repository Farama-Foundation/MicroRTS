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
 * **one** player, where the opponent is controlled directly by a Java-based AI. 
 * See JNIGridnetClientSelfPlay.java for a client where both players are 
 * externally controlled.
 *
 * @author santi and costa
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

    // Storage
    
    // [Y][X][
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
    int[][][] masks;
    
    double[] rewards;
    boolean[] dones;
    Response response;
    PlayerAction pa1;
    PlayerAction pa2;

    /**
     * 
     * @param a_rfs Reward functions we want to use to compute rewards at every step.
     * @param a_micrortsPath Path for the microrts root directory (with Java code and maps).
     * @param a_mapPath Path (under microrts root dir) for map to load.
     * @param a_ai2 The AI object that should select actions for the opponent (i.e., for the
     * 	player that will not be controlled by external/JNI/Python code)
     * @param a_utt
     * @param partial_obs
     * @throws Exception
     */
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
        try {
        	pa2 = ai2.getAction(1 - player, player2gs);
        }
        catch (final Exception e) {
        	System.out.println("AI crash on map: " + mapPath);
        	e.printStackTrace(System.out);
        	throw e;
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

    /**
     * @param player
     * @return Legal actions mask for given player.
     * @throws Exception
     */
    public int[][][] getMasks(int player) throws Exception {
        for (int i = 0; i < masks.length; i++) {
            for (int j = 0; j < masks[0].length; j++) {
                Arrays.fill(masks[i][j], 0);
            }
        }
        for (Unit u: pgs.getUnits()) {
            if (u.getPlayer() == player && gs.getActionAssignment(u) == null) {
                masks[u.getY()][u.getX()][0] = 1;
                UnitAction.getValidActionArray(u, gs, utt, masks[u.getY()][u.getX()], maxAttackRadius, 1);
            }
        }
        return masks;
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

    public Response reset(int player) throws Exception {
        ai1.reset();
        ai2 = ai2.clone();
        ai2.reset();
        pgs = PhysicalGameState.load(mapPath, utt);
        masks = new int[pgs.getHeight()][pgs.getWidth()][1+6+4+4+4+4+utt.getUnitTypes().size()+maxAttackRadius*maxAttackRadius];
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
