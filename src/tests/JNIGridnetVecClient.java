package tests;

import ai.PassiveAI;
import ai.core.AI;
import ai.jni.Response;
import ai.jni.Responses;
import ai.reward.RewardFunctionInterface;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi and costa
 */
public class JNIGridnetVecClient {
    public JNIGridnetClient[] clients;
    public JNIGridnetClientSelfPlay[] selfPlayClients;
    public JNIBotClient[] botClients;
    public int maxSteps;
    public int[] envSteps; 
    public RewardFunctionInterface[] rfs;
    public UnitTypeTable utt;
    boolean partialObs = false;
    public String[] mapPaths;

    // storage
    int[][][][] masks;
    int[][][][] observation;
    double[][] reward;
    boolean[][] done;
    Response[] rs;
    Responses responses;

    double[] terminalReward1;
    boolean[] terminalRone1;
    double[] terminalReward2;
    boolean[] terminalRone2;

    public JNIGridnetVecClient(int a_num_selfplayenvs, int a_num_envs, int a_max_steps, RewardFunctionInterface[] a_rfs, String a_micrortsPath, String[] a_mapPaths,
        AI[] a_ai2s, UnitTypeTable a_utt, boolean partial_obs) throws Exception {
        maxSteps = a_max_steps;
        utt = a_utt;
        rfs = a_rfs;
        partialObs = partial_obs;
        mapPaths = a_mapPaths;

        // initialize clients
        envSteps = new int[a_num_selfplayenvs + a_num_envs];
        selfPlayClients = new JNIGridnetClientSelfPlay[a_num_selfplayenvs/2];
        for (int i = 0; i < selfPlayClients.length; i++) {
            selfPlayClients[i] = new JNIGridnetClientSelfPlay(a_rfs, a_micrortsPath, mapPaths[i*2], a_utt, partialObs);
        }
        clients = new JNIGridnetClient[a_num_envs];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = new JNIGridnetClient(a_rfs, a_micrortsPath, mapPaths[a_num_selfplayenvs+i], a_ai2s[i], a_utt, partialObs);
        }

