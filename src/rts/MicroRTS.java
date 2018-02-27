package rts;

import ai.core.AI;
import gui.PhysicalGameStatePanel;
import gui.frontend.FrontEnd;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JFrame;
import rts.units.UnitTypeTable;

/***
 * The main class for running a MicroRTS game. To modify existing settings change the file "config.properties".
 */
public class MicroRTS {

    public static void main(String args[]) throws Exception {
        GameSettings gameSettings = GameSettings.loadFromConfig(GameSettings.fetchDefaultConfig());
        System.out.println(gameSettings);

        switch (gameSettings.getLaunchMode()) {
            case STANDALONE:
                runStandAloneGame(gameSettings);
            case GUI:
                FrontEnd.main(args);
            case SERVER:
                startServer(gameSettings);
                break;
            case CLIENT:
                startClient(gameSettings);
                break;
        }
    }

    /**
     * Starts microRTS as a server instance.
     * @param gameSettings The game settings.
     */
    private static void startServer(GameSettings gameSettings) throws Exception {
        try(ServerSocket serverSocket = new ServerSocket(gameSettings.getServerPort())) {
            while(true) {
                try( Socket socket = serverSocket.accept() ) {
                    new RemoteGame(socket, gameSettings).run();
                } catch (Exception e ) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Starts microRTS as a client instance.
     * @param gameSettings The game settings.
     */
    private static void startClient(GameSettings gameSettings) throws Exception {
        Socket socket = new Socket(gameSettings.getServerAddress(), gameSettings.getServerPort());
        new RemoteGame(socket, gameSettings).run();
    }
    
    
    /**
     * Starts a standalone game of microRTS with the specified opponents, and game setting
     * @param gameSettings
     * @throws Exception 
     */
    public static void runStandAloneGame(GameSettings gameSettings) throws Exception {
        UnitTypeTable utt = new UnitTypeTable(gameSettings.getUTTVersion(), gameSettings.getConflictPolicy());
        PhysicalGameState pgs = PhysicalGameState.load(gameSettings.getMapLocation(), utt);

        GameState gs = new GameState(pgs, utt);
        int PERIOD = 20;
        boolean gameover = false;
        
        Constructor cons1 = Class.forName(gameSettings.getAI1()).getConstructor(UnitTypeTable.class);
        AI ai1 = (AI)cons1.newInstance(utt);
        Constructor cons2 = Class.forName(gameSettings.getAI2()).getConstructor(UnitTypeTable.class);
        AI ai2 = (AI)cons2.newInstance(utt);

        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,gameSettings.isPartiallyObservable(),
                                                        PhysicalGameStatePanel.COLORSCHEME_BLACK);

        long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
        do{
            if (System.currentTimeMillis()>=nextTimeToUpdate) {
                if (gameSettings.isPartiallyObservable()) {
                    PlayerAction pa1 = ai1.getAction(0, new PartiallyObservableGameState(gs,0));
                    PlayerAction pa2 = ai2.getAction(1, new PartiallyObservableGameState(gs,1));            
                    gs.issueSafe(pa1);
                    gs.issueSafe(pa2);
                } else {
                    PlayerAction pa1 = ai1.getAction(0, gs);
                    PlayerAction pa2 = ai2.getAction(1, gs);
                    gs.issueSafe(pa1);
                    gs.issueSafe(pa2);
                }

                // simulate:
                gameover = gs.cycle();
                w.repaint();
                nextTimeToUpdate+=PERIOD;
            } else {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }while(!gameover && gs.getTime()<gameSettings.getMaxCycles());
        ai1.gameOver(gs.winner());
        ai2.gameOver(gs.winner());        
    }       
}
