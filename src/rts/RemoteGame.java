package rts;

import ai.core.AI;
import ai.socket.SocketAI;
import java.lang.reflect.Constructor;
import java.net.Socket;
import rts.units.UnitTypeTable;

class RemoteGame implements Runnable {

    private Socket socket;
    private GameSettings gameSettings;

    RemoteGame(Socket socket, GameSettings gameSettings) {
        this.socket = socket;
        this.gameSettings = gameSettings;
    }

    /**
     * Starts the game.
     */
    @Override
    public void run() {
        try {
            UnitTypeTable unitTypeTable = new UnitTypeTable(
                gameSettings.getUTTVersion(), gameSettings.getConflictPolicy());

            // Generate players
            // player 1 is created from SocketAI
            AI player_one = SocketAI.createFromExistingSocket(100, 0, unitTypeTable,
                gameSettings.getSerializationType(), gameSettings.isIncludeConstantsInState(),
                gameSettings.isCompressTerrain(), socket);
            // player 2 is created using the info from gameSettings
            Constructor cons2 = Class.forName(gameSettings.getAI2())
                .getConstructor(UnitTypeTable.class);
            AI player_two = (AI) cons2.newInstance(unitTypeTable);

            Game game = new Game(gameSettings, player_one, player_two);
            game.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
