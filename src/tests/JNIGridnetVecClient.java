/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package tests;

import java.io.Writer;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.awt.image.BufferedImage;
import java.io.StringWriter;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

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
import rts.Player;
import rts.PlayerAction;
import rts.Trace;
import rts.TraceEntry;
import rts.UnitAction;
import rts.UnitActionAssignment;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import tests.JNIGridnetClient;
import tests.JNIGridnetClient.Response;

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
public class JNIGridnetVecClient {
    public JNIGridnetClient[] clients;
    public int maxSteps;
    public int[] envSteps; 
    public ExecutorService pool;
    public RewardFunctionInterface[] rfs;
    public UnitTypeTable utt;

    // storage
    int[][][][] masks;
    int[][][][] observation;
    double[][] reward;
    boolean[][] done;
    JNIGridnetClient.Response[] rs;
    Responses responses;

    public JNIGridnetVecClient(int a_num_envs, int a_max_steps, RewardFunctionInterface[] a_rfs, String a_micrortsPath, String a_mapPath,
            AI[] a_ai2s, UnitTypeTable a_utt) throws Exception {
        maxSteps = a_max_steps;
        utt = a_utt;
        clients = new JNIGridnetClient[a_num_envs];
        for (int i = 0; i < a_num_envs; i++) {
            clients[i] = new JNIGridnetClient(a_rfs, a_micrortsPath, a_mapPath, a_ai2s[i], a_utt);
        }
        rfs = a_rfs;
        envSteps = new int[a_num_envs];
        pool = Executors.newFixedThreadPool(64);
        
        // initialize storage
        JNIGridnetClient.Response r = clients[0].reset(0);
        int s1 = a_num_envs, s2 = r.observation.length, s3 = r.observation[0].length,
                s4 = r.observation[0][0].length;
        masks = new int[s1][][][];
        observation = new int[s1][s2][s3][s4];
        reward = new double[s1][rfs.length];
        done = new boolean[s1][rfs.length];
        responses = new Responses(null, null, null);
        rs = new JNIGridnetClient.Response[s1];
    }

    public class Responses {
        public int[][][][] observation;
        public double[][] reward;
        public boolean[][] done;
        // public String info;

        public Responses(int[][][][] observation, double reward[][], boolean done[][]) {
            this.observation = observation;
            this.reward = reward;
            this.done = done;
            // this.info = info;
        }

        public void set(int[][][][] observation, double reward[][], boolean done[][]) {
            this.observation = observation;
            this.reward = reward;
            this.done = done;
            // this.info = info;
        }
    }

    public Responses reset(int[] players) throws Exception {
        for (int i = 0; i < players.length; i++) {
            rs[i] = clients[i].reset(players[i]);
        }
        for (int i = 0; i < rs.length; i++) {
            observation[i] = rs[i].observation;
            reward[i] = rs[i].reward;
            done[i] = rs[i].done;
        }
        responses.set(observation, reward, done);
        return responses;
    }

    public Responses gameStep(int[][][] action, int[] players) throws Exception {
        for (int i = 0; i < players.length; i++) {
            envSteps[i] += 1;
            rs[i] = clients[i].gameStep(action[i], players[i]);
            // System.out.println(i);
            // System.out.println(rs[i].done[0]);
            if (rs[i].done[0] || envSteps[i] >= maxSteps) {
                JNIGridnetClient.Response r = clients[i].reset(players[i]);
                rs[i].observation = r.observation;
                rs[i].done[0] = true;
                envSteps[i] = 0;
            }
        }
        for (int i = 0; i < rs.length; i++) {
            observation[i] = rs[i].observation;
            reward[i] = rs[i].reward;
            done[i] = rs[i].done;
        }
        responses.set(observation, reward, done);
        return responses;
    }


    public int[][][][] getMasks(int player) throws Exception {
        for (int i = 0; i < masks.length; i++) {
            masks[i] = clients[i].getMasks(player);
        }
        return masks;
    }
}
