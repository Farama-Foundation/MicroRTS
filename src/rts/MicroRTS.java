package rts;

import gui.frontend.FrontEnd;
import java.net.ServerSocket;
import java.net.Socket;

/***
 * The main class for running a MicroRTS game. To modify existing settings change the file "config.properties".
 */
public class MicroRTS {

    public static void main(String args[]) throws Exception {

        for (int i = args.length; i > 0; i--) {
            if (args[i - 1].equals("-h")) {
                System.out.println(GameSettings.getHelpMessage());
                return;
            }
        }
        
        String configFile = "resources/config.properties";

        for (int i = args.length; i > 0; i--) {
            if (args[i - 1].equals("-f")) {
                configFile = args[i];
            }
        }

        GameSettings gameSettings;
        try {
            gameSettings = GameSettings.loadFromConfig(GameSettings.fetchConfig(configFile))
                .overrideFromArgs(args);
        } catch (java.io.FileNotFoundException ex) {
            System.err.println(
                "File " + configFile + " not found. Trying to initialize from command-line args.");
            gameSettings = new GameSettings(args);
        }

        System.out.println(gameSettings);

        switch (gameSettings.getLaunchMode()) {
            case STANDALONE:
            case HUMAN:
                runStandAloneGame(gameSettings);
                break;
            case GUI:
                FrontEnd.main(args);
                break;
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
        if (gameSettings.getLaunchMode() == GameSettings.LaunchMode.STANDALONE)
            new Game(gameSettings).start();
        else if (gameSettings.getLaunchMode() == GameSettings.LaunchMode.HUMAN)
            new MouseGame(gameSettings).start();
    }
}
