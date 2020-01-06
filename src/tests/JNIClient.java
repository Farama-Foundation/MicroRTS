/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package tests;

import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;
import javax.imageio.ImageIO;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ai.PassiveAI;
import ai.RandomBiasedAI;
import ai.RandomNoAttackAI;
import ai.core.AI;
import ai.jni.JNIAI;
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
public class JNIClient {
    @Parameter(names = "--server-ip", description = "The microRTS server IP")
    String serverIP = "127.0.0.1";

    @Parameter(names = "--server-port", description = "The microRTS server port")
    int serverPort = 9898;

    @Parameter(names = "--unix-socket-path", description = "The path to the unix domain socket file")
    String unixSocketPath = "/home/costa/Documents/work/go/src/github.com/vwxyzjn/gym-microrts/unix/u";

    @Parameter(names = "--map", description = "Which map in the `maps` folder are you using?")
    String map = "maps/4x4/baseTwoWorkersMaxResources4x4.xml";

    @Parameter(names = "--ai1-type", description = "The type of AI1")
    String ai1Type = "random-no-attack";

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
    public JNIAI ai1;
    public AI ai2;
    PhysicalGameState pgs;
    GameState gs;
    UnitTypeTable utt;
    boolean gameover = false;
    boolean layerJSON = true;

    public class Response {
        public int[][][] observation;
        public double reward;
        public boolean done;
        public String info;

        public Response(int[][][] observation, double reward, boolean done, String info) {
            this.observation = observation;
            this.reward = reward;
            this.done = done;
            this.info = info;
        }
    }

    public JNIClient() throws Exception{
        utt = new UnitTypeTable();
        utt.getUnitType("Worker").harvestTime = 10;
        ai1 = new JNIAI(100, 0, utt);
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
        if (micrortsPath.length() != 0) {
            map = Paths.get(micrortsPath, map).toString();
        }
        System.out.println(map);
    }

    // public byte[] render(boolean returnPixels) throws Exception {
    //     long startTime = System.nanoTime();
    //     if (w==null) {
    //         w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);
    //     }
    //     w.setStateCloning(gs);
    //     w.repaint();
    //     if (!returnPixels) {
    //         return null;
    //     }

    //     BufferedImage image = new BufferedImage(w.getWidth(),
    //     w.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    //     w.paint(image.getGraphics());
    //     ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //     ImageIO.write(image, "jpg", baos);
    //     return baos.toByteArray();
    // }

    public byte[] render(boolean returnPixels) throws Exception {
        long startTime = System.nanoTime();
        if (w==null) {
            w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);
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

    public Response step(int[][] action) throws Exception {
        PlayerAction pa1 = ai1.getAction(0, gs, action);
        PlayerAction pa2 = ai2.getAction(1, gs);
        gs.issueSafe(pa1);
        gs.issueSafe(pa2);

        // simulate:
        gameover = gs.cycle();
        if (gameover) {
            ai1.gameOver(gs.winner());
            ai2.gameOver(gs.winner());
        }
        try {
            Thread.yield();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TODO return observation in JSON format
        return new Response(
            ai1.getObservation(0, gs),
            ai1.computeReward(0, 1, gs),
            gameover,
            "{}");
    }

    public String sendUTT() throws Exception {
        Writer w = new StringWriter();
        utt.toJSON(w);
        return w.toString(); // now it works fine
    }

    public Response reset() throws Exception {
        ai1.reset();
        ai2.reset();
        pgs = PhysicalGameState.load(map, utt);
        gs = new GameState(pgs, utt);
        return new Response(
            ai1.getObservation(0, gs),
            0.0,
            false,
            "{}");
    }

    public void close() throws Exception {
        if (w!=null) {
            w.dispose();    
        }
    }

    private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;
  
        int[][] result = new int[height][width];
        if (hasAlphaChannel) {
           final int pixelLength = 4;
           for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
              int argb = 0;
              argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
              argb += ((int) pixels[pixel + 1] & 0xff); // blue
              argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
              argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
              result[row][col] = argb;
              col++;
              if (col == width) {
                 col = 0;
                 row++;
              }
           }
        } else {
           final int pixelLength = 3;
           for (int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += pixelLength) {
              int argb = 0;
              argb += -16777216; // 255 alpha
              argb += ((int) pixels[pixel] & 0xff); // blue
              argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
              argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
              result[row][col] = argb;
              col++;
              if (col == width) {
                 col = 0;
                 row++;
              }
           }
        }
  
        return result;
     }

     private static int[][][] convertTo2DWithoutUsingGetRGB2(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;
  
        int[][] result = new int[height][width];

        int[][][] rgbarray = new int[height][width][3];
        if (hasAlphaChannel) {
           final int pixelLength = 4;
           for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
                rgbarray[row][col][0] = ((int) pixels[pixel + 1] & 0xff);
                rgbarray[row][col][1] = (((int) pixels[pixel + 2] & 0xff) << 8);
                rgbarray[row][col][2] = (((int) pixels[pixel + 3] & 0xff) << 16);
              col++;
              if (col == width) {
                 col = 0;
                 row++;
              }
           }
        } else {
           final int pixelLength = 3;
           for (int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += pixelLength) {
              rgbarray[row][col][0] = ((int) pixels[pixel] & 0xff);
              rgbarray[row][col][1] = (((int) pixels[pixel + 1] & 0xff) << 8);
              rgbarray[row][col][2] = (((int) pixels[pixel + 2] & 0xff) << 16);
              col++;
              if (col == width) {
                 col = 0;
                 row++;
              }
           }
        }
  
        return rgbarray;
     }

     private static int[][] convertTo2DUsingGetRGB(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] result = new int[height][width];
  
        for (int row = 0; row < height; row++) {
           for (int col = 0; col < width; col++) {
              result[row][col] = image.getRGB(col, row);
           }
        }
  
        return result;
     }
}
