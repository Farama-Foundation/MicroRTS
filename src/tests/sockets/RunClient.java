/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package tests.sockets;

import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ai.PassiveAI;
import ai.RandomBiasedAI;
import ai.RandomNoAttackAI;
import ai.core.AI;
import ai.socket.IndividualSocketRewardAI;
import ai.socket.SocketAIInterface;
import ai.socket.SocketRewardAI;
import ai.socket.SocketRewardPenaltyOnInvalidActionAI;
import gui.PhysicalGameStateJFrame;
import gui.PhysicalGameStatePanel;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
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
public class RunClient {
    @Parameter(names = "--server-ip", description = "The microRTS server IP")
    String serverIP = "127.0.0.1";

    @Parameter(names = "--server-port", description = "The microRTS server port")
    int serverPort = 9898;

    @Parameter(names = "--unix-socket-path", description = "The path to the unix domain socket file")
    String unixSocketPath = "/home/costa/Documents/work/go/src/github.com/vwxyzjn/gym-microrts/unix/u";

    @Parameter(names = "--map", description = "Which map in the `maps` folder are you using?")
    String map = "maps/4x4/baseTwoWorkersMaxResources4x4.xml";

    @Parameter(names = "--ai1-type", description = "The type of AI1")
    String ai1Type = "no-penalty";

    @Parameter(names = "--ai2-type", description = "The type of AI2")
    String ai2Type = "passive";

    @Parameter(names = "--window-size", description = "The microRTS server IP")
    int windowSize = 1;

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
        utt.getUnitType("Worker").harvestTime = 10;

        boolean gameover = false;
        boolean layerJSON = true;
        
        switch (ai1Type) {
            case "penalty":
                ai1 = new SocketRewardPenaltyOnInvalidActionAI(100, 0, serverIP, serverPort, SocketRewardAI.LANGUAGE_JSON, utt, layerJSON);
                break;
            case "no-penalty":
                if (unixSocketPath.length() > 0) {
                    ai1 = new SocketRewardAI(100, 0, unixSocketPath, SocketRewardAI.LANGUAGE_JSON, utt, layerJSON);
                    System.out.println("unixSocket used");
                } else {
                    ai1 = new SocketRewardAI(100, 0, serverIP, serverPort, SocketRewardAI.LANGUAGE_JSON, utt, layerJSON);
                }
                break;
            case "no-penalty-individual":
                if (unixSocketPath.length() > 0) {
                    ai1 = new IndividualSocketRewardAI(100, 0, unixSocketPath, SocketRewardAI.LANGUAGE_JSON, utt, layerJSON, windowSize);
                } else {
                    ai1 = new IndividualSocketRewardAI(100, 0, serverIP, serverPort, SocketRewardAI.LANGUAGE_JSON, utt, layerJSON, windowSize);
                }
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

        // game evaluation
        ArrayList <Integer> firstHarvestedResourcesTimesteps = new ArrayList<Integer>();
        ArrayList <Integer> firstReturnedResourcesTimesteps = new ArrayList<Integer>();
        ArrayList <Integer> resourcesGathereds = new ArrayList<Integer>();
        while (true) {
            boolean firstReturnedResources = true;
            boolean firstHarvestedResources = true;
            int firstReturnedResourcesTimestep = 2000;
            int firstHarvestedResourcesTimestep = 2000;
            ai1.reset();
            ai2.reset();
            pgs = PhysicalGameState.load(map, utt);
            gs = new GameState(pgs, utt);
            while (true) {
                ai1.computeReward(0, 1, gs);
                if (ai1.getReward() == 10.0 && firstHarvestedResources) {
                    firstHarvestedResources = false;
                    firstHarvestedResourcesTimestep = gs.getTime();
                }
                if (gs.getPlayer(0).getResources() == 6 && firstReturnedResources) {
                    firstReturnedResources = false;
                    firstReturnedResourcesTimestep = gs.getTime();
                }
                PlayerAction pa1 = ai1.getAction(0, gs);
                if (ai1.getRender()) {
                    if (w==null) {
                        w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);
                    }
                    w.setStateCloning(gs);
                    w.repaint();
                    ai1.sendGameStateRGBArray(w);
                    continue;
                }
                if (ai1.getReset()) {
                    break;
                }
                PlayerAction pa2 = ai2.getAction(1, gs);
                gs.issueSafe(pa1);
                gs.issueSafe(pa2);

                // simulate:
                gameover = gs.cycle();
                if (gameover) {
                    ai1.gameOver(gs.winner());
                }
                try {
                    Thread.yield();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            firstReturnedResourcesTimesteps.add(firstReturnedResourcesTimestep);
            firstHarvestedResourcesTimesteps.add(firstHarvestedResourcesTimestep);
            resourcesGathereds.add(gs.getPlayer(0).getResources()-5);
            ai2.gameOver(gs.winner());
            if (ai1.getFinished()) {
                System.out.println("Socket client finished");
                break;
            }
        }
        if (w!=null) {
            w.dispose();    
        }
        if (evaluationFileName.length() != 0) {
            try (Writer writer = new FileWriter(evaluationFileName)) {
                Map<String, Object> eval = new HashMap<String, Object>();
                eval.put("average_first_returned_resources_timestep", calculateAverage(firstReturnedResourcesTimesteps));
                eval.put("first_returned_resources_timestep", firstReturnedResourcesTimesteps);
                eval.put("average_total_resources_gathered", calculateAverage(resourcesGathereds));
                eval.put("total_resources_gathered", resourcesGathereds);
                eval.put("average_first_harvested_resources_timestep", calculateAverage(firstHarvestedResourcesTimesteps));
                eval.put("first_harvested_resources_timestep", firstHarvestedResourcesTimesteps);
                eval.put("episodes_run", firstReturnedResourcesTimesteps.size());
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