        // initialize storage
        Response r = new JNIGridnetClient(a_rfs, a_micrortsPath, mapPaths[0], new PassiveAI(a_utt), a_utt, partialObs).reset(0);
        int s1 = a_num_selfplayenvs + a_num_envs, s2 = r.observation.length, s3 = r.observation[0].length,
                s4 = r.observation[0][0].length;
        masks = new int[s1][][][];
        observation = new int[s1][s2][s3][s4];
        reward = new double[s1][rfs.length];
        done = new boolean[s1][rfs.length];
        terminalReward1 = new double[rfs.length];
        terminalRone1 = new boolean[rfs.length];
        terminalReward2 = new double[rfs.length];
        terminalRone2 = new boolean[rfs.length];
        responses = new Responses(null, null, null);
        rs = new Response[s1];
    }

    public JNIGridnetVecClient(int a_max_steps, RewardFunctionInterface[] a_rfs, String a_micrortsPath, String[] a_mapPaths,
        AI[] a_ai1s, AI[] a_ai2s, UnitTypeTable a_utt, boolean partial_obs) throws Exception {
        maxSteps = a_max_steps;
        utt = a_utt;
        rfs = a_rfs;
        partialObs = partial_obs;
        mapPaths = a_mapPaths;

        // initialize clients
        botClients = new JNIBotClient[a_ai2s.length];
        for (int i = 0; i < botClients.length; i++) {
            botClients[i] = new JNIBotClient(a_rfs, a_micrortsPath, mapPaths[i], a_ai1s[i], a_ai2s[i], a_utt, partialObs);
        }
        responses = new Responses(null, null, null);
        rs = new Response[a_ai2s.length];
        reward = new double[a_ai2s.length][rfs.length];
        done = new boolean[a_ai2s.length][rfs.length];
        envSteps = new int[a_ai2s.length];
        terminalReward1 = new double[rfs.length];
        terminalRone1 = new boolean[rfs.length];
    }

    public Responses reset(int[] players) throws Exception {
        if (botClients != null) {
            for (int i = 0; i < botClients.length; i++) {
                rs[i] = botClients[i].reset(players[i]);
            }
            for (int i = 0; i < rs.length; i++) {
                // observation[i] = rs[i].observation;
                reward[i] = rs[i].reward;
                done[i] = rs[i].done;
            }
            responses.set(null, reward, done);
            return responses;
        }
        for (int i = 0; i < selfPlayClients.length; i++) {
            selfPlayClients[i].reset();
            rs[i*2] = selfPlayClients[i].getResponse(0);
            rs[i*2+1] = selfPlayClients[i].getResponse(1);
        }
        for (int i = selfPlayClients.length*2; i < players.length; i++) {
            rs[i] = clients[i-selfPlayClients.length*2].reset(players[i]);
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
        if (botClients != null) {
            for (int i = 0; i < botClients.length; i++) {
                rs[i] = botClients[i].gameStep(players[i]);
                envSteps[i] += 1;
                if (rs[i].done[0] || envSteps[i] >= maxSteps) {
                    for (int j = 0; j < terminalReward1.length; j++) {
                        terminalReward1[j] = rs[i].reward[j];
                        terminalRone1[j] = rs[i].done[j];
                    }
                    botClients[i].reset(players[i]);
                    for (int j = 0; j < terminalReward1.length; j++) {
                        rs[i].reward[j] = terminalReward1[j];
                        rs[i].done[j] = terminalRone1[j];
                    }
                    rs[i].done[0] = true;
                    envSteps[i] =0;
                }
            }
            for (int i = 0; i < rs.length; i++) {
                // observation[i] = rs[i].observation;
                reward[i] = rs[i].reward;
                done[i] = rs[i].done;
            }
            responses.set(null, reward, done);
            return responses;
        }
        for (int i = 0; i < selfPlayClients.length; i++) {
            selfPlayClients[i].gameStep(action[i*2], action[i*2+1]);
            rs[i*2] = selfPlayClients[i].getResponse(0);
            rs[i*2+1] = selfPlayClients[i].getResponse(1);
            envSteps[i*2] += 1;
            envSteps[i*2+1] += 1;
            if (rs[i*2].done[0] || envSteps[i*2] >= maxSteps) {
                for (int j = 0; j < terminalReward1.length; j++) {
                    terminalReward1[j] = rs[i*2].reward[j];
                    terminalRone1[j] = rs[i*2].done[j];
                    terminalReward2[j] = rs[i*2+1].reward[j];
                    terminalRone2[j] = rs[i*2+1].done[j];
                }

                selfPlayClients[i].reset();
                for (int j = 0; j < terminalReward1.length; j++) {
                    rs[i*2].reward[j] = terminalReward1[j];
                    rs[i*2].done[j] = terminalRone1[j];
                    rs[i*2+1].reward[j] = terminalReward2[j];
                    rs[i*2+1].done[j] = terminalRone2[j];
                }
                rs[i*2].done[0] = true;
                rs[i*2+1].done[0] = true;
                envSteps[i*2] =0;
                envSteps[i*2+1] =0;
            }
        }

        for (int i = selfPlayClients.length*2; i < players.length; i++) {
            envSteps[i] += 1;
            rs[i] = clients[i-selfPlayClients.length*2].gameStep(action[i], players[i]);
            if (rs[i].done[0] || envSteps[i] >= maxSteps) {
                // TRICKY: note that `clients` already resets the shared `observation`
                // so we need to set the old reward and done to this response
                for (int j = 0; j < rs[i].reward.length; j++) {
                    terminalReward1[j] = rs[i].reward[j];
                    terminalRone1[j] = rs[i].done[j];
                }
                clients[i-selfPlayClients.length*2].reset(players[i]);
                for (int j = 0; j < rs[i].reward.length; j++) {
                    rs[i].reward[j] = terminalReward1[j];
                    rs[i].done[j] = terminalRone1[j];
                }
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
        for (int i = 0; i < selfPlayClients.length; i++) {
            masks[i*2] = selfPlayClients[i].getMasks(0);
            masks[i*2+1] = selfPlayClients[i].getMasks(1);
        }
        for (int i = selfPlayClients.length*2; i < masks.length; i++) {
            masks[i] = clients[i-selfPlayClients.length*2].getMasks(player);
        }
        return masks;
    }

    public void close() throws Exception {
        if (clients != null) {
            for (JNIGridnetClient client : clients) {
                client.close();
            }
        }
        if (selfPlayClients != null) {
            for (JNIGridnetClientSelfPlay client : selfPlayClients) {
                client.close();
            }
        }
        if (botClients != null) {
            for (int i = 0; i < botClients.length; i++) {
                botClients[i].close();
            }
        }
    }
}
