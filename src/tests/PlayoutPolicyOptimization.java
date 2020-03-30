package tests;

import ai.RandomBiasedAI;
import ai.core.AI;
import ai.stochastic.UnitActionProbabilityDistributionAI;
import ai.stochastic.UnitActionTypeConstantDistribution;
import gui.PhysicalGameStatePanel;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

import javax.swing.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayoutPolicyOptimization {
    static Random r = new Random();
    
    static List<Integer> maxGameLength = new ArrayList<>();
    static UnitTypeTable utt = new UnitTypeTable();

    public static double computeRandomAIWinrate(double[] action_distribution, int mapIdx, int worker, String micrortsPath) throws Exception {
        List<String> maps = new ArrayList<>();
        maps.add(Paths.get(micrortsPath, "maps/8x8/basesWorkers8x8A.xml").toString());
        maxGameLength.add(3000);
        maps.add(Paths.get(micrortsPath, "maps/8x8/FourBasesWorkers8x8.xml").toString());
        maxGameLength.add(3000);
        maps.add(Paths.get(micrortsPath, "maps/NoWhereToRun9x8.xml").toString());
        maxGameLength.add(3000);
        maps.add(Paths.get(micrortsPath, "maps/16x16/basesWorkers16x16A.xml").toString());
        maxGameLength.add(4000);
        maps.add(Paths.get(micrortsPath, "maps/16x16/TwoBasesBarracks16x16.xml").toString());
        maxGameLength.add(4000);
        
        boolean visualize = false;
        UnitActionProbabilityDistributionAI pp1 = new UnitActionProbabilityDistributionAI(new UnitActionTypeConstantDistribution(utt, action_distribution), utt, "uapdai1");
        AI ai1 = pp1;
        AI ai2 = new RandomBiasedAI();
        ArrayList<Double> rewards = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();
        for (int j = 0; j < worker; j++) {
            Runnable runnable =
                    () -> {
                        try {
                            double reward = 0.5;
                            if (r.nextBoolean()) {
                                GameState endState = runGame(ai1, ai2, maps.get(mapIdx), maxGameLength.get(mapIdx), visualize, utt);
                                if (endState.winner() == 0) reward = 1.0;
                                if (endState.winner() == 1) reward = 0.0;
                            } else {
                                GameState endState = runGame(ai2, ai1, maps.get(mapIdx), maxGameLength.get(mapIdx), visualize, utt);
                                if (endState.winner() == 0) reward = 0.0;
                                if (endState.winner() == 1) reward = 1.0;
                            }
                            synchronized (rewards) {
                                rewards.add(reward);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    };
            Thread thread = new Thread(runnable);
            threads.add(thread);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        double reward = 0;
        for (Double rw : rewards) {
            reward += rw;
        }
        reward /= rewards.size();
        return reward;
    }

    public static GameState runGame(AI ai1, AI ai2, String mapFile, int maxcycles, boolean visualize, UnitTypeTable utt) throws Exception {
        PhysicalGameState pgs = PhysicalGameState.load(mapFile, utt);
        GameState gs = new GameState(pgs, utt);
        JFrame w = null;
        if (visualize)
            w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);
        boolean gameover;
        do {
            PlayerAction pa1 = ai1.getAction(0, gs);
            PlayerAction pa2 = ai2.getAction(1, gs);
            gs.issueSafe(pa1);
            gs.issueSafe(pa2);
            gameover = gs.cycle();
            if (visualize) w.repaint();
            Thread.yield();
        } while (!gameover && gs.getTime() < maxcycles);
        ai1.gameOver(gs.winner());
        ai2.gameOver(gs.winner());
        return gs;
    }
}
