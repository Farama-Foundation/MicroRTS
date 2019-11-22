package rts;

import ai.core.AI;
import ai.socket.SocketAI;
import gui.PhysicalGameStatePanel;
import java.net.Socket;
import javax.swing.JFrame;
import rts.units.UnitTypeTable;

class RemoteGame extends Thread {

    private Socket socket;
    private UnitTypeTable unitTypeTable;
    private PhysicalGameState pgs;
    private GameState gameState;
    private GameSettings gameSettings;
    private boolean gameOver = false;

    private static int PERIOD = 20;


    RemoteGame(Socket socket, GameSettings gameSettings) {
        this.socket = socket;
        this.gameSettings = gameSettings;

        this.unitTypeTable = new UnitTypeTable(gameSettings.getUTTVersion(),gameSettings.getConflictPolicy());

        try {
            this.pgs = PhysicalGameState.load(gameSettings.getMapLocation(), unitTypeTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.gameState = new GameState(pgs, unitTypeTable);
    }

    /**
     * Starts the game.
     */
    @Override
    public void run() {
        try {
            // Generate players
            // player 1 is created from SocketAI
            AI player_one = SocketAI.createFromExistingSocket(100, 0, unitTypeTable,
                gameSettings.getSerializationType(), gameSettings.isIncludeConstantsInState(),
                gameSettings.isCompressTerrain(), socket);
            // player 2 is created using the info from gameSettings
            java.lang.reflect.Constructor cons2 = Class.forName(gameSettings.getAI2())
                .getConstructor(UnitTypeTable.class);
            AI player_two = (AI) cons2.newInstance(unitTypeTable);

            // Reset all players
            player_one.reset();
            player_two.reset();

            // allow for pre-game analysis
            player_one.preGameAnalysis(gameState,0);
            player_two.preGameAnalysis(gameState,0);

            // Setup UI
            JFrame w = PhysicalGameStatePanel.newVisualizer(gameState,640, 640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);

            long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
            do {
                if (System.currentTimeMillis() >= nextTimeToUpdate) {

                    GameState playerOneGameState = gameSettings.isPartiallyObservable() ? new PartiallyObservableGameState(gameState,0) : gameState;
                    GameState playerTwoGameState = gameSettings.isPartiallyObservable() ? new PartiallyObservableGameState(gameState,1) : gameState;

                    PlayerAction pa1 = player_one.getAction(0, playerOneGameState);
                    PlayerAction pa2 = player_two.getAction(1, playerTwoGameState);

                    gameState.issueSafe(pa1);
                    gameState.issueSafe(pa2);

                    // simulate:
                    gameOver = gameState.cycle();

                    w.repaint();
                    nextTimeToUpdate += PERIOD;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } while (!gameOver && gameState.getTime() < gameSettings.getMaxCycles());
            player_one.gameOver(gameState.winner());
            player_two.gameOver(gameState.winner());
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}



