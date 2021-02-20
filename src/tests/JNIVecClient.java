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
import tests.JNIClient;
import tests.JNIClient.Response;

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
public class JNIVecClient {
    public JNIClient[] clients;
    public int maxSteps;
    public int[] envSteps; 
    public ExecutorService pool;
    public UnitTypeTable utt;

    public JNIVecClient(int a_num_envs, int a_max_steps, RewardFunctionInterface[] a_rfs, String a_micrortsPath, String a_mapPath,
            AI[] a_ai2s, UnitTypeTable a_utt) throws Exception {
        maxSteps = a_max_steps;
        utt = a_utt;
        clients = new JNIClient[a_num_envs];
        for (int i = 0; i < a_num_envs; i++) {
            clients[i] = new JNIClient(a_rfs, a_micrortsPath, a_mapPath, a_ai2s[i], a_utt);
        }
        envSteps = new int[a_num_envs];
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
    }

    public Responses reset(int[] players) throws Exception {
        JNIClient.Response[] rs = new JNIClient.Response[players.length];
        for (int i = 0; i < players.length; i++) {
            rs[i] = clients[i].reset(players[i]);
        }
        int s1 = players.length, s2 = rs[0].observation.length, s3 = rs[0].observation[0].length,
                s4 = rs[0].observation[0][0].length;
        int[][][][] observation = new int[s1][s2][s3][s4];
        double[][] reward = new double[s1][rs[0].reward.length];
        boolean[][] done = new boolean[s1][rs[0].done.length];

        for (int i = 0; i < rs.length; i++) {
            observation[i] = rs[i].observation;
            reward[i] = rs[i].reward;
            done[i] = rs[i].done;
        }
        return new Responses(observation, reward, done);
    }

    public Responses step(int[][] action, int[] players) throws Exception {
        JNIClient.Response[] rs = new JNIClient.Response[players.length];
        for (int i = 0; i < players.length; i++) {
            envSteps[i] += 1;
            rs[i] = clients[i].step(new int[][] { action[i] }, players[i]);
            if (rs[i].done[0] || envSteps[i] >= maxSteps) {
                JNIClient.Response r = clients[i].reset(players[i]);
                rs[i].observation = r.observation;
                rs[i].done[0] = true;
                envSteps[i] = 0;
            }
        }
        int s1 = players.length, s2 = rs[0].observation.length, s3 = rs[0].observation[0].length,
                s4 = rs[0].observation[0][0].length;
        int[][][][] observation = new int[s1][s2][s3][s4];
        double[][] reward = new double[s1][rs[0].reward.length];
        boolean[][] done = new boolean[s1][rs[0].done.length];

        for (int i = 0; i < rs.length; i++) {
            observation[i] = rs[i].observation;
            reward[i] = rs[i].reward;
            done[i] = rs[i].done;
        }
        return new Responses(observation, reward, done);
    }

    public Responses gameStep(int[][][] action, int[] players) throws Exception {
        JNIClient.Response[] rs = new JNIClient.Response[players.length];
        for (int i = 0; i < players.length; i++) {
            envSteps[i] += 1;
            rs[i] = clients[i].gameStep(action[i], players[i]);
            if (rs[i].done[0] || envSteps[i] >= maxSteps) {
                JNIClient.Response r = clients[i].reset(players[i]);
                rs[i].observation = r.observation;
                rs[i].done[0] = true;
                envSteps[i] = 0;
            }
        }
        int s1 = players.length, s2 = rs[0].observation.length, s3 = rs[0].observation[0].length,
                s4 = rs[0].observation[0][0].length;
        int[][][][] observation = new int[s1][s2][s3][s4];
        double[][] reward = new double[s1][rs[0].reward.length];
        boolean[][] done = new boolean[s1][rs[0].done.length];

        for (int i = 0; i < rs.length; i++) {
            observation[i] = rs[i].observation;
            reward[i] = rs[i].reward;
            done[i] = rs[i].done;
        }
        return new Responses(observation, reward, done);
    }

    public int[][][] getUnitLocationMasks() {
        int[][][] unitLocationMasks = new int[clients.length][clients[0].ai1UnitMasks.length][clients[0].ai1UnitMasks[0].length];
        for (int i = 0; i < clients.length; i++) {
            unitLocationMasks[i] = clients[i].ai1UnitMasks;
        }
        return unitLocationMasks;
    }

    public int[][] getUnitActionMasks(int[] units) throws Exception {
        int[][] unitLocationMasks = new int[clients.length][6+4+4+4+4];
        for (int i = 0; i < clients.length; i++) {
            unitLocationMasks[i] = clients[i].getUnitActionMasks(new int[][]{{units[i]}})[0];
        }
        return unitLocationMasks;
    }

}
