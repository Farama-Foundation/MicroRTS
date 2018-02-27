package rts;

import gui.frontend.FrontEnd;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import java.net.ServerSocket;
import java.net.Socket;

/***
 * The main class for running a MicroRTS game. To modify existing settings change the file "config.properties".
 */
public class MicroRTS {

    public static void main(String args[]) throws Exception {
        GameSettings gameSettings = GameSettings.loadFromConfig(GameSettings.fetchDefaultConfig());
        System.out.println(gameSettings);

        switch (gameSettings.getLaunchMode()) {
            case SERVER:
                startServer(gameSettings);
                break;
            case CLIENT:
                startClient(gameSettings);
                break;
            case STANDALONE:
                throw new NotImplementedException();
            case TOURNAMENT:
                FrontEnd.main(args);
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
}
