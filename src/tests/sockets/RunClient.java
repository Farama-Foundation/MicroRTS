/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package tests.sockets;

import ai.core.AI;
import ai.*;
import ai.socket.IndividualSocketRewardAI;
import ai.socket.SocketAI;
import ai.socket.SocketAIInterface;
import gui.PhysicalGameStatePanel;

import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import ai.socket.SocketRewardAI;
import ai.socket.SocketRewardPenaltyOnInvalidActionAI;
import gui.PhysicalGameStateJFrame;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
public class RunClient {
    @Parameter(names = "--server-ip", description = "The microRTS server IP")
    String serverIP = "127.0.0.1";

    @Parameter(names = "--server-port", description = "The microRTS server port")
    int serverPort = 9898;

    @Parameter(names = "--map", description = "Which map in the `maps` folder are you using?")
    String map = "maps/4x4/baseTwoWorkersMaxResources4x4.xml";

    @Parameter(names = "--ai1-type", description = "The type of AI1")
    String ai1Type = "random-no-attack";

    @Parameter(names = "--ai2-type", description = "The type of AI2")
    String ai2Type = "passive";

    @Parameter(names = "--window-size", description = "The microRTS server IP")
    int windowSize = 2;

    @Parameter(names = "--render", description = "Whether to render the game")
    boolean render = true;

    @Parameter(names = "--seed", description = "The random seed")
    int seed = 3;

    @Parameter(names = "--evaluation-filename", description = "Whether to save the evaluation results in a the supplied filename")
    String evaluationFileName = "./test.json";

    @Parameter(names = "--microrts-path", description = "The path of microrts unzipped folder")
    String micrortsPath = "";

    PhysicalGameStateJFrame w;
    SocketAIInterface ai1;
    AI ai2;

    public static void main(String args[]) throws Exception {
        RunClient rc = new RunClient();
        JCommander.newBuilder().addObject(rc).build().parse(args);
        rc.run();
    }

    public void run() throws Exception {

        UnitTypeTable utt = new UnitTypeTable();

        boolean gameover = false;
        boolean layerJSON = true;
        
        switch (ai1Type) {
            case "penalty":
                ai1 = new SocketRewardPenaltyOnInvalidActionAI(100, 0, serverIP, serverPort, SocketRewardAI.LANGUAGE_JSON, utt, layerJSON);
                break;
            case "no-penalty":
                ai1 = new SocketRewardAI(100, 0, serverIP, serverPort, SocketRewardAI.LANGUAGE_JSON, utt, layerJSON);
                break;
            case "no-penalty-individual":
                ai1 = new IndividualSocketRewardAI(100, 0, serverIP, serverPort, SocketRewardAI.LANGUAGE_JSON, utt, layerJSON, windowSize);
                break;
            case "random-no-attack":
                ai1 = new RandomNoAttackAI(seed);
                break;
            default:
                throw new Exception("no ai1 was chosen");
        }
        switch (ai2Type) {
            case "passive":
                ai2 = new PassiveAI();
                break;
            case "random-biased":
                ai2 = new RandomBiasedAI();
                break;
            default:
                throw new Exception("no ai2 was chosen");
        }

        System.out.println("Socket client started");

        if (micrortsPath.length() != 0) {
            map = Paths.get(micrortsPath, map).toString();
        }

        PhysicalGameState pgs = PhysicalGameState.load(map, utt);
        GameState gs = new GameState(pgs, utt);
        if (render) {
            w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);
        }

        // game evaluation
        ArrayList <Integer> firstMineTimesteps = new ArrayList<Integer>();
        ArrayList <Integer> resourcesGathereds = new ArrayList<Integer>();
        while (true) {
            int firstMineTimestep = 2000;
            ai1.reset();
            ai2.reset();
            pgs = PhysicalGameState.load(map, utt);
            gs = new GameState(pgs, utt);
            while (true) {
                if (render) {
                    w.setStateCloning(gs);
                    w.repaint();
                }
                ai1.computeReward(0, 1, gs);
                if (gs.getPlayer(0).getResources() == 6) {
                    firstMineTimestep = gs.getTime();
                }
                PlayerAction pa1 = ai1.getAction(0, gs);
                if (ai1.getDone()) {
                    break;
                }
                PlayerAction pa2 = ai2.getAction(1, gs);
                gs.issueSafe(pa1);
                gs.issueSafe(pa2);

                // simulate:
                gameover = gs.cycle();
                if (gameover) {
                    break;
                }
                try {
                    Thread.yield();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            firstMineTimesteps.add(firstMineTimestep);
            resourcesGathereds.add(gs.getPlayer(0).getResources()-5);
            ai1.gameOver(gs.winner(), gs);
            ai2.gameOver(gs.winner());
            if (ai1.getFinished()) {
                System.out.println("Socket client finished");
                break;
            }
        }
        if (render) {
            w.dispose();
        }
        if (evaluationFileName.length() != 0) {
            try (Writer writer = new FileWriter(evaluationFileName)) {
                Map<String, Object> eval = new HashMap<String, Object>();
                eval.put("average_first_mine_timestep", calculateAverage(firstMineTimesteps));
                eval.put("average_total_resources_gathered", calculateAverage(resourcesGathereds));
                Gson gson = new GsonBuilder().create();
                gson.toJson(eval, writer);
            }
        }
    }

    private double calculateAverage(ArrayList<Integer> array) {
      Integer sum = 0;
      if(!array.isEmpty()) {
        for (Integer item : array) {
            sum += item;
        }
        return sum.doubleValue() / array.size();
      }
      return sum;
    }
}
