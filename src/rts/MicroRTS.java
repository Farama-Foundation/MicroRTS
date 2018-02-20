package rts;

import java.net.ServerSocket;
import java.net.Socket;

public class MicroRTS {

    public static void main(String args[]) throws Exception {
        GameSettings gameSettings = GameSettings.loadFromConfig(GameSettings.fetchDefaultConfig());
        System.out.println(gameSettings);

        if( gameSettings.isServer() ) {
            startServer(gameSettings);
        } else {
            startClient(gameSettings);
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
